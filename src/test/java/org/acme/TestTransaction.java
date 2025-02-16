package org.acme;

import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.Handler;
import org.acme.pattern.pipeline.Pipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestTransaction {

//    @Mock
//    private BaseTransactionContext mockContext;
//
//    private Pipeline<String, Integer> pipeline;
//
//    // Sample handlers for testing
//    private static class StringLengthHandler implements Handler<String, Integer> {
//        @Override
//        public Integer process(String input) {
//            if (input == null) {
//                throw new IllegalArgumentException("Input cannot be null");
//            }
//            return input.length();
//        }
//    }
//
//    private static class MultiplyHandler implements Handler<Integer, Integer> {
//        private final int multiplier;
//
//        public MultiplyHandler(int multiplier) {
//            this.multiplier = multiplier;
//        }
//
//        @Override
//        public Integer process(Integer input) {
//            return input * multiplier;
//        }
//    }
//
//    @BeforeEach
//    void setUp() {
//        pipeline = new Pipeline<>(new StringLengthHandler(), mockContext);
//        // Inject mock context
//        TestUtils.setFieldValue(pipeline, "baseContext", mockContext);
//    }
//
//    @Test
//    void testSuccessfulPipelineExecution() {
//        // Given
//        Pipeline<String, Integer> testPipeline = pipeline
//                .addHandler(new MultiplyHandler(2));
//
//        // When
//        Integer result = testPipeline.execute("test");
//
//        // Then
//        assertEquals(8, result); // "test".length() = 4, then 4 * 2 = 8
//        verify(mockContext, times(2)).setIntermediateResult(any());
//    }
//
//    @Test
//    void testPipelineWithNullInput() {
//        // Given
//        Pipeline<String, Integer> testPipeline = pipeline
//                .addHandler(new MultiplyHandler(2));
//
//        // When/Then
//        Exception exception = assertThrows(IllegalArgumentException.class,
//                () -> testPipeline.execute(null));
//
//        verify(mockContext).setError(any(IllegalArgumentException.class));
//    }
//
//    @Test
//    void testMultipleHandlers() {
//        // Given
//        Pipeline<String, Integer> testPipeline = pipeline
//                .addHandler(new MultiplyHandler(2))
//                .addHandler(new MultiplyHandler(3));
//
//        // When
//        Integer result = testPipeline.execute("test");
//
//        // Then
//        assertEquals(24, result); // "test".length() = 4, then 4 * 2 * 3 = 24
//        verify(mockContext, times(3)).setIntermediateResult(any());
//    }
//
//    @Test
//    void testContextTracking() {
//        // Given
//        Pipeline<String, Integer> testPipeline = pipeline
//                .addHandler(new MultiplyHandler(2));
//
//        // When
//        testPipeline.execute("test");
//
//        // Then
//        verify(mockContext).setIntermediateResult(4); // First handler result
//        verify(mockContext).setIntermediateResult(8); // Second handler result
//    }
//
//    // Utility class for setting private fields in tests
//    private static class TestUtils {
//        static void setFieldValue(Object object, String fieldName, Object value) {
//            try {
//                var field = object.getClass().getDeclaredField(fieldName);
//                field.setAccessible(true);
//                field.set(object, value);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
}