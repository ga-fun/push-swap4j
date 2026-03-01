export class ListView {
    #container;
    #list = [];
    #currentIndex = 0;
    #onItemClick;
    #onItemMouseEnter;

    /**
     * @param {HTMLElement} container - L'élément où injecter la liste
     * @param {Object} options - Callbacks pour les interactions
     */
    constructor(container, options = {}) {
        this.#container = container;
        this.#onItemClick = options.onItemClick || null;
        this.#onItemMouseEnter = options.onItemMouseEnter || null;
    }

    /**
     * Met à jour la liste complète et l'index actuel
     */
    update(list, currentIndex, redraw = true) {
        const listChanged = !this.#arraysEqual(this.#list, list);
        const indexChanged = this.#currentIndex !== currentIndex;
        
        if (!listChanged && !indexChanged) return;
        
        if (listChanged) {
            this.#list = list;
            if (redraw) {
                this.render();
            }
        } else if (indexChanged && redraw) {
            // Seul l'index a changé, on met juste à jour les classes
            this.#updateClassesOnly(this.#currentIndex, currentIndex);
        }
        
        this.#currentIndex = currentIndex;
    }

    /**
     * Rendu complet de la liste (Méthode gourmande à optimiser plus tard)
     */
    render() {
        this.#container.innerHTML = '';
        
        this.#list.forEach((item, i) => {
            const span = document.createElement('span');
            
            // On garde la logique de classe spécifique (past, current)
            // Note: On pourrait passer une fonction de style en option pour détacher la logique métiers
            span.className = "move-item " + 
                (i === this.#currentIndex - 1 ? "move-current " : "") + 
                (i < this.#currentIndex ? "move-past" : "");
            
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
    #scrollToSelected() {
        const current = this.#container.querySelector('.move-current');
        if (current) {
            current.scrollIntoView({ behavior: 'auto', block: 'nearest' });
        }
    }

    getIndex() { return this.#currentIndex; }

    setIndex(idx) {
        console.log("setIndex", idx);
        if (idx === this.#currentIndex) return;
        const oldIndex = this.#currentIndex;
        this.#currentIndex = idx;
        
        // Optimisation: mise à jour des classes seulement
        this.#updateClassesOnly(oldIndex, idx);
    }

    getList() { return this.#list; }

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
    #updateClassesOnly(oldIndex, newIndex) {
        const spans = this.#container.querySelectorAll('.move-item');
        
        // Déterminer la plage d'éléments à mettre à jour
        const minIndex = Math.min(oldIndex, newIndex);
        const maxIndex = Math.max(oldIndex, newIndex);
        
        for (let i = minIndex; i <= maxIndex && i < spans.length; i++) {
            const span = spans[i];
            if (!span) continue;
            
            // Mettre à jour les classes
            span.className = "move-item " + 
                (i === this.#currentIndex - 1 ? "move-current " : "") + 
                (i < this.#currentIndex ? "move-past" : "");
        }
        
        this.#scrollToSelected();
    }
}