export const Move = Object.freeze({
    SA: 'sa', SB: 'sb', SS: 'ss',
    PA: 'pa', PB: 'pb',
    RA: 'ra', RB: 'rb', RR: 'rr',
    RRA: 'rra', RRB: 'rrb', RRR: 'rrr'
});

export class Stack {
    #data;

    constructor(numbers = []) {
        // Entrée : [1, 2, 3] (1 est le top)
        // Interne : [3, 2, 1] (1 est à l'index length-1)
        this.#data = [...numbers].reverse();
    }

    /**
     * Ajoute une valeur au sommet de la pile (O(1)).
     */
    push(value) {
        this.#data.push(value);
    }

    /**
     * Retire et renvoie la valeur au sommet de la pile (O(1)).
     * Renvoie undefined si la pile est vide.
     */
    pop() {
        return this.#data.pop();
    }

    /**
     * Renvoie la valeur au sommet de la pile sans la retirer (O(1)).
     * Renvoie undefined si la pile est vide.
     */
    top() {
        return this.#data.at(-1);
    }

    /**
     * Décale tous les éléments vers le haut d'une position.
     * Le premier élément devient le dernier (ra / rb).
     */
    rotate() {
        if (this.#data.length > 1) {
            // Le top (fin du tableau) est retiré et inséré au fond (début du tableau)
            this.#data.unshift(this.#data.pop());
        }
    }

    /**
     * Décale tous les éléments vers le bas d'une position.
     * Le dernier élément devient le premier (rra / rrb).
     */
    reverseRotate() {
        if (this.#data.length > 1) {
            // Le fond (début du tableau) est retiré et inséré au top (fin du tableau)
            this.#data.push(this.#data.shift());
        }
    }

    /**
     * Échange les deux premiers éléments de la pile (sa / sb).
     */
    swap() {
        if (this.#data.length > 1) {
            const top = this.#data.length - 1;
            [this.#data[top], this.#data[top - 1]] = [this.#data[top - 1], this.#data[top]];
        }
    }

    /**
     * Itérateur du haut vers le bas.
     */
    *iterator() {
        for (let i = this.#data.length - 1; i >= 0; i--) {
            yield this.#data[i];
        }
    }

    equals(otherStack) {
        if (!(otherStack instanceof Stack)) return false;
        const otherData = otherStack.#data; 
        if (this.#data.length !== otherData.length) return false;
        return this.#data.every((val, index) => val === otherData[index]);
    }

    // Utile pour le debug ou l'export
    getSize() {
        return this.#data.length;
    }

    isStrictlySorted() {
        return this.#data.every((val, i) => i === 0 || val < this.#data[i-1]);
    }
}

export class TwoStacks {
    #a;
    #b;

    constructor(numbersA = [], numbersB = []) {
        // On stocke en interne avec Top à la fin
        this.#a = new Stack(numbersA);
        this.#b = new Stack(numbersB);
    }

    getStackA() {
        // On "re-reverse" pour le constructeur de Stack qui attend le Top en premier
        return this.#a;
    }

    getStackB() {
        return this.#b;
    }

    applyMove(move) {
        switch (move) {
            case Move.SA:  this.#a.swap(); break;
            case Move.SB:  this.#b.swap(); break;
            case Move.SS:  this.#a.swap(); this.#b.swap(); break;
            case Move.PA:  this.#push(this.#b, this.#a); break;
            case Move.PB:  this.#push(this.#a, this.#b); break;
            case Move.RA:  this.#a.rotate(); break;
            case Move.RB:  this.#b.rotate(); break;
            case Move.RR:  this.#a.rotate(); this.#b.rotate(); break;
            case Move.RRA: this.#a.reverseRotate(); break;
            case Move.RRB: this.#b.reverseRotate(); break;
            case Move.RRR: this.#a.reverseRotate(); this.#b.reverseRotate(); break;
        }
    }

    undoMove(move) {
        switch (move) {
            case Move.PA: this.#push(this.#a, this.#b); break; // undo PA (B->A) = PB (A->B)
            case Move.PB: this.#push(this.#b, this.#a); break; // undo PB (A->B) = PA (B->A)
            case Move.RA: this.#a.reverseRotate(); break; // undo RA = RRA
            case Move.RB: this.#b.reverseRotate(); break; // undo RB = RRB
            case Move.RR:  this.#a.reverseRotate(); this.#b.reverseRotate(); break;
            case Move.RRA: this.#a.rotate(); break;       // undo RRA = RA
            case Move.RRB: this.#b.rotate(); break;       // undo RRB = RB
            case Move.RRR: this.#a.rotate(); this.#b.rotate(); break;
            // sa, sb, ss sont leurs propres inverses
            case Move.SA:  this.#a.swap(); break;
            case Move.SB:  this.#b.swap(); break;
            case Move.SS:  this.#a.swap(); this.#b.swap(); break;
        }
    }

    #push(from, to) {
        if (from.getSize() > 0) to.push(from.pop());
    }

    isEmpty() {
        return this.#a.getSize() === 0 && this.#b.getSize() === 0;
    }

    isSorted() {
        return this.#a.isStrictlySorted() && this.#b.getSize() === 0;
    }

    equals(otherStacks) {
        if (!(otherStacks instanceof TwoStacks)) return false;
        return this.#a.equals(otherStacks.#a) && this.#b.equals(otherStacks.#b);
    }
}
