import { TwoStacks, Move } from './stack.js';


export class PushSwapSim {
    static #VALID_MOVES = new Set(Object.values(Move));
    
    #currentIndex = 0;
    #isPlaying = false;
    #movesList = [];
    #numbers = [];
    #stacks = new TwoStacks([],[]);

    constructor(containerId, id, title = "Sim", onStateChange = null) {
        this.id = id;
        this.onStateChange = onStateChange;

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
                    ${[Move.SA, Move.SB, Move.SS, Move.PA, Move.PB, '', Move.RA, Move.RB, Move.RR, Move.RRA, Move.RRB, Move.RRR].map(op => 
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
        
        this.#initEvents();
        this.#loadLocal();
    }

    #initEvents() {
        this.container.querySelectorAll('.controls-grid button').forEach(btn => {
            btn.onclick = () => this.#addMove(btn.dataset.op);
        });
        this.container.querySelector('.btn-next').onclick = () => this.step(1);
        this.container.querySelector('.btn-prev').onclick = () => this.step(-1);
        this.container.querySelector('.btn-play').onclick = () => this.#runAuto();
        this.container.querySelector('.btn-stop').onclick = () => this.#isPlaying = false;
        this.container.querySelector('.btn-copy').onclick = () => this.#copyMoves();
        this.container.querySelector('.btn-clear').onclick = () => this.#clearMoves();
        this.movesView.oninput = () => { this.#syncMoves(); this.#saveLocal(); };

        // Gestion des modes (Nettoyé : une seule fois suffit)
        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.onclick = () => {
                this.editMode = btn.dataset.mode;
                this.#saveLocal();
                this.#render();
            };
        });

        const delBtn = this.container.querySelector('.btn-delete');
        delBtn.onclick = () => {
            if (this.#currentIndex > 0) {
                this.#movesList.splice(this.#currentIndex - 1, 1);
                this.setIndex(this.#currentIndex - 1);
            }
        };
    }

    getIndex() { return this.#currentIndex; }

    setIndex(idx) {
        this.#currentIndex = Math.max(0, Math.min(idx, this.#movesList.length));
        this.#saveLocal();
        this.#render(true);
    }

    getMovesList() { return [...this.#movesList]; }

    getMoveListSize() { return this.#movesList.length; }

    getMoveAt(index) { return this.#movesList[index].toLowerCase(); }

    setMovesList(newList) {
        // 1. Validation du type
        if (!Array.isArray(newList)) {
            throw new TypeError(`[PushSwapSim] Expected an Array for movesList, got: ${typeof newList}`);
        }

        // 2. Validation du contenu
        const invalidOp = newList.find(op => !PushSwapSim.#VALID_MOVES.has(op.toLowerCase()));        
        if (invalidOp) {
            throw new Error(`[PushSwapSim] Invalid move detected: "${invalidOp}". Instruction rejected.`);
        }

        this.#movesList = [...newList]; 
        
        // Sécurité : si la nouvelle liste est plus courte que l'index actuel, on recule
        if (this.#currentIndex > this.#movesList.length) {
            this.#currentIndex = this.#movesList.length;
        }

        this.#saveLocal();
        this.#render(true);
    }
    
    getStacks() { return this.#stacks; }

    // --- LOGIQUE DE CALCUL TOOLTIP ---
    #getSnapshotValue(index) {
        const nums = this.#numbers;
        let stacks = new TwoStacks(nums, []);
        let move = this.getMoveAt(index);
        if (move !== 'pa' && move !== 'pb') return null;
        for (let i = 0; i < index; i++) {
            stacks.applyMove(this.getMoveAt(i));
        }
        let stack = move === 'pa' ? stacks.getStackB() : stacks.getStackA();
        return stack.getSize() > 0 ? stack.top() : "empty";
    }

    // --- SYSTEME ---
    #saveLocal() {
        localStorage.setItem(`ps_moves_${this.id}`, JSON.stringify(this.#movesList)); 
        localStorage.setItem(`ps_idx_${this.id}`, this.#currentIndex); 
        localStorage.setItem(`ps_edit_mode_${this.id}`, this.editMode);
    }

    #loadLocal() {
        const savedMoves = localStorage.getItem(`ps_moves_${this.id}`);
        const savedIdx = localStorage.getItem(`ps_idx_${this.id}`);
        const savedMode = localStorage.getItem(`ps_edit_mode_${this.id}`);
        
        if (savedMoves) this.#movesList = JSON.parse(savedMoves);
        if (savedIdx) this.#currentIndex = Number.parseInt(savedIdx);
        if (savedMode) this.editMode = savedMode;
        else this.editMode = 'truncate';
    }
    #copyMoves() { navigator.clipboard.writeText(this.#movesList.join(' ')); }
    #clearMoves() { this.#movesList = []; this.#currentIndex = 0; this.#render(true); this.#saveLocal(); }

    getInitialState() { return [...this.#numbers]; }

    setInitialState(numbers) {
        if (!numbers || !Array.isArray(numbers)) {
            throw new Error('Invalid initial state');
        }
        this.#numbers = [...numbers];
        this.#render();
    }

    #addMove(op) {
        if(this.#isPlaying) return;
        
        if (this.editMode === 'truncate') {
            // Mode actuel : Coupe tout ce qui suit
            this.#movesList = this.#movesList.slice(0, this.#currentIndex);
            this.#movesList.push(op);
            this.#currentIndex++;
        } 
        else if (this.editMode === 'insert') {
            // Mode Insertion : Glisse l'instruction à l'index actuel
            this.#movesList.splice(this.#currentIndex, 0, op);
            this.#currentIndex++;
        } 
        else if (this.editMode === 'overwrite') {
            // Mode Overwrite : Remplace l'instruction suivante (ou ajoute si fin)
            if (this.#currentIndex < this.#movesList.length) {
                this.#movesList[this.#currentIndex] = op;
            } else {
                this.#movesList.push(op);
            }
            this.#currentIndex++;
        }

        this.#render(true);
        this.#saveLocal();
    }

    #syncMoves() {
        const text = this.movesView.innerText.replace(/,/g, ' ');
        this.#movesList = text.trim().split(/\s+/).filter(x => x !== "");
        if (this.#currentIndex > this.getMoveListSize()) this.#currentIndex = this.getMoveListSize().length;
        this.#render(false);
    }

    step(dir) {
        let newIdx = this.#currentIndex + dir;
        if (newIdx >= 0 && newIdx <= this.getMoveListSize()) {
            this.setIndex(newIdx);
            return true;
        }
        return false;
    }

    #render(forceRedrawList = false) {
        const nums = this.#numbers;
        const maxVal = nums.length > 0 ? Math.max(...nums.map(Math.abs)) : 1;
        this.#stacks = new TwoStacks(nums, []);
        for (let i = 0; i < this.#currentIndex; i++) this.#stacks.applyMove(this.getMoveAt(i));
        this.#drawStack(this.#stacks.getStackA(), this.viewA, maxVal);
        this.#drawStack(this.#stacks.getStackB(), this.viewB, maxVal);
        this.container.querySelector('.ps-visualizer').classList.toggle('success-border', this.#stacks.isSorted());

        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.mode === this.editMode);
        });

        // --- LOGIQUE DE GRISAGE ---
        const noMoves = this.getMoveListSize() === 0;
        const noStacks = this.#stacks.isEmpty();
        const isAtEnd = this.#currentIndex === this.getMoveListSize();
        const isAtStart = this.#currentIndex === 0;
        const blockAll = this.#isPlaying || noStacks;

        this.container.querySelector('.btn-play').disabled = blockAll || noMoves || isAtEnd;
        this.container.querySelector('.btn-next').disabled = blockAll || noMoves || isAtEnd;
        this.container.querySelector('.btn-prev').disabled = blockAll || noMoves || isAtStart;
        this.container.querySelector('.btn-stop').disabled = !this.#isPlaying;

        this.container.querySelectorAll('.controls-grid button').forEach(btn => {
            const op = btn.dataset.op;
            if (!op) return; // Ignore les spans vides

            let isDisabled = blockAll;

            // Logique métier : on grise si l'action est impossible
            if (!isDisabled) {
                let aSize = this.#stacks.getStackA().getSize();
                let bSize = this.#stacks.getStackB().getSize();
                if ([Move.SA, Move.RA, Move.RRA].includes(op)) isDisabled = aSize < 2;
                else if ([Move.SB, Move.RB, Move.RRB].includes(op)) isDisabled = bSize < 2;
                else if (op === Move.PA) isDisabled = bSize === 0;
                else if (op === Move.PB) isDisabled = aSize === 0;
                else if ([Move.SS, Move.RR, Move.RRR].includes(op)) isDisabled = (aSize < 2 && bSize < 2);
            }

            btn.disabled = isDisabled;
        });

        this.container.querySelector('.btn-delete').disabled = blockAll || isAtStart;
        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => btn.disabled = blockAll);

        this.#updateSidebar(forceRedrawList);
        
        if (this.onStateChange) this.onStateChange();
    }

    #drawStack(stack, element, maxVal) {
        element.innerHTML = '';
        // Inversion manuelle pour le rendu : le haut de la pile (fin de l'array) est affiché en premier
        for (const val of stack.iterator()) {
            const el = document.createElement('div'); el.className = 'element';
            const width = maxVal !== 0 ? (Math.abs(val) / maxVal) * 100 : 0;
            el.innerHTML = `<span class="el-label">${val}</span><div class="el-bar" style="width:${width}%; background:hsl(${200+(width*1.2)},70%,50%)"></div>`;
            element.appendChild(el);
        }
    }

    #updateSidebar(force) {
        this.container.querySelector('.idx-label').innerText = this.#currentIndex;
        this.container.querySelector('.count-label').innerText = `${this.#movesList.length} moves`;
        if (document.activeElement !== this.movesView || force) {
            this.movesView.innerHTML = '';
            this.#movesList.forEach((m, i) => {
                const span = document.createElement('span');
                span.className = "move-item " + (i === this.#currentIndex-1 ? "move-current" : (i < this.#currentIndex ? "move-past" : ""));
                span.innerText = m;
                span.onclick = () => { this.setIndex(i+1); };
                
                // Tooltip logic
                span.onmouseenter = () => {
                    if (this.#isPlaying) return;
                    const mLower = m.toLowerCase();
                    if (mLower === 'pa' || mLower === 'pb') {
                        const val = this.#getSnapshotValue(i);
                        // Re-calculer l'état à l'instant T pour l'optimum
                        let stacks = new TwoStacks(this.#numbers, []); 
                        for (let j = 0; j < i; j++) stacks.applyMove(this.getMoveAt(j));
                        span.title = `Pushed value: ${val}`;
                    }
                };
                this.movesView.appendChild(span);
            });
            const current = this.movesView.querySelector('.move-current');
            if (current) current.scrollIntoView({ behavior: 'auto', block: 'nearest' });
        }
    }

    async #runAuto() {
        this.setPlaying(true);
        
        while (this.#currentIndex < this.getMoveListSize() && this.#isPlaying) {
            const speed = Number.parseInt(this.speedInput.value);
            this.setIndex(this.#currentIndex + 1); 
            await new Promise(r => setTimeout(r, speed));
        }
        
        this.setPlaying(false);
    }

    setPlaying(val) {
        this.#isPlaying = val;
        this.#render();
    }
    
    isPlaying() { return this.#isPlaying; }
}