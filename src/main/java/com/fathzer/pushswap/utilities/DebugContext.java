package com.fathzer.pushswap.utilities;

import java.util.List;
import java.util.function.Supplier;

public interface DebugContext {
    void setDebug(boolean debug);

    boolean isDebug();

    default void debug(String message) {
        if (isDebug()) {
            System.out.println(message);
        }
    }

    default void debug(String message, List<Supplier<Object>> suppliers) {
        if (isDebug()) {
            String result = message;
            for (Supplier<Object> supplier : suppliers) {
                result = result.replaceFirst("\\{\\}", supplier.get().toString());
            }
            System.out.println(result);
        }
    }
}
