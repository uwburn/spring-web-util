package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.auth.AuthUser;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import it.mgt.util.spring.web.exception.ForbiddenException;
import it.mgt.util.spring.web.exception.UnauthorizedException;

public class RequiredOperationInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod && request instanceof AuthRequestWrapper) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AuthRequestWrapper authRequestWrapper = (AuthRequestWrapper) request;

            RequiredOperation ann = handlerMethod.getMethod().getAnnotation(RequiredOperation.class);
            if (ann != null) {
                if (authRequestWrapper.getAuthType() == null) {
                    throw new UnauthorizedException();
                }
                
                String requiredOperation = ann.value();
                Collection<String> authOperations = authRequestWrapper.getAuthOperations();

                if (!authOperations.contains(requiredOperation)) {
                    throw new ForbiddenException();
                }
            }
        }

        return super.preHandle(request, response, handler);
    }
}
