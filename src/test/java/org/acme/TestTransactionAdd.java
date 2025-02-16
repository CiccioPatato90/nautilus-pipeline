package org.acme;

import org.acme.pattern.context.BaseTransactionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)

public class TestTransactionAdd {

    private BaseTransactionContext context;

    @BeforeEach
    void setUp() {
        context = new BaseTransactionContext();
    }

    @Test
    void testLifecycle() {
        // Test PostConstruct
//        context.onBeginTransaction();
        assertTrue(context.getExecutedSteps().isEmpty());

        // Test operations
        context.setIntermediateResult("test");
        assertEquals("test", context.getIntermediateResult());
        assertEquals(0, context.getExecutedSteps().size());

        // Test error handling
        Exception testError = new RuntimeException("test error");
        context.setError(testError);

        // Test PreDestroy
        context.onBeforeEndTransaction();
        // Note: We can't directly verify logging, but we can verify the steps are preserved
        assertEquals(0, context.getExecutedSteps().size());
    }

    @Test
    void testTypeHandling() {
        // Test different types
        context.setIntermediateResult(42);
        Integer intResult = context.getIntermediateResult();
        assertEquals(42, intResult);

        context.setIntermediateResult("string");
        String strResult = context.getIntermediateResult();
        assertEquals("string", strResult);
    }
}
