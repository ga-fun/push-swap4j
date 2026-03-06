import { PushSwapSim } from './pushswapsim.js';
import { ConvergenceFinder } from './convergenceFinder.js';

export class PushSwapApp {
    #sims = [];
    #compareMode;
    #lastDiff = { offA: -1, offB: -1, convA: -1, convB: -1 };
    
    constructor() {
        this.#compareMode = localStorage.getItem('ps_compare') === 'true';

        this.#initEventListeners();
        this.#initApp();
    }

    #initApp() {
        const wrapper = document.getElementById('main-wrapper');
        wrapper.innerHTML = '';

        // Callback de notification pour les composants
        const refreshCB = () => this.#refreshGlobalUI();
        
        this.#sims.push(new PushSwapSim('main-wrapper', 'left', 'VERSION 1', refreshCB));
        if (this.#compareMode) {
            this.#sims.push(new PushSwapSim('main-wrapper', 'right', 'VERSION 2', refreshCB));
        }

        document.getElementById('sync-controls').style.display = this.#compareMode ? 'flex' : 'none';
        
        const btn = document.getElementById('compareToggle');
        btn.innerText = `Compare: ${this.#compareMode ? 'ON' : 'OFF'}`;
        btn.classList.toggle('active', this.#compareMode);

        const savedStack = localStorage.getItem('ps_global_stack') || "";
        document.getElementById('globalInput').value = savedStack;
        
        this.#applyToAll();
    }

