export class ListView {
    #id;
    #container;
    #listContainer;
    #elementCountSpan;
    #list = [];
    #currentIndex = -1;
    #onItemMouseEnter;
    #onItemClicked;
    #visibleRange = { start: 0, end: 0 }; // Cache de la plage visible
    #selection = { start: -1, end: -1, type: null }; // Mémorisation de la sélection
    #editMode = 'truncate';

    /**
     * @param {HTMLElement} container - L'élément où injecter la liste
     * @param {string} title - Le titre de la liste
     * @param {Object} options - Callbacks pour les interactions
     */
    constructor(container, id, title, options = {}) {
        this.#id = id;
        this.#onItemMouseEnter = options.onItemMouseEnter || null;
        this.#onItemClicked = options.onItemClicked || null;
        
        container.innerHTML = `
            <div style="font-weight: bold; font-size: 11px; color: #888; display:flex; justify-content:space-between; align-items:center;">
                <span>${title}</span> <span class="element-count"> - </span>
                <div class="toolbar-moves">
                    <button class="icon-btn btn-copy" title="Copy">📋</button>
                    <button class="icon-btn btn-clear" title="Clear">🗑️</button>
                </div>
            </div>
            <div class="moves-display" contenteditable="true"></div>
            <div class="edit-mode-container" style="margin-top: 3px">
                    <button class="btn-action btn-mode-edit active" data-mode="truncate">TRUNC</button>
                    <button class="btn-action btn-mode-edit" data-mode="insert">INS</button>
                    <button class="btn-action btn-mode-edit" data-mode="overwrite">OVER</button>
            </div>
        `;
        this.#container = container;
        this.#listContainer = container.querySelector('.moves-display');
        this.#elementCountSpan = container.querySelector('.element-count');
        
        // Initialiser le cache de la plage visible
        this.#updateVisibleRange();
        
        // Observer les changements de taille pour invalider le cache
        if (globalThis.ResizeObserver) {
            const resizeObserver = new ResizeObserver(() => {
                this.#updateVisibleRange();
            });
            resizeObserver.observe(this.#listContainer);
        }

        container.querySelector('.btn-copy').onclick = () => this.#copyMoves();
        container.querySelector('.btn-clear').onclick = () => this.#clearMoves();
        container.querySelector('.moves-display').oninput = () => { this.#syncMoves(); this.#saveList(); };
        container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.onclick = () => {
                this.#editMode = btn.dataset.mode;
                this.#saveEditMode(); 
                this.#render();
            };
        });

        
        // Observer les scroll pour mettre à jour la plage visible
        this.#listContainer.addEventListener('scroll', () => {
            this.#updateVisibleRange();
        }, { passive: true });

        this.#loadLocal();
    }

    /**
     * Met à jour la liste complète et l'index actuel
     */
    update(list, currentIndex, redraw = true, save = true) {
        if (currentIndex < -1 || currentIndex >= list.length) {
            throw new Error("Invalid index");
        }
        const listChanged = !this.#arraysEqual(this.#list, list);
        const indexChanged = this.#currentIndex !== currentIndex;
        
        if (!listChanged && !indexChanged) return;

        const oldIndex = this.#currentIndex;
        this.#currentIndex = currentIndex;
        
        if (listChanged) {
            this.#list = list;
            if (save) {
                this.#saveList();
            }
            if (redraw) {
                this.#render();
            }
        } else if (indexChanged && redraw) {
            // Seul l'index a changé, on met juste à jour les classes
            this.#updateClassesOnly(oldIndex);
        }
        if (indexChanged && save) {
            this.#saveIndex();
        }
    }

    #saveIndex() {
        //TODO Should do debouncing
        // For now, save immediately
        localStorage.setItem(`ps_idx_${this.#id}`, this.#currentIndex); 
    }

    #saveEditMode() {
        localStorage.setItem(`ps_edit_mode_${this.#id}`, this.#editMode); 
    }

    #saveList() {
        localStorage.setItem(`ps_moves_${this.#id}`, JSON.stringify(this.#list)); 
    }

    #loadLocal() {
        const savedMoves = localStorage.getItem(`ps_moves_${this.#id}`);
        const savedIdx = localStorage.getItem(`ps_idx_${this.#id}`);
        const savedMode = localStorage.getItem(`ps_edit_mode_${this.#id}`);
        let idx = savedIdx ? Number.parseInt(savedIdx) : -1;
        this.#editMode = savedMode || 'truncate';
        if (savedMoves) {
            const moves = JSON.parse(savedMoves);
            // Prevent crashing if saved state is inconsistent (possibly by a previous release)
            if (idx < -1) {
                idx = -1;
            } else if (idx >= moves.length) {
                idx = moves.length - 1;
            }
            this.update(moves, idx, false, false);
        }
        this.#render();
    }
    #copyMoves() { navigator.clipboard.writeText(this.getMovesList().join(' ')); }
    #clearMoves() { 
        this.update([], -1);
    }
    #syncMoves() {
        const text = this.#listContainer.querySelector('.moves-display').innerText.replace(/,/g, ' ');
        let moves = text.trim().split(/\s+/).filter(x => x !== "");

        if (this.#currentIndex >= moves.length) this.#currentIndex = moves.length-1;
        this.update(moves, this.#currentIndex);
    }

    /**
     * Rendu complet de la liste (Méthode gourmande à optimiser plus tard)
     */
    #render() {
        this.#elementCountSpan.textContent = this.#list.length==0 ? "-" : "("+this.#list.length+" el)"
        this.#listContainer.innerHTML = '';
        
        this.#list.forEach((item, i) => {
            const span = document.createElement('span');
            this.#style(span, i);
            span.innerText = item;

            span.onclick = () => {
                this.setIndex(i);
                if (this.#onItemClicked) {
                    this.#onItemClicked(i);
                }
            };

            if (this.#onItemMouseEnter) {
                span.onmouseenter = () => this.#onItemMouseEnter(item, i, span);
            }

            this.#listContainer.appendChild(span);
        });
        this.#container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.mode === this.#editMode);
        });

        this.#scrollToSelected();
    }

    /** Scroll automatique vers l'élément actif */
    #scrollToSelected(currentElement = null) {
        // Optimisation: utiliser le cache de plage visible pour éviter les calculs
        const targetIndex = this.#currentIndex;
        
        // Si l'index cible est dans la plage visible, pas besoin de scroller
        if (targetIndex > this.#visibleRange.start && targetIndex <= this.#visibleRange.end) {
            return;
        }
        
        const current = currentElement || this.#listContainer.querySelector('.move-current');
        if (!current) return;

        // Sinon, scroller et mettre à jour le cache
        current.scrollIntoView({ behavior: 'auto', block: 'nearest' });
        this.#updateVisibleRange();
    }

    /**
     * Met à jour le cache de la plage d'éléments visibles
     */
    #updateVisibleRange() {
        const spans = this.#listContainer.children;
        if (spans.length === 0) {
            this.#visibleRange = { start: 0, end: 0 };
            return;
        }

        const containerRect = this.#listContainer.getBoundingClientRect();
        const containerTop = containerRect.top;
        const containerBottom = containerRect.bottom;

        let start = -1;
        let end = -1;

        for (let i = 0; i < spans.length; i++) {
            const span = spans[i];
            const spanRect = span.getBoundingClientRect();
            
            if (spanRect.bottom >= containerTop && spanRect.top <= containerBottom) {
                if (start === -1) start = i;
                end = i;
            }
        }

        this.#visibleRange = { start, end };
    }

    getIndex() { return this.#currentIndex; }

    setIndex(idx) {
        if (idx === this.#currentIndex) return;
        const oldIndex = this.#currentIndex;
        this.#currentIndex = idx;
        
        // Optimisation: mise à jour des classes seulement
        this.#updateClassesOnly(oldIndex);
        this.#saveIndex();
    }

    getList() { return this.#list; }

    /**
     * Applique une sélection sur une plage d'éléments avec un type spécifique
     * @param {number} start - Index de début (inclus)
     * @param {number} end - Index de fin (exclus)
     * @param {string} type - Type de sélection: 'better', 'neutral', 'worse'
     */
    applySelection(start, end, type = 'neutral') {
        // Mémoriser la sélection
        this.#selection = { start, end, type };
        
        const spans = this.#listContainer.querySelectorAll('.move-item');
        
        // Retirer toutes les classes de sélection existantes
        spans.forEach(span => {
            span.classList.remove('move-diff-better', 'move-diff-worse', 'move-diff-neutral');
        });
        
        // Appliquer la nouvelle sélection
        for (let i = start; i <= end; i++) {
            spans[i].classList.add(`move-diff-${type}`);
        }
    }

    /**
     * Efface toutes les sélections
     */
    clearSelection() {
        // Réinitialiser la sélection mémorisée
        this.#selection = { start: -1, end: -1, type: null };
        
        const spans = this.#listContainer.querySelectorAll('.move-item');
        spans.forEach(span => {
            span.classList.remove('move-diff-better', 'move-diff-worse', 'move-diff');
        });
    }

    /**
     * Compare deux tableaux pour savoir s'ils sont égaux
     */
    #arraysEqual(arr1, arr2) {
        if (arr1.length !== arr2.length) return false;
        for (let i = 0; i < arr1.length; i++) {
            if (arr1[i] !== arr2[i]) return false;
        }
        return true;
    }

    /**
     * Met à jour uniquement les classes CSS des éléments entre oldIndex et newIndex
     */
    #updateClassesOnly(oldIndex) {
		// Note: Could be optimized: we only have to remove or add move-past instead of rebuild the whole class
		// Nevertheless it would be more complex to make style customizable.
        const spans = this.#listContainer.querySelectorAll('.move-item');
        
        // Déterminer la plage d'éléments à mettre à jour
        const minIndex = Math.min(oldIndex, this.#currentIndex);
        const maxIndex = Math.max(oldIndex, this.#currentIndex);
        
        let selectedElement = null;

        for (let i = minIndex; i <= maxIndex; i++) {
            const span = spans[i];
            if (span) {
                this.#style(span, i);
                if (i === this.#currentIndex) {
                    selectedElement = span;
                }
            }
        }
        
        this.#scrollToSelected(selectedElement);
    }

    #style(span, i) {
        // Note: On pourrait passer une fonction de style en option pour détacher la logique métiers
        span.className = "move-item";
        if (i <= this.#currentIndex) {
            span.classList.add('move-past');
        }
        
        // Appliquer la sélection mémorisée si applicable
        if (this.#selection.type && i >= this.#selection.start && i <= this.#selection.end) {
            span.classList.add(`move-diff-${this.#selection.type}`);
        }
    }
}