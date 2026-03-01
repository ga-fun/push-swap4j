export class TwoStacksView {
    #container;
    #viewA;
    #viewB;
    #stacks;
    #renderScheduled = false;

    constructor(parentContainer) {
        this.#container = parentContainer;
        this.#container.classList.add('ps-visualizer');
        this.#container.innerHTML = `
            <div class="stack-container"><strong>A</strong><div class="stack stack-a"></div></div>
            <div class="stack-container"><strong>B</strong><div class="stack stack-b"></div></div>
        `;

        this.#viewA = this.#container.querySelector('.stack-a');
        this.#viewB = this.#container.querySelector('.stack-b');
    }

    /**
     * Remplace les stacks actuelles et déclenche le rendu.
     */
    setStacks(stacks) {
        this.#stacks = stacks;
        this.#scheduleRender();
    }

    getStacks() {
        return this.#stacks;
    }

    /**
     * Applique un mouvement directement sur les stacks et met à jour la vue.
     */
    applyMove(move) {
        if (!this.#stacks) return;
        
        // Apply the move to the data model first
        this.#stacks.applyMove(move);
        
        // Apply DOM manipulation for the move
        this.#applyDOMMove(move);
    }

    #applyDOMMove(move) {
        console.log("applyDOMMove", move);
        const stackA = this.#stacks.getStackA();
        const stackB = this.#stacks.getStackB();
        
        // Calculate max value for width calculations
        let maxVal = 1;
        const allValues = [...stackA.iterator(), ...stackB.iterator()];
        if (allValues.length > 0) {
            maxVal = Math.max(...allValues.map(Math.abs));
        }

        switch (move) {
            case 'sa':
                this.#swapTopElements(this.#viewA, stackA, maxVal);
                break;
            case 'sb':
                this.#swapTopElements(this.#viewB, stackB, maxVal);
                break;
            case 'ss':
                this.#swapTopElements(this.#viewA, stackA, maxVal);
                this.#swapTopElements(this.#viewB, stackB, maxVal);
                break;
            case 'pa':
                this.#pushElement(this.#viewB, this.#viewA, stackB, stackA, maxVal);
                break;
            case 'pb':
                this.#pushElement(this.#viewA, this.#viewB, stackA, stackB, maxVal);
                break;
            case 'ra':
                this.#rotateStack(this.#viewA, stackA, maxVal);
                break;
            case 'rb':
                this.#rotateStack(this.#viewB, stackB, maxVal);
                break;
            case 'rr':
                this.#rotateStack(this.#viewA, stackA, maxVal);
                this.#rotateStack(this.#viewB, stackB, maxVal);
                break;
            case 'rra':
                this.#reverseRotateStack(this.#viewA, stackA, maxVal);
                break;
            case 'rrb':
                this.#reverseRotateStack(this.#viewB, stackB, maxVal);
                break;
            case 'rrr':
                this.#reverseRotateStack(this.#viewA, stackA, maxVal);
                this.#reverseRotateStack(this.#viewB, stackB, maxVal);
                break;
        }

        // Update visual feedback
        this.#container.classList.toggle('success-border', this.#stacks.isSorted());
    }

    #swapTopElements(viewElement, stack, maxVal) {
        if (stack.getSize() < 2) return;
        
        const elements = viewElement.children;
        if (elements.length < 2) return;
        
        // Swap the first two elements (top of stack)
        const first = elements[0];
        const second = elements[1];
        const temp = document.createElement('div');
        
        first.before(temp);
        second.before(first);
        temp.before(second);
        temp.remove();
    }

    #pushElement(fromView, toView, fromStack, toStack, maxVal) {
        if (fromStack.getSize() === 0) return;
        
        const fromElements = fromView.children;
        if (fromElements.length === 0) return;
        
        // Get the top element from source stack
        const topElement = fromElements[0];
        const value = fromStack.top();
        
        // Create new element for destination
        const newElement = document.createElement('div');
        newElement.className = 'element';
        const width = maxVal ? (Math.abs(value) / maxVal) * 100 : 0;
        newElement.innerHTML = `
            <span class="el-label">${value}</span>
            <div class="el-bar" style="width:${width}%; background:hsl(${200 + (width * 1.2)},70%,50%)"></div>
        `;
        
        // Remove from source and add to destination
        topElement.remove();
        toView.insertBefore(newElement, toView.firstChild);
    }

    #rotateStack(viewElement, stack, maxVal) {
        if (stack.getSize() < 2) return;
        
        const elements = viewElement.children;
        if (elements.length < 2) return;
        
        // Move the first element (top) to the end (bottom)
        const topElement = elements[0];
        topElement.remove();
        viewElement.appendChild(topElement);
    }

    #reverseRotateStack(viewElement, stack, maxVal) {
        if (stack.getSize() < 2) return;
        
        const elements = viewElement.children;
        if (elements.length < 2) return;
        
        // Move the last element (bottom) to the beginning (top)
        const bottomElement = elements[elements.length - 1];
        bottomElement.remove();
        viewElement.insertBefore(bottomElement, viewElement.firstChild);
    }

    #scheduleRender() {
        if (this.#renderScheduled) return;

        this.#renderScheduled = true;

        requestAnimationFrame(() => {
            this.#renderScheduled = false;
            this.#render();
        });
    }

    #render() {
        if (!this.#stacks) return;

        // On récupère les valeurs pour calculer les ratios de largeur
        const stackA = this.#stacks.getStackA();
        const stackB = this.#stacks.getStackB();
        
        // Calcul du max pour les barres (on pourrait l'optimiser en le passant en paramètre)
        let maxVal = 1;
        const allValues = [...stackA.iterator(), ...stackB.iterator()];
        if (allValues.length > 0) {
            maxVal = Math.max(...allValues.map(Math.abs));
        }

        this.#drawStack(stackA, this.#viewA, maxVal);
        this.#drawStack(stackB, this.#viewB, maxVal);

        // Feedback visuel du tri
        this.#container.classList.toggle('success-border', this.#stacks.isSorted());
    }

    #drawStack(stack, element, maxVal) {
        element.innerHTML = '';
        for (const val of stack.iterator()) {
            const el = document.createElement('div');
            el.className = 'element';
            const width = maxVal !== 0 ? (Math.abs(val) / maxVal) * 100 : 0;
            el.innerHTML = `
                <span class="el-label">${val}</span>
                <div class="el-bar" style="width:${width}%; background:hsl(${200 + (width * 1.2)},70%,50%)"></div>
            `;
            element.appendChild(el);
        }
    }
}