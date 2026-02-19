package com.fathzer.pushswap.buterfly;


public abstract class AbstractBPusher implements BPusher {
    protected int low;
    protected int high;

    @Override
    public Command evaluate(int value) {
        if (value<low) {
            // Élément "petit" -> fond de B
            incrementLimits();
            return Command.TO_BOTTOM;
        } else if (value <= high) {
            // Élément "moyen" -> haut de B
            incrementLimits();
            return Command.TO_TOP;
        } else {
            // Élément "grand" -> on le garde en A
            return Command.KEEP;
            // Optionnel : on pourrait incrémenter très légèrement high ici 
            // pour éviter de tourner trop longtemps, mais le ra suffit 
            // car i finira par augmenter via les autres conditions.            
        }
    }

    protected abstract void incrementLimits();
}
