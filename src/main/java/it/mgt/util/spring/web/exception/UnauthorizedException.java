package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class UnauthorizedException extends WebException {

    public UnauthorizedException() {
        super(401);
    }

    public UnauthorizedException(String code, String message) {
        super(401, code, message);
    }

    public UnauthorizedException(ErrorDefinition errorDefinition) {
        super(401, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public UnauthorizedException(Object payload) {
        super(401, payload);
    }

}
