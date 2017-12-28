package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class NotFoundException extends WebException {

    public NotFoundException() {
        super(404);
    }

    public NotFoundException(String code, String message) {
        super(404, code, message);
    }

    public NotFoundException(ErrorDefinition errorDefinition) {
        super(404, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public NotFoundException(Object payload) {
        super(404, payload);
    }

}
