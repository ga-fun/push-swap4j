import { TwoStacks } from './stack.js';

export class ConvergenceFinder {
    #numbers;
    
    constructor(numbers) {
        this.#numbers = numbers;
    }

    #getFirstDivergentState(moveList, offset) {
        let state = new TwoStacks(this.#numbers, []);
        for (let i = 0; i <= offset; i++) {
            state.applyMove(moveList.get(i));
        }
        return state;
    }

    #testConvergenceForPosition(targetState, bState, moves, offB, range) {
        if (targetState.equals(bState)) return offB;
        const end = Math.min(offB + range, moves.getSize()-1);
        for (let j = offB+1; j <= end; j++) {
            bState.applyMove(moves.get(j));
            if (targetState.equals(bState)) {
                return j;
            }
        }
        return null;
    }

    findConvergence(moveListA, moveListB, offA, offB) {
        const range = 200;

        const aState = this.#getFirstDivergentState(moveListA, offA);
        let bState = this.#getFirstDivergentState(moveListB, offB);

        const startStackAofB = bState.getStackA().toArray();
        const startStackBofB = bState.getStackB().toArray();

        const end = Math.min(offA + range, moveListA.getSize()-1);
        for (let i = offA; i <= end; i++) {
            if (i>offA) {
                aState.applyMove(moveListA.get(i));
            }
            bState = new TwoStacks(startStackAofB, startStackBofB);
            let convB = this.#testConvergenceForPosition(aState, bState, moveListB, offB, range);
            if (convB !== null) {
                let convA = i;
                return { convA, convB };
            }
        }

        return null;
    }
}
