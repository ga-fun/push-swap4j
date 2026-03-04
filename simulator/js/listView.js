export class ListView {
    #container;
    #list = [];
    #currentIndex = -1;
    #onItemClick;
    #onItemMouseEnter;
    #visibleRange = { start: 0, end: 0 }; // Cache de la plage visible

    /**
     * @param {HTMLElement} container - L'élément où injecter la liste
     * @param {Object} options - Callbacks pour les interactions
     */
    constructor(container, options = {}) {
        this.#container = container;
        this.#onItemClick = options.onItemClick || null;
        this.#onItemMouseEnter = options.onItemMouseEnter || null;
        
        // Initialiser le cache de la plage visible
        this.#updateVisibleRange();
        
        // Observer les changements de taille pour invalider le cache
        if (globalThis.ResizeObserver) {
            const resizeObserver = new ResizeObserver(() => {
                this.#updateVisibleRange();
            });
            resizeObserver.observe(this.#container);
        }
        
        // Observer les scroll pour mettre à jour la plage visible
        this.#container.addEventListener('scroll', () => {
            this.#updateVisibleRange();
        }, { passive: true });
    }

    /**
     * Met à jour la liste complète et l'index actuel
     */
    update(list, currentIndex, redraw = true) {
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
            if (redraw) {
                this.render();
            }
        } else if (indexChanged && redraw) {
            // Seul l'index a changé, on met juste à jour les classes
            this.#updateClassesOnly(oldIndex);
        }
    }

    /**
     * Rendu complet de la liste (Méthode gourmande à optimiser plus tard)
     */
    render() {
        this.#container.innerHTML = '';
        
        this.#list.forEach((item, i) => {
            const span = document.createElement('span');
            this.#style(span, i);
            span.innerText = item;

            if (this.#onItemClick) {
                span.onclick = () => this.#onItemClick(item, i);
            }

            if (this.#onItemMouseEnter) {
                span.onmouseenter = () => this.#onItemMouseEnter(item, i, span);
            }

            this.#container.appendChild(span);
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
        
        const current = currentElement || this.#container.querySelector('.move-current');
        if (!current) return;

        // Sinon, scroller et mettre à jour le cache
        current.scrollIntoView({ behavior: 'auto', block: 'nearest' });
        this.#updateVisibleRange();
    }

    /**
     * Met à jour le cache de la plage d'éléments visibles
     */
    #updateVisibleRange() {
        const spans = this.#container.children;
        if (spans.length === 0) {
            this.#visibleRange = { start: 0, end: 0 };
            return;
        }

        const containerRect = this.#container.getBoundingClientRect();
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
    }

    getList() { return this.#list; }

    /**
     * Applique une sélection sur une plage d'éléments avec un type spécifique
     * @param {number} start - Index de début (inclus)
     * @param {number} end - Index de fin (exclus)
     * @param {string} type - Type de sélection: 'better', 'neutral', 'worse'
     */
    applySelection(start, end, type = 'neutral') {
        const spans = this.#container.querySelectorAll('.move-item');
        
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
        const spans = this.#container.querySelectorAll('.move-item');
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
        const spans = this.#container.querySelectorAll('.move-item');
        
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
    }
}