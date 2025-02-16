package org.acme.pattern.pipeline;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.pattern.Handler;
import org.acme.pattern.RollbackCallback;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.context.TypedPipelineContext;
import org.acme.pattern.handlers.LoggingHandlerWrapper;

public class Pipeline<I, O> {

    private final BaseTransactionContext baseContext;

    private final Handler<I, O> currentHandler;

    public Pipeline(Handler<I, O> currentHandler, BaseTransactionContext baseContext) {
//        this.currentHandler = currentHandler;
        this.baseContext = baseContext;
        this.currentHandler = new LoggingHandlerWrapper<>(currentHandler, baseContext);
    }

    public <K> Pipeline<I, K> addHandler(Handler<O, K> newHandler) {
        newHandler.setContext(baseContext);
        return new Pipeline<>(input -> {
            TypedPipelineContext<O> typedContext = new TypedPipelineContext<>(baseContext);
            O intermediateResult = currentHandler.process(input);
            typedContext.setResult(intermediateResult);
            return newHandler.process(intermediateResult);
        }, this.baseContext);
    }

    @Transactional
    public O execute(I input) {
        try {
            TypedPipelineContext<O> typedContext = new TypedPipelineContext<>(baseContext);
            O result = currentHandler.process(input);
            typedContext.setResult(result);
            return result;
        } catch (Exception e) {
            baseContext.setError(e);
            baseContext.getExecutedSteps().forEach(System.err::println);
            // Invoke all registered rollback callbacks
            for (RollbackCallback callback : baseContext.getRollbackCallbacks()) {
                try {
                    callback.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Rollback callback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        }
    }

//    public <K> Pipeline<I, K> addHandler(Handler<O, K> newHandler) {
//        // Wrap the new handler to enable logging
//        Handler<O, K> wrappedHandler = new LoggingHandlerWrapper<>(newHandler, baseContext);
//        // Compose the handlers
//        Handler<I, K> combinedHandler = input -> {
//            O intermediate = currentHandler.process(input);
//            return wrappedHandler.process(intermediate);
//        };
//        return new Pipeline<>(combinedHandler, baseContext);
//    }
//
//    @Transactional
//    public O execute(I input) {
//        try {
//            O result = currentHandler.process(input);
//            baseContext.setIntermediateResult(result);
//            return result;
//        } catch (Exception e) {
//            baseContext.setError(e);
//            baseContext.getExecutedSteps().forEach(System.err::println);
//            throw e;
//        }
//    }
}
