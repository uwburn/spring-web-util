package it.mgt.util.spring.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

@ControllerAdvice
public class WebApiExceptionAdvice {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebApiExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object exception(HttpServletRequest req, Exception e) {
        LOGGER.error("Internal server error intercepted", e);

        return null;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public void httpRequestMethodNotSupportedException(HttpServletRequest req, Exception e) {
        LOGGER.warn("Method not supported", e);
    }
    
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public void httpMediaTypeNotAcceptableException(HttpServletRequest req, Exception e) {
        LOGGER.warn("Media type not acceptable", e);
    }
    
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public void httpMediaTypeNotSupportedException(HttpServletRequest req, Exception e) {
        LOGGER.warn("Media type not supported", e);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public void httpMessageNotReadableException(HttpServletRequest req, Exception e) {
    }

    @ExceptionHandler(WebException.class)
    @ResponseBody
    public Object webException(HttpServletRequest req, HttpServletResponse res, WebException e) {
        if (e.getStatus() != null)
            res.setStatus(e.getStatus());
        else
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return e.getPayload();
    }

}
