package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class ForbiddenException extends WebException {

    public ForbiddenException() {
        super(403);
    }

    public ForbiddenException(String code, String message) {
        super(403, code, message);
    }

    public ForbiddenException(ErrorDefinition errorDefinition) {
        super(403, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public ForbiddenException(Object payload) {
        super(403, payload);
    }

}
