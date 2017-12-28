package it.mgt.util.spring.web.exception;

import org.springframework.http.HttpStatus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class ExceptionPayload {

    private String code;
    private String message;

    public ExceptionPayload() {
    }

    public ExceptionPayload(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ExceptionPayload(HttpStatus status, String message) {
        this.code = String.valueOf(status.value());
        this.message = message;
    }

    public ExceptionPayload(HttpStatus status) {
        this.code = String.valueOf(status.value());
        this.message = status.getReasonPhrase();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
