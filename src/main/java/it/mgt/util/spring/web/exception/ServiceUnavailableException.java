package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.ErrorDefinition;

public class ServiceUnavailableException extends WebException {

    public ServiceUnavailableException() {
        super(503);
    }

    public ServiceUnavailableException(String code, String message) {
        super(503, code, message);
    }

    public ServiceUnavailableException(ErrorDefinition errorDefinition) {
        super(503, errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public ServiceUnavailableException(Object payload) {
        super(503, payload);
    }

}
