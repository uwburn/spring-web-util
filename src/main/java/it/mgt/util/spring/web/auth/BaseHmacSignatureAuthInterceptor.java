package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.web.util.RequestURL;
import it.mgt.util.spring.auth.AuthUser;
import it.mgt.util.spring.auth.AuthSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public abstract class BaseHmacSignatureAuthInterceptor extends HandlerInterceptorAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseHmacSignatureAuthInterceptor.class);

    @Autowired(required = false)
    AuthSvc authUserService;

    private long maxTimeSkew = 15 * 60 * 1000;

    private long getMaxTimeSkew() {
        return maxTimeSkew;
    }

    public void setMaxTimeSkew(long maxTimeSkew) {
        this.maxTimeSkew = maxTimeSkew;
    }

    protected abstract String getAuthType();

    protected abstract String hmac(String input, String key);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getAuthType() != null) {
            return true;
        }

        if (!(request instanceof AuthRequestWrapper)) {
            return true;
        }

        if (authUserService == null) {
            return true;
        }

        AuthRequestWrapper authRequestWrapper = (AuthRequestWrapper) request;

        String header = authRequestWrapper.getHeader("Authorization");

        if (header == null) {
            LOGGER.trace("No authorization header found");
            return true;
        }

        if (header.indexOf(getAuthType()) != 0) {
            LOGGER.trace(header + " not matching with " + getAuthType());
            return true;
        }

        String strReceivedTime = authRequestWrapper.getHeader("Time");
        if (strReceivedTime == null) {
            LOGGER.trace("Missing time header");
            return true;
        }

        String username = authRequestWrapper.getHeader("Username");
        if (username == null) {
            LOGGER.trace("Missing username header");
            return true;
        }

        String receivedSignature = authRequestWrapper.getHeader("Signature");
        if (receivedSignature == null) {
            LOGGER.trace("Missing signature header");
            return true;
        }

        long computedTime = new Date().getTime();
        long receivedTime;
        try {
            receivedTime = Long.parseLong(strReceivedTime);
        } catch (NumberFormatException nfe) {
            LOGGER.trace("Unparsable time header");
            return true;
        }

        if (computedTime - receivedTime > getMaxTimeSkew()) {
            LOGGER.trace("Received time (" + receivedTime + ") exceeding maximum allowed skew (" + computedTime + " - " + getMaxTimeSkew() + ")");
            return true;
        }

        AuthUser authUser = authUserService.getAuthUser(username);
        if (authUser == null) {
            LOGGER.trace("User " + username + " not found");
            return true;
        }

        String computedStringToSign = request.getMethod() + ":" + RequestURL.getCompleteResource(request) + ":" + receivedTime;
        String computedSignature = hmac(computedStringToSign, authUser.getPassword());
        if (!receivedSignature.equals(computedSignature)) {
            LOGGER.trace("Received signature (" + receivedSignature + ") not matching with computed signature (" + computedSignature + ")");
            return true;
        }

        authRequestWrapper.setAuth(getAuthType(), authUser);

        return true;
    }
}
