import { PushSwapSim } from './pushswapsim.js';

export class PushSwapApp {
    constructor() {
        this.sims = [];
        this.compareMode = localStorage.getItem('ps_compare') === 'true';
        this.lastDiff = { offA: -1, offB: -1, convA: -1, convB: -1 };

        this.initEventListeners();
        this.initApp();
    }

    initApp() {
        const wrapper = document.getElementById('main-wrapper');
        wrapper.innerHTML = '';
        this.sims = [];

        // Callback de notification pour les composants
        const refreshCB = () => this.refreshGlobalUI();
        
        this.sims.push(new PushSwapSim('main-wrapper', 'left', 'VERSION A', refreshCB));
        if (this.compareMode) {
            this.sims.push(new PushSwapSim('main-wrapper', 'right', 'VERSION B', refreshCB));
        }

        document.getElementById('sync-controls').style.display = this.compareMode ? 'flex' : 'none';
        
        const btn = document.getElementById('compareToggle');
        btn.innerText = `Compare: ${this.compareMode ? 'ON' : 'OFF'}`;
        btn.classList.toggle('active', this.compareMode);

        const savedStack = localStorage.getItem('ps_global_stack') || "";
        document.getElementById('globalInput').value = savedStack;
        
        this.applyToAll();
    }

    initEventListeners() {
        this.bindClick('#btn-random', () => this.generateRandom());
        this.bindClick('#btn-apply', () => this.applyToAll());
        this.bindClick('#compareToggle', () => this.toggleCompareMode());
        this.bindClick('#btn-sync-play', () => this.syncPlay());
        this.bindClick('#btn-sync-stop', () => this.syncStop());
        this.bindClick('#btn-sync-prev', () => this.syncStep(-1));
        this.bindClick('#btn-sync-next', () => this.syncStep(1));
        this.bindClick('#btn-find-main', () => {
            this.checkIfMatch() ? this.findNextDiff() : this.findNextConvergenceOnly();
        });
        this.bindClick('#btn-skip-diff', () => this.skipToConvergence());
        this.bindClick('#btn-merge-a-to-b', () => this.replaceMoveZone('A', 'B'));
        this.bindClick('#btn-merge-b-to-a', () => this.replaceMoveZone('B', 'A'));
    }

    bindClick(selector, fn) {
        const el = document.querySelector(selector);
        if (el) {
            el.onclick = null; // On nettoie l'ancien onclick s'il existe
            el.addEventListener('click', fn);
        }
    }

    // --- LOGIQUE CORE ---

    refreshGlobalUI() {
        this.updateSyncStatus();
        this.updateSyncToolbar();
    }

    toggleCompareMode() {
        this.compareMode = !this.compareMode;
        localStorage.setItem('ps_compare', this.compareMode);
        this.initApp();
    }

    generateRandom() {
        const size = parseInt(document.getElementById('randomSize').value) || 100;
        const arr = Array.from({length: size}, (_, i) => i + 1).sort(() => Math.random() - 0.5);
        document.getElementById('globalInput').value = arr.join(' ');
        this.applyToAll();
    }

    applyToAll() {
        const val = document.getElementById('globalInput').value;
        localStorage.setItem('ps_global_stack', val);
        this.sims.forEach(sim => { 
            sim.currentIndex = 0; 
            sim.setInitialState(val); 
        });
    }

    syncStep(dir) { this.sims.forEach(s => s.step(dir)); }

    syncStop() {
        this.sims.forEach(s => s.isPlaying = false);
        this.updateSyncToolbar();
    }

    async syncPlay() {
        this.sims.forEach(s => s.isPlaying = true);
        this.updateSyncToolbar();

        while (this.sims.some(s => s.currentIndex < s.movesList.length) && this.sims.every(s => s.isPlaying)) {
            let moved = false;
            this.sims.forEach(s => { 
                if (s.currentIndex < s.movesList.length) {
                    s.currentIndex++; 
                    s.render(true); 
                    s.saveLocal();
                    moved = true;
                }
            });
            if (!moved) break;
            await new Promise(r => setTimeout(r, parseInt(this.sims[0].speedInput.value)));
        }
        this.syncStop();
    }

    // --- COMPARAISON & DIFF ---

    checkIfMatch() {
        if (this.sims.length < 2) return true;
        return JSON.stringify(this.sims[0].a) === JSON.stringify(this.sims[1].a) && 
               JSON.stringify(this.sims[0].b) === JSON.stringify(this.sims[1].b);
    }

