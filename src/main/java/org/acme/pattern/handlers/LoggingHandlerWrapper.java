package org.acme.pattern.handlers;

import org.acme.pattern.Handler;
import org.acme.pattern.context.BaseTransactionContext;

import java.time.LocalDateTime;

public class LoggingHandlerWrapper<I, O> implements Handler<I, O> {
    private final Handler<I, O> delegate;
    private BaseTransactionContext context;

    public LoggingHandlerWrapper(Handler<I, O> delegate, BaseTransactionContext context) {
        this.delegate = delegate;
        this.context = context;
        this.delegate.setContext(context);
    }

    @Override
    public O process(I input) {
//        String stepName = delegate.getClass().getName();
        try {
            O output = delegate.process(input);
            context.setIntermediateResult(output);
            return output;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void setContext(BaseTransactionContext context) {
        this.context = context;
        delegate.setContext(context);
    }
}
