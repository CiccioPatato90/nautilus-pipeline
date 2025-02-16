package org.acme.pattern.context;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.TransactionScoped;
import lombok.Getter;
import lombok.Setter;
import org.acme.pattern.RollbackCallback;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Getter
@Setter
@TransactionScoped
public class BaseTransactionContext{
    //        executedSteps.add("Step completed at: " + LocalDateTime.now() + "on Obj: " + result.getClass());
    @Setter
    private Object intermediateResult;
    private final List<String> executedSteps = new ArrayList<>();
    private Exception error;
    private final Map<String, Object> contextData = new ConcurrentHashMap<>();
    @Getter
    private final List<RollbackCallback> rollbackCallbacks = new ArrayList<>();

    @PostConstruct
    void onBeginTransaction() {
        executedSteps.clear();
        contextData.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getIntermediateResult() {
        return (T) intermediateResult;
    }

    public <T> void put(String key, T value) {
        contextData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) contextData.get(key);
    }

    public void addRollbackCallback(RollbackCallback callback) {
        rollbackCallbacks.add(callback);
    }

    public void logStep(String message) {
        executedSteps.add(message);
        System.out.println(message);
    }

    @PreDestroy
    public void onBeforeEndTransaction() {
        if (error != null) {
            executedSteps.forEach(step ->
                    Logger.getLogger(getClass().getName())
                            .warning("Rolling back step: " + step));
        }
    }
}
