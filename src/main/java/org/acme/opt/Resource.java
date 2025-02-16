package org.acme.opt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Resource {
    private final String name;
    private final int availableCapacity;
    private final int cost;
}
