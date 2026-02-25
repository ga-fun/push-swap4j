export class PushSwapSim {
    constructor(containerId, id, title = "Sim", onStateChange = null) {
        this.id = id;
        this.onStateChange = onStateChange;
        this.a = []; this.b = [];
        this.movesList = [];
        this.currentIndex = 0;
        this.initialState = "";
        this.isPlaying = false;

        this.container = document.createElement('div');
        this.container.className = 'ps-component';
        this.container.innerHTML = `
            <div class="ps-sidebar">
                <div style="font-weight: bold; font-size: 11px; color: #888; display:flex; justify-content:space-between; align-items:center;">
                    <span>${title}</span>
                    <div class="toolbar-moves">
                        <button class="icon-btn btn-copy" title="Copy">📋</button>
                        <button class="icon-btn btn-clear" title="Clear">🗑️</button>
                    </div>
                </div>
                <div class="moves-display" contenteditable="true"></div>
                <div style="font-size: 10px; display: flex; justify-content: space-between;">
                    <span class="count-label">0 moves</span>
                    <span style="color:#40c4ff">Idx: <span class="idx-label">0</span></span>
                </div>
                <div class="edit-mode-container">
                    <button class="btn-action btn-mode-edit active" data-mode="truncate">TRUNC</button>
                    <button class="btn-action btn-mode-edit" data-mode="insert">INS</button>
                    <button class="btn-action btn-mode-edit" data-mode="overwrite">OVER</button>
                </div>
                <div class="controls-grid">
                    ${['sa','sb','ss','pa','pb','','ra','rb','rr','rra','rrb','rrr'].map(op => 
                        op ? `<button class="btn-action" data-op="${op}" ${op.startsWith('p')?'style="background:#1b5e20"':''}>${op}</button>` : `<span></span>`
                    ).join('')}
                </div>
                <button class="btn-action btn-delete btn-stop-style" disabled>DELETE CURRENT ⌫</button>
                <div style="display: flex; gap: 3px;">
                    <button class="btn-action btn-prev btn-blue-style" style="flex:1">PREV</button>
                    <button class="btn-action btn-next btn-blue-style" style="flex:1">NEXT</button>
                </div>
                <button class="btn-action btn-play btn-play-style">PLAY ▶️</button>
                <button class="btn-action btn-stop btn-stop-style" disabled>STOP ⏹️</button>
                <input type="range" class="speed-range" min="1" max="1000" value="100" dir="rtl">
            </div>
            <div class="ps-visualizer">
                <div class="stack-container"><strong>A</strong><div class="stack stack-a"></div></div>
                <div class="stack-container"><strong>B</strong><div class="stack stack-b"></div></div>
            </div>
        `;
        document.getElementById(containerId).appendChild(this.container);

        this.viewA = this.container.querySelector('.stack-a');
        this.viewB = this.container.querySelector('.stack-b');
        this.movesView = this.container.querySelector('.moves-display');
        this.speedInput = this.container.querySelector('.speed-range');
        
        this.initEvents();
        this.loadLocal();
    }

    initEvents() {
        this.container.querySelectorAll('.controls-grid button').forEach(btn => {
            btn.onclick = () => this.addMove(btn.dataset.op);
        });
        this.container.querySelector('.btn-next').onclick = () => this.step(1);
        this.container.querySelector('.btn-prev').onclick = () => this.step(-1);
        this.container.querySelector('.btn-play').onclick = () => this.runAuto();
        this.container.querySelector('.btn-stop').onclick = () => this.isPlaying = false;
        this.container.querySelector('.btn-copy').onclick = () => this.copyMoves();
        this.container.querySelector('.btn-clear').onclick = () => this.clearMoves();
        this.movesView.oninput = () => { this.syncMoves(); this.saveLocal(); };

        // Gestion des modes (Nettoyé : une seule fois suffit)
        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.onclick = () => {
                this.editMode = btn.dataset.mode;
                this.saveLocal();
                this.render(); // On laisse render mettre à jour la classe 'active'
            };
        });

        const delBtn = this.container.querySelector('.btn-delete');
        delBtn.onclick = () => {
            if (this.currentIndex > 0) {
                this.movesList.splice(this.currentIndex - 1, 1);
                this.currentIndex--; 
                this.render(true);
                this.saveLocal();
            }
        };
    }

    // --- LOGIQUE DE CALCUL TOOLTIP ---
    getSnapshotValue(index) {
        const nums = this.initialState.replace(/,/g, ' ').trim().split(/\s+/).filter(x => x !== "").map(Number);
        let sA = [...nums].reverse(); let sB = [];
        for (let i = 0; i < index; i++) {
            this.executePhysics(this.movesList[i], sA, sB);
        }
        let move = this.movesList[index].toLowerCase();
        if (move === 'pa') return sB.length > 0 ? sB[sB.length - 1] : "B empty";
        if (move === 'pb') return sA.length > 0 ? sA[sA.length - 1] : "A empty";
        return null;
    }

    getBestCost(val, srcStack, targetStack, targetIsA) {
        if (targetStack.length === 0) return { total: 0, label: "Direct push" };
        const srcIdx = srcStack.indexOf(val);
        const r_src = srcStack.length - 1 - srcIdx;
        const rr_src = (srcIdx + 1) % srcStack.length;

        const sTarget = [...targetStack].reverse();
        let tIdx = 0;
        let min = Math.min(...sTarget), max = Math.max(...sTarget);
        if (targetIsA) {
            if (val < min || val > max) tIdx = sTarget.indexOf(min);
            else { for (let i = 0; i < sTarget.length; i++) {
                if (val > sTarget[i] && val < sTarget[(i+1)%sTarget.length]) { tIdx = (i+1)%sTarget.length; break; }
            } }
        } else {
            if (val < min || val > max) tIdx = sTarget.indexOf(max);
            else { for (let i = 0; i < sTarget.length; i++) {
                if (val < sTarget[i] && val > sTarget[(i+1)%sTarget.length]) { tIdx = (i+1)%sTarget.length; break; }
            } }
        }
        const r_target = tIdx;
        const rr_target = (sTarget.length - tIdx) % sTarget.length;
        const ra = targetIsA ? r_target : r_src;
        const rra = targetIsA ? rr_target : rr_src;
        const rb = targetIsA ? r_src : r_target;
        const rrb = targetIsA ? rr_src : rr_target;

        const strategy = [
            { total: Math.max(ra, rb), label: `RR (ra:${ra}, rb:${rb})` },
            { total: Math.max(rra, rrb), label: `RRR (rra:${rra}, rrb:${rrb})` },
            { total: ra + rrb, label: `Mix (ra:${ra}, rrb:${rrb})` },
            { total: rra + rb, label: `Mix (rra:${rra}, rb:${rb})` }
        ];
        return strategy.reduce((prev, curr) => (curr.total < prev.total) ? curr : prev);
    }

    // --- SYSTEME ---
    saveLocal() {
        localStorage.setItem(`ps_moves_${this.id}`, JSON.stringify(this.movesList)); 
        localStorage.setItem(`ps_idx_${this.id}`, this.currentIndex); 
        localStorage.setItem(`ps_edit_mode_${this.id}`, this.editMode); // Ajout
    }

    loadLocal() {
        const savedMoves = localStorage.getItem(`ps_moves_${this.id}`);
        const savedIdx = localStorage.getItem(`ps_idx_${this.id}`);
        const savedMode = localStorage.getItem(`ps_edit_mode_${this.id}`); // Ajout
        
        if (savedMoves) this.movesList = JSON.parse(savedMoves);
        if (savedIdx) this.currentIndex = parseInt(savedIdx);
        if (savedMode) this.editMode = savedMode;
        else this.editMode = 'truncate';
    }
    copyMoves() { navigator.clipboard.writeText(this.movesList.join(' ')); }
    clearMoves() { this.movesList = []; this.currentIndex = 0; this.render(true); this.saveLocal(); }
    setInitialState(str) { this.initialState = str; this.render(); }

    addMove(op) {
        if(this.isPlaying) return;
        
        if (this.editMode === 'truncate') {
            // Mode actuel : Coupe tout ce qui suit
            this.movesList = this.movesList.slice(0, this.currentIndex);
            this.movesList.push(op);
            this.currentIndex++;
        } 
        else if (this.editMode === 'insert') {
            // Mode Insertion : Glisse l'instruction à l'index actuel
            this.movesList.splice(this.currentIndex, 0, op);
            this.currentIndex++;
        } 
        else if (this.editMode === 'overwrite') {
            // Mode Overwrite : Remplace l'instruction suivante (ou ajoute si fin)
            if (this.currentIndex < this.movesList.length) {
                this.movesList[this.currentIndex] = op;
            } else {
                this.movesList.push(op);
            }
            this.currentIndex++;
        }

        this.render(true);
        this.saveLocal();
    }

    syncMoves() {
        const text = this.movesView.innerText.replace(/,/g, ' ');
        this.movesList = text.trim().split(/\s+/).filter(x => x !== "");
        if (this.currentIndex > this.movesList.length) this.currentIndex = this.movesList.length;
        this.render(false);
    }

    step(dir) {
        let newIdx = this.currentIndex + dir;
        if (newIdx >= 0 && newIdx <= this.movesList.length) {
            this.currentIndex = newIdx;
            this.render(true);
            this.saveLocal();
        }
    }

    executePhysics(type, stackA, stackB) {
        if (!type) return;
        type = type.toLowerCase();
        if (type === 'sa' && stackA.length > 1) [stackA[stackA.length-1], stackA[stackA.length-2]] = [stackA[stackA.length-2], stackA[stackA.length-1]];
        else if (type === 'sb' && stackB.length > 1) [stackB[stackB.length-1], stackB[stackB.length-2]] = [stackB[stackB.length-2], stackB[stackB.length-1]];
        else if (type === 'pa' && stackB.length > 0) stackA.push(stackB.pop());
        else if (type === 'pb' && stackA.length > 0) stackB.push(stackA.pop());
        else if (type === 'ra' && stackA.length > 1) stackA.unshift(stackA.pop());
        else if (type === 'rb' && stackB.length > 1) stackB.unshift(stackB.pop());
        else if (type === 'rra' && stackA.length > 1) stackA.push(stackA.shift());
        else if (type === 'rrb' && stackB.length > 1) stackB.push(stackB.shift());
        else if (type === 'rr') { this.executePhysics('ra', stackA, stackB); this.executePhysics('rb', stackA, stackB); }
        else if (type === 'rrr') { this.executePhysics('rra', stackA, stackB); this.executePhysics('rrb', stackA, stackB); }
        else if (type === 'ss') { this.executePhysics('sa', stackA, stackB); this.executePhysics('sb', stackA, stackB); }
    }

    render(forceRedrawList = false) {
        const nums = this.initialState.replace(/,/g, ' ').trim().split(/\s+/).filter(x => x !== "").map(Number);
        const maxVal = nums.length > 0 ? Math.max(...nums.map(Math.abs)) : 1;
        this.a = [...nums].reverse(); this.b = [];
        for (let i = 0; i < this.currentIndex; i++) this.executePhysics(this.movesList[i], this.a, this.b);
        this.drawStack(this.a, this.viewA, true, maxVal);
        this.drawStack(this.b, this.viewB, false, maxVal);
        const isSorted = this.a.every((val, i) => i === 0 || val < this.a[i-1]);
        this.container.querySelector('.ps-visualizer').classList.toggle('success-border', (this.b.length === 0 && isSorted && this.a.length > 0));

        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.mode === this.editMode);
        });

        // --- LOGIQUE DE GRISAGE ---
        const noMoves = this.movesList.length === 0;
        const noStacks = this.a.length === 0 && this.b.length === 0;
        const isAtEnd = this.currentIndex === this.movesList.length;
        const isAtStart = this.currentIndex === 0;
        const blockAll = this.isPlaying || noStacks;

        this.container.querySelector('.btn-play').disabled = blockAll || noMoves || isAtEnd;
        this.container.querySelector('.btn-next').disabled = blockAll || noMoves || isAtEnd;
        this.container.querySelector('.btn-prev').disabled = blockAll || noMoves || isAtStart;
        this.container.querySelector('.btn-stop').disabled = !this.isPlaying; // Ajouté pour la cohérence

        this.container.querySelectorAll('.controls-grid button').forEach(btn => {
            const op = btn.dataset.op;
            if (!op) return; // Ignore les spans vides

            let isDisabled = blockAll;

            // Logique métier : on grise si l'action est impossible
            if (!isDisabled) {
                if (op === 'sa' || op === 'ra' || op === 'rra') isDisabled = this.a.length < 2;
                else if (op === 'sb' || op === 'rb' || op === 'rrb') isDisabled = this.b.length < 2;
                else if (op === 'pa') isDisabled = this.b.length === 0;
                else if (op === 'pb') isDisabled = this.a.length === 0;
                else if (op === 'ss' || op === 'rr' || op === 'rrr') {
                    isDisabled = (this.a.length < 2 && this.b.length < 2);
                }
            }

            btn.disabled = isDisabled;
        });

        this.container.querySelector('.btn-delete').disabled = blockAll || isAtStart;
        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => btn.disabled = blockAll);

        this.updateSidebar(forceRedrawList);
        
        if (this.onStateChange) this.onStateChange();
    }

    drawStack(stack, element, isA, maxVal) {
        element.innerHTML = '';
        // Inversion manuelle pour le rendu : le haut de la pile (fin de l'array) est affiché en premier
        [...stack].reverse().forEach(val => {
            const el = document.createElement('div'); el.className = 'element';
            const width = maxVal !== 0 ? (Math.abs(val) / maxVal) * 100 : 0;
            el.innerHTML = `<span class="el-label">${val}</span><div class="el-bar" style="width:${width}%; background:hsl(${200+(width*1.2)},70%,50%)"></div>`;
            element.appendChild(el);
        });
    }

    updateSidebar(force) {
        this.container.querySelector('.idx-label').innerText = this.currentIndex;
        this.container.querySelector('.count-label').innerText = `${this.movesList.length} moves`;
        if (document.activeElement !== this.movesView || force) {
            this.movesView.innerHTML = '';
            this.movesList.forEach((m, i) => {
                const span = document.createElement('span');
                span.className = "move-item " + (i === this.currentIndex-1 ? "move-current" : (i < this.currentIndex ? "move-past" : ""));
                span.innerText = m;
                span.onclick = () => { this.currentIndex = i+1; this.render(true); this.saveLocal(); };
                
                // Tooltip logic
                span.onmouseenter = () => {
                    if (this.isPlaying) return;
                    const mLower = m.toLowerCase();
                    if (mLower === 'pa' || mLower === 'pb') {
                        const val = this.getSnapshotValue(i);
                        // Re-calculer l'état à l'instant T pour l'optimum
                        let sA = [], sB = []; 
                        const nums = this.initialState.replace(/,/g, ' ').trim().split(/\s+/).filter(x => x !== "").map(Number);
                        sA = [...nums].reverse();
                        for (let j = 0; j < i; j++) this.executePhysics(this.movesList[j], sA, sB);
                        const opt = this.getBestCost(val, mLower==='pa'?sB:sA, mLower==='pa'?sA:sB, mLower==='pa');
                        span.title = `Pushed value: ${val}\nTheoretical optimum: ${opt.label} (Total: ${opt.total})`;
                    }
                };
                this.movesView.appendChild(span);
            });
            const current = this.movesView.querySelector('.move-current');
            if (current) current.scrollIntoView({ behavior: 'auto', block: 'nearest' });
        }
    }

    async runAuto() {
        this.isPlaying = true;
        this.render(); // Désactive les boutons via l'état isPlaying
        
        while (this.currentIndex < this.movesList.length && this.isPlaying) {
            this.currentIndex++; 
            this.render(true); 
            this.saveLocal();
            await new Promise(r => setTimeout(r, Number.parseInt(this.speedInput.value)));
        }
        
        this.isPlaying = false;
        this.render();
    }
}