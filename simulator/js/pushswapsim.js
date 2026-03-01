import { TwoStacks, Move } from './stack.js';
import { TwoStacksView } from './twostacksview.js';
import { ListView } from './listView.js';

export class PushSwapSim {
    static #VALID_MOVES = new Set(Object.values(Move));
    
    #isPlaying = false;
    #numbers = [];
    #stacksView;
    #movesView;

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
            <div class="ps-visualizer-anchor"></div>
        `;
        document.getElementById(containerId).appendChild(this.container);

        const anchor = this.container.querySelector('.ps-visualizer-anchor');
        this.#stacksView = new TwoStacksView(anchor);

        const movesView = this.container.querySelector('.moves-display');
        this.#movesView = new ListView(movesView, {
            onItemClick: (move, i) => this.setIndex(i + 1),
            onItemMouseEnter: (move, i, el) => {
                if (this.#isPlaying) return;
                if (move === 'pa' || move === 'pb') {
                    const val = this.#getSnapshotValue(i);
                    el.title = `Pushed value: ${val}`;
                }
            }
        });
        this.speedInput = this.container.querySelector('.speed-range');
        
        this.#initEvents();
        this.#loadLocal();
        
        // Initialize stacks if no initial state was set
        if (this.#numbers.length === 0) {
            this.#numbers = [];
        }
        this.#rebuildStacks();
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
        this.container.querySelector('.moves-display').oninput = () => { this.#syncMoves(); this.#saveLocal(); };

        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.onclick = () => {
                this.editMode = btn.dataset.mode;
                this.#saveLocal();
                this.#render();
            };
        });

        const delBtn = this.container.querySelector('.btn-delete');
        delBtn.onclick = () => {
            const index = this.#movesView.getIndex();
            if (index > 0) {
                this.#movesView.update(this.#movesView.getList().splice(index - 1, 1), index - 1);
            }
        };
    }

    getIndex() { return this.#movesView.getIndex(); }

    setIndex(idx) {
        let futureIndex = Math.max(0, Math.min(idx, this.#movesView.getList().length));
        const oldIndex = this.#movesView.getIndex();
        if (futureIndex == this.#movesView.getIndex()) return;
        this.#movesView.setIndex(futureIndex);
        this.#saveLocal();
        
        // Use optimized applyMove for adjacent steps
        if (Math.abs(oldIndex - futureIndex) === 1) {
            // Use incremental moves for adjacent steps
            this.#applyIncrementalMoves(oldIndex);
        } else {
            // For non-adjacent changes, rebuild stacks completely
            this.#rebuildStacks();
        }
        
        // Render sidebar only (stacks are already updated in TwoStacksView)
        this.#render(true);
    }

    #applyIncrementalMoves(oldIndex) {
        //FIXME Undo seems buggy
        const newIndex = this.#movesView.getIndex();
        const direction = newIndex > oldIndex ? 1 : -1;
        const start = direction > 0 ? oldIndex : newIndex;
        const end = direction > 0 ? newIndex : oldIndex;
        
        for (let i = start; i < end; i++) {
            const moveIndex = direction > 0 ? i : i - 1;
            if (moveIndex >= 0 && moveIndex < this.getMoveListSize()) {
                this.#stacksView.applyMove(this.getMoveAt(moveIndex));
            }
        }
    }

    getMovesList() { return [...this.#movesView.getList()]; }

    getMoveListSize() { return this.#movesView.getList().length; }

    getMoveAt(index) { return this.#movesView.getList()[index].toLowerCase(); }

    setMovesList(newList) {
        if (!Array.isArray(newList)) throw new TypeError(`Expected Array, got: ${typeof newList}`);
        const invalidOp = newList.find(op => !PushSwapSim.#VALID_MOVES.has(op.toLowerCase()));        
        if (invalidOp) throw new Error(`Invalid move: "${invalidOp}"`);

        this.#movesView.update([...newList], 0); 

        // Rebuild stacks in TwoStacksView
        this.#rebuildStacks();

        this.#saveLocal();
        this.#render(true);
    }
    
    // On expose les stacks de la vue si besoin
    getStacks() { return this.#stacksView.getStacks(); }

    #getSnapshotValue(index) {
        const nums = this.#numbers;
        let stacks = new TwoStacks(nums, []);
        let move = this.getMoveAt(index);
        if (move !== 'pa' && move !== 'pb') return null;
        for (let i = 0; i < index; i++) stacks.applyMove(this.getMoveAt(i));
        
        let stack = move === 'pa' ? stacks.getStackB() : stacks.getStackA();
        return stack.getSize() > 0 ? stack.top() : "empty";
    }

    #saveLocal() {
        localStorage.setItem(`ps_moves_${this.id}`, JSON.stringify(this.#movesView.getList())); 
        localStorage.setItem(`ps_idx_${this.id}`, this.#movesView.getIndex()); 
        localStorage.setItem(`ps_edit_mode_${this.id}`, this.editMode);
    }

    #loadLocal() {
        const savedMoves = localStorage.getItem(`ps_moves_${this.id}`);
        const savedIdx = localStorage.getItem(`ps_idx_${this.id}`);
        const savedMode = localStorage.getItem(`ps_edit_mode_${this.id}`);
        const idx = savedIdx ? Number.parseInt(savedIdx) : 0;
        if (savedMoves) this.#movesView.update(JSON.parse(savedMoves), idx);
        this.editMode = savedMode || 'truncate';
    }
    #copyMoves() { navigator.clipboard.writeText(this.getMovesList().join(' ')); }
    #clearMoves() { 
        this.#movesView.update([], 0); 
        this.#rebuildStacks();
        this.#render(true); 
        this.#saveLocal(); 
    }

    getInitialState() { return [...this.#numbers]; }
    setInitialState(numbers) {
        if (!numbers || !Array.isArray(numbers)) throw new Error('Invalid initial state');
        this.#numbers = [...numbers];
        
        // Initialize TwoStacksView with the initial state
        const stacks = new TwoStacks(numbers, []);
        this.#stacksView.setStacks(stacks);
        
        // Reset to beginning and render sidebar
        this.#movesView.setIndex(0);
        this.#saveLocal();
        this.#render(true);
    }

    #addMove(op) {
        if(this.#isPlaying) return;
        const idx = this.#movesView.getIndex();
        let list = this.#movesView.getList();
        if (this.editMode === 'truncate') {
            list = list.slice(0, idx);
            list.push(op);
            this.#movesView.update(list, idx + 1);
        } else if (this.editMode === 'insert') {
            list = list.splice(idx, 0, op);
            this.#movesView.update(list, idx + 1);
        } else if (this.editMode === 'overwrite') {
            if (idx < list.length) list[idx] = op;
            else list.push(op);
            this.#movesView.update(list, idx + 1);
        }
        this.#render(true);
        this.#saveLocal();
    }

    #syncMoves() {
        const text = this.container.querySelector('.moves-display').innerText.replace(/,/g, ' ');
        let moves = text.trim().split(/\s+/).filter(x => x !== "");
        let idx = this.#movesView.getIndex();

        if (idx > moves.length) idx = moves.length;
        this.#movesView.update(moves, idx);
        this.#render(false);
    }

    step(dir) {
        //TODO Is this still used ?
        let newIdx = this.#movesView.getIndex() + dir;
        if (newIdx >= 0 && newIdx <= this.getMoveListSize()) {
            this.setIndex(newIdx);
            return true;
        }
        return false;
    }

    #rebuildStacks() {
        const stacks = new TwoStacks(this.#numbers, []);
        for (let i = 0; i < this.#movesView.getIndex(); i++) {
            stacks.applyMove(this.getMoveAt(i));
        }
        this.#stacksView.setStacks(stacks);
    }

    #render(forceRedrawList = false) {
        // 1. STACKS ARE NOW MANAGED BY TwoStacksView - NO CREATION NEEDED HERE
        const stacks = this.#stacksView.getStacks();
        
        // 2. SIDEBAR Update
        this.container.querySelectorAll('.btn-mode-edit').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.mode === this.editMode);
        });

        const noMoves = this.getMoveListSize() === 0;
        const noStacks = stacks.isEmpty();
        const isAtEnd = this.#movesView.getIndex() === this.getMoveListSize();
        const isAtStart = this.#movesView.getIndex() === 0;
        const blockAll = this.#isPlaying || noStacks;

        this.container.querySelector('.btn-play').disabled = blockAll || noMoves || isAtEnd;
        this.container.querySelector('.btn-next').disabled = blockAll || noMoves || isAtEnd;
        this.container.querySelector('.btn-prev').disabled = blockAll || noMoves || isAtStart;
        this.container.querySelector('.btn-stop').disabled = !this.#isPlaying;

        this.container.querySelectorAll('.controls-grid button').forEach(btn => {
            const op = btn.dataset.op;
            if (!op) return;
            let isDisabled = blockAll;
            if (!isDisabled) {
                let aSize = stacks.getStackA().getSize();
                let bSize = stacks.getStackB().getSize();
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

    #updateSidebar(force) {
        this.container.querySelector('.idx-label').innerText = this.#movesView.getIndex();
        this.container.querySelector('.count-label').innerText = `${this.#movesView.getList().length} moves`;
    }

    setMovesSelection(start, end, className) {
        this.#movesView.clearSelection();
        if (start < end) this.#movesView.applySelection(start, end, className);
    }

    async #runAuto() {
        console.log("runAuto", Number.parseInt(this.speedInput.value)); //TODO
        this.setPlaying(true);
        while (this.#movesView.getIndex() < this.getMoveListSize() && this.#isPlaying) {
            const speed = Number.parseInt(this.speedInput.value);
            this.setIndex(this.#movesView.getIndex() + 1); 
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