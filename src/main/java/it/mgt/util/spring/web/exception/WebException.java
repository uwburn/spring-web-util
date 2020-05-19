package it.mgt.util.spring.web.exception;

import it.mgt.util.spring.exception.CodedException;
import it.mgt.util.spring.exception.ErrorDefinition;

public abstract class WebException extends RuntimeException {

    private Integer status;
    private Object payload;

    public WebException() { }

    public WebException(int status) {
        super("(" + status + ")");
        this.status = status;
    }

    public WebException(int status, String code, String message) {
        super("(" + status + ") " + code + " - " + message);
        this.status = status;
        payload = new ExceptionPayload(code, message);
    }

    public WebException(int status, ErrorDefinition errorDefinition) {
        super("(" + status + ") " + errorDefinition.getCode() + " - " + errorDefinition.getMessage());
        this.status = status;
        payload = new ExceptionPayload(errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public WebException(String code, String message) {
        super(code + " - " + message);
        payload = new ExceptionPayload(code, message);
    }

    public WebException(ErrorDefinition errorDefinition) {
        super(errorDefinition.getCode() + " - " + errorDefinition.getMessage());
        payload = new ExceptionPayload(errorDefinition.getCode(), errorDefinition.getMessage());
    }

    public WebException(int status, CodedException codedException) {
        super("(" + status + ") " + codedException.getCode() + " - " + codedException.getMessage());
        this.status = status;
        payload = new ExceptionPayload(codedException.getCode(), codedException.getMessage());
    }

    public WebException(int status, Object payload) {
        super("(" + status + ")");
        this.status = status;
        this.payload = payload;
    }

    public WebException(Object payload) {
        this.payload = payload;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
