package org.acme.pattern;

import io.smallrye.mutiny.Uni;

public interface MutinyHandler<I, O> {
    Uni<O> process(I input);
}
