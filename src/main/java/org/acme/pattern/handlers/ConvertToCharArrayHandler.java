package org.acme.pattern.handlers;

import org.acme.pattern.Handler;

public class ConvertToCharArrayHandler implements Handler<String, char[]> {
    public char[] process(String input) {
        return input.toCharArray();
    }
}
