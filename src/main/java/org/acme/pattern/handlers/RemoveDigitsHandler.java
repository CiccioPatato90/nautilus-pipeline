package org.acme.pattern.handlers;

import org.acme.pattern.Handler;

public class RemoveDigitsHandler implements Handler<String, String> {
    public String process(String input) {
        // Simulate DB operation that might fail
        return input.replaceAll("\\d", "");
    }
}