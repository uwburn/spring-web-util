package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.auth.AuthSession;
import it.mgt.util.spring.auth.AuthUser;
import it.mgt.util.spring.auth.AuthSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionTokenInterceptor extends HandlerInterceptorAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(SessionTokenInterceptor.class);

    private final static String AUTH_TYPE = "Session token";

    private String cookieName = "AuthSession";
    private String cookiePath = "/";
    private boolean cookieHttpOnly = true;

    @Autowired(required = false)
    AuthSvc authSvc;

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public boolean isCookieHttpOnly() {
        return cookieHttpOnly;
    }

    public void setCookieHttpOnly(boolean cookieHttpOnly) {
        this.cookieHttpOnly = cookieHttpOnly;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getAuthType() != null) {
            return true;
        }

        if (!(request instanceof AuthRequestWrapper)) {
            return true;
        }

        if (authSvc == null) {
            return true;
        }

        AuthRequestWrapper authRequestWrapper = (AuthRequestWrapper) request;

        Cookie[] cookies = authRequestWrapper.getCookies();

        if (cookies == null) {
            LOGGER.trace("No cookies found");
            return true;
        }

        Cookie cookie = null;
        for (int i = 0; i < cookies.length; i++) {
            cookie = cookies[i];

            if (cookieName.equals(cookie.getName())) {
                break;
            }
        }

        if (cookie == null) {
            LOGGER.trace("Cookie " + cookieName + " not found");
            return true;
        }

        String token = cookie.getValue();

        AuthSession authSession = authSvc.getValidAuthSession(token);

        if (authSession == null) {
            LOGGER.trace("No valid session found for token " + token);
            return true;
        }

        AuthUser authUser = authSession.authUser();

        if (authUser == null) {
            LOGGER.trace("Unable to resolve user associated with the session");
            return true;
        }

        authRequestWrapper.setAuth(AUTH_TYPE, authUser);

        authSvc.touchSession(authSession);
        cookie.setPath(getCookiePath());
        cookie.setHttpOnly(isCookieHttpOnly());
        cookie.setMaxAge(authSession.getExpirySeconds());
        response.addCookie(cookie);

        return true;
    }
}
