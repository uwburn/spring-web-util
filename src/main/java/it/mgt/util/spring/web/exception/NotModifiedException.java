package it.mgt.util.spring.web.exception;

public class NotModifiedException extends WebException {

    public NotModifiedException() {
        super(301);
    }

}
