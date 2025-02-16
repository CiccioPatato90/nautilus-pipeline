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
        String stepName = delegate.getClass().getName();
        try {
            context.logStep("Starting step: " + stepName + " at " + LocalDateTime.now());
            O output = delegate.process(input);
            context.setIntermediateResult(output);
            context.logStep("Completed step: " + stepName + " at " + LocalDateTime.now());
            return output;
        } catch (Exception e) {
            context.logStep("Error in step: " + stepName + " at " + LocalDateTime.now() + " - " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void setContext(BaseTransactionContext context) {
        this.context = context;
        delegate.setContext(context);
    }
}