    updateSyncStatus() {
        const indicator = document.getElementById('stack-match-indicator');
        const btnFind = document.getElementById('btn-find-main');
        if (!indicator || this.sims.length < 2) return;
        
        const isMatch = this.checkIfMatch();
        indicator.innerText = isMatch ? "STACKS IDENTICAL" : "STACKS DIVERGENT";
        indicator.className = isMatch ? "match-true" : "match-false";
        btnFind.innerText = isMatch ? "FIND NEXT DIFF 🔍" : "FIND CONVERGENCE 🎯";
        if (isMatch) document.getElementById('merge-tools').style.display = 'none';
    }

    updateSyncToolbar() {
        const anyPlaying = this.sims.some(s => s.isPlaying);
        const anyEmpty = this.sims.some(s => s.movesList.length === 0);
        
        document.getElementById('btn-sync-play').disabled = anyPlaying || anyEmpty;
        document.getElementById('btn-sync-stop').disabled = !anyPlaying;
        document.getElementById('btn-find-main').disabled = anyPlaying || anyEmpty;
    }

    getStateAt(sim, index) {
        let a = [...sim.initialState.replace(/,/g, ' ').trim().split(/\s+/).filter(x => x !== "").map(Number)].reverse();
        let b = [];
        const limit = Math.min(index, sim.movesList.length);
        for (let i = 0; i < limit; i++) sim.executePhysics(sim.movesList[i], a, b);
        return JSON.stringify({a, b});
    }

    findNextDiff() {
        const [s1, s2] = this.sims;
        let offA = s1.currentIndex, offB = s2.currentIndex;
        let found = false;

        while (offA < s1.movesList.length || offB < s2.movesList.length) {
            if (s1.movesList[offA] !== s2.movesList[offB]) { found = true; break; }
            offA++; offB++;
        }

        if (found) {
            this.lastDiff.offA = offA; this.lastDiff.offB = offB;
            this.findConvergence(offA, offB);
        }
    }

    findNextConvergenceOnly() {
        this.findConvergence(this.sims[0].currentIndex, this.sims[1].currentIndex);
    }

    findConvergence(offA, offB) {
        const [s1, s2] = this.sims;
        let convA = -1, convB = -1;
        const range = 200;

        outer: for (let i = 0; i <= range; i++) {
            for (let j = 0; j <= range; j++) {
                if (i === 0 && j === 0) continue;
                if (this.getStateAt(s1, offA + i) === this.getStateAt(s2, offB + j)) {
                    convA = offA + i; convB = offB + j;
                    break outer;
                }
            }
        }

        this.lastDiff = { offA, offB, convA, convB };
        s1.currentIndex = offA; s2.currentIndex = offB;
        s1.render(true); s2.render(true);
        this.applyHighlight();
    }

    applyHighlight() {
        const { offA, offB, convA, convB } = this.lastDiff;
        const endA = convA !== -1 ? convA : this.sims[0].movesList.length;
        const endB = convB !== -1 ? convB : this.sims[1].movesList.length;
        
        const lenA = endA - offA, lenB = endB - offB;
        document.getElementById('diff-stats').innerHTML = `<span class="diff-badge">A: ${lenA}</span> <span class="diff-badge">B: ${lenB}</span>`;

        const apply = (sim, start, end, cls) => {
            const spans = sim.movesView.querySelectorAll('.move-item');
            for (let k = start; k < end; k++) if(spans[k]) spans[k].classList.add(cls);
        };

        const classA = lenA < lenB ? "move-diff-better" : (lenA > lenB ? "move-diff-worse" : "move-diff");
        const classB = lenB < lenA ? "move-diff-better" : (lenB > lenA ? "move-diff-worse" : "move-diff");

        apply(this.sims[0], offA, endA, classA);
        apply(this.sims[1], offB, endB, classB);
        document.getElementById('merge-tools').style.display = 'flex';
    }

    skipToConvergence() {
        if (this.lastDiff.convA === -1) return;
        this.sims[0].currentIndex = this.lastDiff.convA;
        this.sims[1].currentIndex = this.lastDiff.convB;
        this.sims.forEach(s => s.render(true));
        document.getElementById('merge-tools').style.display = 'none';
    }

    replaceMoveZone(fromLabel, toLabel) {
        const fromSim = fromLabel === 'A' ? this.sims[0] : this.sims[1];
        const toSim = toLabel === 'A' ? this.sims[0] : this.sims[1];
        const { offA, offB, convA, convB } = this.lastDiff;

        const startFrom = fromLabel === 'A' ? offA : offB;
        const endFrom = fromLabel === 'A' ? convA : convB;
        const startTo = toLabel === 'A' ? offA : offB;
        const endTo = toLabel === 'A' ? convA : convB;

        if (endFrom === -1 || endTo === -1) return;

        const newMoves = fromSim.movesList.slice(startFrom, endFrom);
        toSim.movesList.splice(startTo, endTo - startTo, ...newMoves);
        
        toSim.render(true);
        toSim.saveLocal();
        this.refreshGlobalUI();
    }
}