    #initEventListeners() {
        this.#bindClick('#btn-random', () => this.#generateRandom());
        this.#bindClick('#btn-apply', () => this.#applyToAll());
        this.#bindClick('#compareToggle', () => this.#toggleCompareMode());
        this.#bindClick('#btn-sync-play', () => this.#syncPlay());
        this.#bindClick('#btn-sync-stop', () => this.#syncStop());
        this.#bindClick('#btn-sync-prev', () => this.#syncStep(-1));
        this.#bindClick('#btn-sync-next', () => this.#syncStep(1));
        this.#bindClick('#btn-find-main', () => {
            this.#checkIfMatch() ? this.#findNextDiff() : this.#findNextConvergenceOnly();
        });
        this.#bindClick('#btn-skip-diff', () => this.#skipToConvergence());
        this.#bindClick('#btn-merge-a-to-b', () => this.#replaceMoveZone('A', 'B'));
        this.#bindClick('#btn-merge-b-to-a', () => this.#replaceMoveZone('B', 'A'));
    }

    #bindClick(selector, fn) {
        const el = document.querySelector(selector);
        if (el) {
            el.onclick = null; // On nettoie l'ancien onclick s'il existe
            el.addEventListener('click', fn);
        }
    }

    #animateButtonFeedback(message, duration = 1500) {
        const btn = document.getElementById('btn-find-main');
        btn.classList.add('btn-no-diff');
        
        // Créer ou mettre à jour une balise style pour le message personnalisé
        let styleEl = document.getElementById('dynamic-btn-style');
        if (!styleEl) {
            styleEl = document.createElement('style');
            styleEl.id = 'dynamic-btn-style';
            document.head.appendChild(styleEl);
        }
        styleEl.textContent = `.btn-no-diff::after { content: '${message}' !important; }`;
        
        // Restaurer le bouton après l'animation
        setTimeout(() => {
            btn.classList.remove('btn-no-diff');
        }, duration);
    }

    // --- LOGIQUE CORE ---

    #refreshGlobalUI() {
        this.#updateSyncStatus();
        this.#updateSyncToolbar();
    }

    #toggleCompareMode() {
        this.#compareMode = !this.#compareMode;
        localStorage.setItem('ps_compare', this.#compareMode);
        this.#initApp();
    }

    #generateRandom() {
        const size = Number.parseInt(document.getElementById('randomSize').value) || 100;
        const arr = Array.from({length: size}, (_, i) => i).sort(() => Math.random() - 0.5);
        document.getElementById('globalInput').value = arr.join(' ');
        this.#applyToAll();
    }

    #applyToAll() {
        const val = document.getElementById('globalInput').value;
        localStorage.setItem('ps_global_stack', val);
        const numbers = val.replace(/,/g, ' ').trim().split(/\s+/).filter(x => x !== "").map(Number);
        document.getElementById('randomSize').value = numbers.length;
        this.#sims.forEach(sim => { 
            sim.setInitialState(numbers); 
            sim.setIndex(-1); 
        });
    }

    #syncStep(dir) { this.#sims.forEach(s => s.step(dir)); }

    async #syncPlay() {
        const speed = () => this.#sims[0].getFastForwardSpeed();
        this.#sims.forEach(s => s.fastForward(speed));
        this.#updateSyncToolbar();
    }
    
    #syncStop() {
        this.#sims.forEach(s => s.stopFastForward());
        this.#updateSyncToolbar();
    }

    // --- COMPARAISON & DIFF ---

    #checkIfMatch() {
        if (this.#sims.length < 2) return true;
        return this.#sims[0].getStacks(false).equals(this.#sims[1].getStacks(false));
    }

    #updateSyncStatus() {
        const indicator = document.getElementById('stack-match-indicator');
        const btnFind = document.getElementById('btn-find-main');
        const isMatch = this.#checkIfMatch();
        indicator.innerText = isMatch ? "STACKS IDENTICAL" : "STACKS DIVERGENT";
        indicator.className = isMatch ? "match-true" : "match-false";
        btnFind.innerText = isMatch ? "FIND NEXT DIFF 🔍" : "FIND CONVERGENCE 🎯";
        if (isMatch) document.getElementById('merge-tools').style.display = 'none';
    }

    #updateSyncToolbar() {
        const allAtEnd = this.#sims.every(s => s.getIndex() >= s.getMoveListSize() - 1);
        const allAtStart = this.#sims.every(s => s.getIndex() < 0);
        const anyPlaying = this.#sims.some(s => s.isPlaying());
        const anyEmpty = this.#sims.some(s => s.getMoveListSize() === 0);
        
        document.getElementById('btn-sync-play').disabled = anyPlaying || anyEmpty || allAtEnd;
        document.getElementById('btn-sync-stop').disabled = !anyPlaying;
        document.getElementById('btn-find-main').disabled = anyPlaying || anyEmpty;
        document.getElementById('btn-sync-next').disabled = anyPlaying || allAtEnd;
        document.getElementById('btn-sync-prev').disabled = anyPlaying || allAtStart;
    }

    #findNextDiff() {
        const [s1, s2] = this.#sims;
        let offA = s1.getIndex()+1, offB = s2.getIndex()+1;
        let found = false;

        while (offA < s1.getMoveListSize() || offB < s2.getMoveListSize()) {
            if (s1.getMoveAt(offA) !== s2.getMoveAt(offB)) { found = true; break; }
            offA++; offB++;
        }

        if (found) {
            this.#lastDiff.offA = offA; this.#lastDiff.offB = offB;
            this.#findConvergence(offA, offB);
        } else {
            this.#animateButtonFeedback('No differences found!');
        }
    }

    #findNextConvergenceOnly() {
        this.#findConvergence(this.#sims[0].getIndex(), this.#sims[1].getIndex());
    }

    #getConvergenceMoveList(sim) {
        return {
            getSize: () => sim.getMoveListSize(),
            get: (index) => sim.getMoveAt(index)
        }
    }

    #findConvergence(offA, offB) {
        const [s1, s2] = this.#sims;
        const finder = new ConvergenceFinder(s1.getInitialState());
        const result = finder.findConvergence(this.#getConvergenceMoveList(s1), this.#getConvergenceMoveList(s2), offA, offB);
        let convA = result?.convA, convB = result?.convB;
        this.#lastDiff = { offA, offB, convA, convB };
        s1.setIndex(offA); s2.setIndex(offB);
        this.#applyHighlight();
    }

    #applyHighlight() {
        const { offA, offB, convA, convB } = this.#lastDiff;
        
        if (!convA || !convB) {
            this.#animateButtonFeedback('No convergence found!');
        } else {
            const lenA = convA - offA, lenB = convB - offB;
            document.getElementById('diff-stats').innerHTML = `<span class="diff-badge">1: ${lenA}</span> <span class="diff-badge">2: ${lenB}</span>`;

            // Appliquer les nouvelles sélections avec les bons types
            this.#sims[0].setMovesSelection(offA, convA, this.#getHightLightClass(lenA, lenB));
            this.#sims[1].setMovesSelection(offB, convB, this.#getHightLightClass(lenB, lenA));
            document.getElementById('merge-tools').style.display = 'flex';
        }
    }

    #getHightLightClass(myLen, otherLen) {
        if (myLen < otherLen) {
            return 'better';
        } else if (myLen > otherLen) {
            return 'worse';
        } else {
            return 'neutral';
        }
    }

    #skipToConvergence() {
        if (this.#lastDiff.convA === -1) return;
        this.#sims[0].setIndex(this.#lastDiff.convA);
        this.#sims[1].setIndex(this.#lastDiff.convB);
        this.#sims[0].setMovesSelection(this.#lastDiff.offA, this.#lastDiff.convA, null);
        this.#sims[1].setMovesSelection(this.#lastDiff.offB, this.#lastDiff.convB, null);
        document.getElementById('merge-tools').style.display = 'none';
    }

    #replaceMoveZone(fromLabel, toLabel) {
        const fromSim = fromLabel === 'A' ? this.#sims[0] : this.#sims[1];
        const toSim = toLabel === 'A' ? this.#sims[0] : this.#sims[1];
        const { offA, offB, convA, convB } = this.#lastDiff;

        const startFrom = fromLabel === 'A' ? offA : offB;
        const endFrom = fromLabel === 'A' ? convA : convB;
        const startTo = toLabel === 'A' ? offA : offB;
        const endTo = toLabel === 'A' ? convA : convB;

        if (endFrom === -1 || endTo === -1) return;

        const newMoves = fromSim.getMovesList().slice(startFrom, endFrom);
        const updatedMoves = toSim.getMovesList(); 
        updatedMoves.splice(startTo, endTo - startTo, ...newMoves);
        toSim.setMovesList(updatedMoves);
        this.#refreshGlobalUI();
    }
}