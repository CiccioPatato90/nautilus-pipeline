package org.acme.pattern.handlers;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.acme.pattern.Handler;
import org.acme.pattern.RollbackCallback;
import org.acme.pattern.context.BaseTransactionContext;

import java.util.function.BiFunction;

public class DatabaseHandler<I, O, R> implements Handler<I, O> {
    private final R repository;
    private final BiFunction<I, R, O> operation;
//    private final RollbackCallback<R> rollbackCallback;
    private final RollbackCallback rollbackCallback;
    private BaseTransactionContext context;

    public DatabaseHandler(R repository, BiFunction<I, R, O> operation, RollbackCallback rollbackCallback) {
        this.repository = repository;
        this.operation = operation;
        this.rollbackCallback = rollbackCallback;
    }

    @Override
    public void setContext(BaseTransactionContext context) {
        this.context = context;
        // Automatically register the rollback callback in the context
//        context.addRollbackCallback((repo, ctx) -> rollbackCallback.rollback(this.repository, this.context));
        context.addRollbackCallback(rollbackCallback);
    }

    @Transactional
    @Override
    public O process(I input) {
        return operation.apply(input, repository);
    }
}
