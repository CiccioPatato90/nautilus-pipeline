package org.acme.pattern;

public interface TransactionalHandler<I, O> extends Handler<I, O> {
    void rollback(O output);
}
