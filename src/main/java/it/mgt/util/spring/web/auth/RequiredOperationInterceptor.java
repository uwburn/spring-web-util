package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.web.exception.ForbiddenException;
import it.mgt.util.spring.web.exception.UnauthorizedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class RequiredOperationInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod && request instanceof AuthRequestWrapper) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequiredOperation ann = handlerMethod.getMethod().getAnnotation(RequiredOperation.class);
            if (ann != null) {
                AuthRequestWrapper authRequestWrapper = null;
                try {
                    authRequestWrapper = AuthRequestWrapper.extract(request);
                }
                catch (IllegalArgumentException ignored) { }

                if (authRequestWrapper == null || authRequestWrapper.getAuthType() == null) {
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
