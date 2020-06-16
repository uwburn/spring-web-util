package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.auth.AuthUser;
import it.mgt.util.spring.web.exception.BadRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class AuthUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(AuthUserInj.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        AuthUserInj ann = methodParameter.getParameterAnnotation(AuthUserInj.class);

        AuthUser authUser = null;
        try {
            authUser = (AuthUser) httpServletRequest.getAttribute(AuthAttributes.AUTH_USER);
        }
        catch (ClassCastException ignored) { }

        if (authUser == null && ann.required())
            throw new BadRequestException();

        return authUser;
    }

}
