package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class BadRequestException extends WebException {

    public BadRequestException() {
        super(400);
    }

    public BadRequestException(String code, String message) {
        super(400, code, message);
    }

    public BadRequestException(ErrorDefinition errorDefinition) {
        super(400, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public BadRequestException(Object payload) {
        super(400, payload);
    }

}
