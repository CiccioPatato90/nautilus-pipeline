package org.acme.pattern;

import org.acme.pattern.context.BaseTransactionContext;

@FunctionalInterface
public interface RollbackCallback<R> {
//    void rollback(R repository, BaseTransactionContext context);
    void rollback();
}
