package org.acme.pattern;

import io.smallrye.mutiny.Uni;
import org.acme.pattern.context.BaseTransactionContext;

public interface Handler<I, O> {
//    void setContext(BaseTransactionContext context);

    O process(I input);
    default void setContext(BaseTransactionContext context) {
        // Default no-op: handlers override this if they need the context.
    };
}