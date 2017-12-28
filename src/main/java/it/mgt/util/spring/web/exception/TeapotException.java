package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class TeapotException extends WebException {

    public TeapotException() {
        super(418);
    }

    public TeapotException(String code, String message) {
        super(418, code, message);
    }

    public TeapotException(ErrorDefinition errorDefinition) {
        super(418, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public TeapotException(Object payload) {
        super(418, payload);
    }

}
