package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class InternalServerErrorException extends WebException {

    public InternalServerErrorException() {
        super(500);
    }

    public InternalServerErrorException(String code, String message) {
        super(500, code, message);
    }

    public InternalServerErrorException(ErrorDefinition errorDefinition) {
        super(500, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public InternalServerErrorException(Object payload) {
        super(500, payload);
    }

}
