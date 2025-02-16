package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.MutinyHandler;
import org.acme.pattern.pipeline.MutinyPipeline;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class MutinyPipelineTest {

    // A simple handler that appends a suffix and prints the thread name.
    static class DummyHandler implements MutinyHandler<String, String> {
        private final String suffix;
        DummyHandler(String suffix) { this.suffix = suffix; }
        @Override
        public Uni<String> process(String input) {
            System.out.println("Processing '" + input + "' in thread: " + Thread.currentThread().toString());
            return Uni.createFrom().item(input + suffix);
        }
    }

    // An asynchronous handler that runs on the COMPUTATION scheduler.
    static class AsyncHandler implements MutinyHandler<String, String> {
        private final String suffix;
        AsyncHandler(String suffix) { this.suffix = suffix; }
        @Override
        public Uni<String> process(String input) {
            return Uni.createFrom().item(input + suffix)
                    .onItem().delayIt().by(Duration.ofMillis(10))
                    .invoke(result ->
                            System.out.println("Async processing '" + input + "' -> '" + result
                                    + "' in thread: " + Thread.currentThread().getName()));
        }
    }

    @Test
    void testPipelineChaining() {
        BaseTransactionContext context = new BaseTransactionContext();
        MutinyHandler<String, String> handler1 = new DummyHandler("-step1");
        MutinyPipeline<String, String> pipeline = new MutinyPipeline<>(handler1, context);
        MutinyHandler<String, String> handler2 = new DummyHandler("-step2");
        MutinyPipeline<String, String> fullPipeline = pipeline.addHandler(handler2);

        String result = fullPipeline.execute("start").await().indefinitely();
        System.out.println("Final result: " + result + " on thread: " + Thread.currentThread().toString());
        assertEquals("start-step1-step2", result);
    }

    @Test
    void testPipelineWithAsyncHandler() {
        BaseTransactionContext context = new BaseTransactionContext();
        MutinyHandler<String, String> handler1 = new AsyncHandler("-asyncStep1");
        MutinyPipeline<String, String> pipeline = new MutinyPipeline<>(handler1, context);
        MutinyHandler<String, String> handler2 = new AsyncHandler("-asyncStep2");
        MutinyPipeline<String, String> fullPipeline = pipeline.addHandler(handler2);

        String result = fullPipeline.execute("start").await().indefinitely();
        System.out.println("Final async result: " + result + " on thread: " + Thread.currentThread().toString());
        assertEquals("start-asyncStep1-asyncStep2", result);
    }
}
