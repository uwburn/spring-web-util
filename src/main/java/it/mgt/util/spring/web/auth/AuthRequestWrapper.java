package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.auth.AuthRole;
import it.mgt.util.spring.auth.AuthUser;
import it.mgt.util.spring.auth.PrincipalWrapper;

import javax.servlet.ServletException;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

public class AuthRequestWrapper extends ServletRequestWrapper implements HttpServletRequest {

    protected HttpServletRequest originalRequest;
    protected String authType;
    protected AuthUser authUser;

    public AuthRequestWrapper(HttpServletRequest request) {
        super(request);

        this.originalRequest = request;
    }

    /*public AuthRequestWrapper(HttpServletRequest request, String authType, AuthUser authUser) {
        this(request);

        setAuth(authType, authUser);
    }*/

    @Override
    public String getAuthType() {
        return authType;
    }

    public final void setAuth(String authType, AuthUser authUser) {
        if (this.authType == null) {
            this.authType = authType;
        }
        else {
            this.authType += ", " + authType;
        }
        this.authUser = authUser;

        // Ensure data is fetched immediately, not when we mightn't have a valid persistence context
        authUser.getUsername();
        authUser.authRoles();
    }

    public AuthUser getAuthUser() {
        return authUser;
    }

    public Set<AuthRole> getAuthRoles() {
        return authUser != null ? authUser.authRoles() : Collections.emptySet();
    }

    public Set<String> getAuthOperations() {
        return authUser != null ? authUser.authOperations() : Collections.emptySet();
    }

    public HttpServletRequest unwrap() {
        return originalRequest;
    }

    @Override
    public Cookie[] getCookies() {
        return originalRequest.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return originalRequest.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return originalRequest.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return originalRequest.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return originalRequest.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name) {
        return originalRequest.getIntHeader(name);
    }

    @Override
    public String getMethod() {
        return originalRequest.getMethod();
    }

    @Override
    public String getPathInfo() {
        return originalRequest.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return originalRequest.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return originalRequest.getContextPath();
    }

    @Override
    public String getQueryString() {
        return originalRequest.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return originalRequest.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        if (authUser == null)
            return false;

        return authUser.authRoles()
                .stream()
                .anyMatch(r -> r.getName().equals(role));
    }

    @Override
    public Principal getUserPrincipal() {
        if (authUser == null)
            return null;

        return new PrincipalWrapper(authUser);
    }

    @Override
    public String getRequestedSessionId() {
        return originalRequest.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return originalRequest.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return originalRequest.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return originalRequest.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return originalRequest.getSession(create);
    }

    @Override
    public HttpSession getSession() {
        return originalRequest.getSession();
    }

    @Override
    public String changeSessionId() {
        return originalRequest.changeSessionId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return originalRequest.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return originalRequest.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return originalRequest.isRequestedSessionIdFromURL();
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return originalRequest.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return originalRequest.authenticate(response);
    }

    @Override
    public void login(String username, String password) throws ServletException {
        originalRequest.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        originalRequest.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return originalRequest.getParts();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return originalRequest.getPart(name);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return originalRequest.upgrade(handlerClass);
    }
    
    

}
