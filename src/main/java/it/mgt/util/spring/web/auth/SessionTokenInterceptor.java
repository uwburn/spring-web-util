package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.auth.AuthSession;
import it.mgt.util.spring.auth.AuthSvc;
import it.mgt.util.spring.auth.AuthUser;
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
    private String header = "session";
    private boolean extendSession = true;

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

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public boolean isExtendSession() {
        return extendSession;
    }

    public void setExtendSession(boolean extendSession) {
        this.extendSession = extendSession;
    }

    private Cookie getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            LOGGER.trace("No cookies found");
            return null;
        }

        Cookie cookie = null;
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                cookie = c;
                break;
            }
        }

        if (cookie == null) {
            LOGGER.trace("Cookie " + cookieName + " not found");
            return null;
        }

        return cookie;
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String headerValue = request.getHeader(header);

        if (headerValue == null) {
            LOGGER.trace("Header " + header + " not found");
        }

        return headerValue;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getAuthType() != null) {
            return true;
        }

        if (authSvc == null) {
            return true;
        }

        String token = null;
        // Try the cookie first
        Cookie cookie = getCookie(request);
        if (cookie != null) {
            token = cookie.getValue();
        }

        // If cookie had no token, then try the header
        if (token == null) {
            token = getTokenFromHeader(request);
        }

        if (token == null) {
            LOGGER.trace("No session token found");
            return true;
        }

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

        request.setAttribute(AuthAttributes.AUTH_TYPE, AUTH_TYPE);
        request.setAttribute(AuthAttributes.AUTH_USER, authUser);

        if (extendSession) {
            authSvc.touchSession(authSession);

            if (cookie != null) {
                cookie.setPath(getCookiePath());
                cookie.setHttpOnly(isCookieHttpOnly());
                cookie.setMaxAge(authSession.getExpirySeconds());
                response.addCookie(cookie);
            }
        }

        return true;
    }
}
