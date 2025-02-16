package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.handlers.ConvertToCharArrayHandler;
import org.acme.pattern.handlers.RemoveAlphabetHandler;
import org.acme.pattern.handlers.RemoveDigitsHandler;
import org.acme.pattern.pipeline.Pipeline;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TestPattern {
    @Test
    void testPipelineSuccessfulExecution() {
        var filters = new Pipeline<>(new RemoveAlphabetHandler(), new BaseTransactionContext())
                .addHandler(new RemoveDigitsHandler())
                .addHandler(new ConvertToCharArrayHandler());
        var input = "GoYankees123!";
        var output = filters.execute(input);
        System.out.println("Piepline executed. Result: " + output);
    }
}
