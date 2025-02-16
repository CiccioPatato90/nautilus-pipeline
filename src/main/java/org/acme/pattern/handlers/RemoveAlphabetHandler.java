package org.acme.pattern.handlers;

import org.acme.pattern.Handler;

public class RemoveAlphabetHandler implements Handler<String, String> {
    public String process(String input) {
        return input.replaceAll("[A-Za-z]", "");
    }
}
