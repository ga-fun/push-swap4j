export class AnimationRunner {
    #target;
    #onAnimationEnd;
    #isRunning = false;
    #lastTime;
    #lastMoveIndex;
    #animateFrame;

    constructor(target, onAnimationEnd) {
        this.#target = target;
        this.#onAnimationEnd = onAnimationEnd;
    }

    run(getMovesPerSecond) {
        if (this.#isRunning) return;
        
        this.#isRunning = true;
        this.#lastTime = performance.now();
        this.#lastMoveIndex = this.#target.getIndex();
        
        const animate = (currentTime) => {
            if (!this.#isRunning || this.#target.getIndex() >= this.#target.getSize() - 1) {
                this.stop();
                return;
            }
            
            const movesPerSecond = getMovesPerSecond();
            const deltaTime = currentTime - this.#lastTime;
            const expectedMoveIndex = Math.floor(this.#lastMoveIndex + (deltaTime * movesPerSecond / 1000));
            const targetIndex = Math.min(expectedMoveIndex, this.#target.getSize() - 1);
            
            if (targetIndex > this.#target.getIndex()) {
                if ('requestIdleCallback' in globalThis) {
                    requestIdleCallback(() => {
                        if (this.#isRunning) {
                            this.#target.setIndex(targetIndex);
                        }
                    });
                } else {
                    this.#target.setIndex(targetIndex);
                }
                this.#lastMoveIndex = targetIndex;
                this.#lastTime = currentTime;
            }
            
            if (targetIndex < this.#target.getSize() - 1) {
                this.#animateFrame = requestAnimationFrame(animate);
            } else {
                this.stop();
            }
        };
        
        this.#animateFrame = requestAnimationFrame(animate);
    }

    stop() {
        this.#isRunning = false;
        if (this.#animateFrame) {
            cancelAnimationFrame(this.#animateFrame);
            this.#animateFrame = null;
        }
        if (this.#onAnimationEnd) {
            console.log("Notifying end of animation"); //TODO
            if (this.#onAnimationEnd) {
                this.#onAnimationEnd();
            }
        }
    }

    isRunning() {
        return this.#isRunning;
    }
}
