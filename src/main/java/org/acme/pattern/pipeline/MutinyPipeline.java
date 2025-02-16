package org.acme.pattern.pipeline;

import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.MutinyHandler;
import org.acme.pattern.context.TypedPipelineContext;

@AllArgsConstructor
public class MutinyPipeline<I, O> {
    private final MutinyHandler<I, O> currentHandler;
    private final BaseTransactionContext baseContext;


    public <K> MutinyPipeline<I, K> addHandler(MutinyHandler<O, K> newHandler) {
        MutinyHandler<I, K> combinedHandler = input ->
                currentHandler.process(input)
                        .invoke(intermediateResult ->
                                new TypedPipelineContext<>(baseContext).setResult(intermediateResult))
                        .flatMap(newHandler::process);
        return new MutinyPipeline<>(combinedHandler, baseContext);
    }


    @Transactional
    public Uni<O> execute(I input) {
        return currentHandler.process(input)
                .onItem().transformToUni(o -> {
                    TypedPipelineContext<O> typedContext = new TypedPipelineContext<>(baseContext);
                    typedContext.setResult(o);
                    System.out.println("Executing pipeline on thread: " + Thread.currentThread().getName());
                    return Uni.createFrom().item(o);
                })
                .onFailure().invoke(e -> {
                    baseContext.setError((Exception) e);
                    System.out.println("Pipeline error on thread: " + Thread.currentThread().getName());
                });
    }

}
