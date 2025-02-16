package org.acme.pattern.context;

import org.acme.pattern.context.BaseTransactionContext;

import java.util.List;

public class TypedPipelineContext<T> {
    private final BaseTransactionContext context;

    public TypedPipelineContext(BaseTransactionContext context) {
        this.context = context;
    }

    public void setResult(T result) {
        context.setIntermediateResult(result);
    }

    public T getResult() {
        return context.getIntermediateResult();
    }

    public List<String> getExecutedSteps() {
        return context.getExecutedSteps();
    }
}
