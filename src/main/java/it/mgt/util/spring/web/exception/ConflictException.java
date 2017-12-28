package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class ConflictException extends WebException {

    public ConflictException() {
        super(409);
    }

    public ConflictException(String code, String message) {
        super(409, code, message);
    }

    public ConflictException(ErrorDefinition errorDefinition) {
        super(409, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public ConflictException(Object payload) {
        super(409, payload);
    }

}
