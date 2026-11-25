package com.simulador.api;

import com.mongodb.MongoException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/** Retries an entire proxied service call when MongoDB reports a transient write conflict. */
/** Reintenta operaciones afectadas por conflictos de escritura MongoDB. */
@Component
public class MongoWriteConflictRetry {
    private static final int MAX_ATTEMPTS = 5;

    /** Ejecuta una operación con reintentos ante conflictos de escritura. */
    public <T> T execute(Supplier<T> operation) {
        for (int attempt = 1; ; attempt++) {
            try {
                return operation.get();
            } catch (RuntimeException error) {
                if (attempt >= MAX_ATTEMPTS || !isWriteConflict(error)) throw error;
                try {
                    Thread.sleep(35L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw error;
                }
            }
        }
    }

    private boolean isWriteConflict(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof MongoException mongo
                    && (mongo.getCode() == 112 || mongo.hasErrorLabel("TransientTransactionError"))) {
                return true;
            }
        }
        return false;
    }
}
