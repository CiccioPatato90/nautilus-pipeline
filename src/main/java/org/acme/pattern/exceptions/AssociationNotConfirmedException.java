package org.acme.pattern.exceptions;

public class AssociationNotConfirmedException extends RuntimeException {
    public AssociationNotConfirmedException(String message) {
        super("Association not confirmed on request with ID:" + message);
    }
}
