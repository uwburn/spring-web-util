package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.auth.AuthUser;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
	
	private enum AuthStatus {
		OK, FORBIDDEN, UNAUTHORIZED
	}

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        AuthStatus authStatus = denyAllMethod(handlerMethod);
        if (!applyStatus(httpServletResponse, authStatus))
        	return false;
        
        authStatus = rolesAllowedMethod(httpServletRequest, handlerMethod);
        if (!applyStatus(httpServletResponse, authStatus))
        	return false;

		authStatus = permitAllMethod(httpServletRequest, handlerMethod);
		if (!applyStatus(httpServletResponse, authStatus))
			return false;

		authStatus = denyAllType(handlerMethod);
		if (!applyStatus(httpServletResponse, authStatus))
			return false;

        authStatus = rolesAllowedType(httpServletRequest, handlerMethod);
        if (!applyStatus(httpServletResponse, authStatus))
        	return false;

		authStatus = permitAllType(httpServletRequest, handlerMethod);
		if (!applyStatus(httpServletResponse, authStatus))
			return false;

        return true;
    }
    
    private AuthStatus denyAllMethod(HandlerMethod handlerMethod) {
    	DenyAll denyAll = handlerMethod.getMethod().getAnnotation(DenyAll.class);
        if (denyAll == null)
        	return AuthStatus.OK;
        
        return AuthStatus.FORBIDDEN;
    }
    
    private AuthStatus rolesAllowedMethod(HttpServletRequest httpServletRequest, HandlerMethod handlerMethod) {
    	RolesAllowed rolesAllowed = handlerMethod.getMethod().getAnnotation(RolesAllowed.class);
    	
    	return rolesAllowed(httpServletRequest, rolesAllowed);
    }

	private AuthStatus permitAllMethod(HttpServletRequest httpServletRequest, HandlerMethod handlerMethod) {
		PermitAll permitAll = handlerMethod.getMethod().getAnnotation(PermitAll.class);

		return permitAll(httpServletRequest, permitAll);
	}

	private AuthStatus denyAllType(HandlerMethod handlerMethod) {
		Class<?> type = handlerMethod.getBeanType();
		DenyAll denyAll = type.getAnnotation(DenyAll.class);
		if (denyAll == null)
			return AuthStatus.OK;

		return AuthStatus.FORBIDDEN;
	}

    private AuthStatus rolesAllowedType(HttpServletRequest httpServletRequest, HandlerMethod handlerMethod) {
		Class<?> type = handlerMethod.getBeanType();
		RolesAllowed rolesAllowed = type.getAnnotation(RolesAllowed.class);

		return rolesAllowed(httpServletRequest, rolesAllowed);
	}

	private AuthStatus permitAllType(HttpServletRequest httpServletRequest, HandlerMethod handlerMethod) {
		Class<?> type = handlerMethod.getBeanType();
		PermitAll permitAll = type.getAnnotation(PermitAll.class);

		return permitAll(httpServletRequest, permitAll);
	}

    private AuthStatus rolesAllowed(HttpServletRequest httpServletRequest, RolesAllowed rolesAllowed) {
    	if (rolesAllowed == null || rolesAllowed.value().length == 0)
    		return AuthStatus.OK;

    	boolean hasPrincipal = getPrincipal(httpServletRequest) != null;
    	
    	if (!hasPrincipal)
    		return AuthStatus.UNAUTHORIZED;
    	
		for (String role : rolesAllowed.value())
			if (isUserInRole(httpServletRequest, role))
				return AuthStatus.OK;
		
		return AuthStatus.FORBIDDEN;
    }

	private AuthStatus permitAll(HttpServletRequest httpServletRequest, PermitAll permitAll) {
		if (permitAll == null)
			return AuthStatus.OK;

		boolean hasPrincipal = getPrincipal(httpServletRequest) != null;

		if (!hasPrincipal)
			return AuthStatus.UNAUTHORIZED;

		return AuthStatus.OK;
	}
    
    private boolean applyStatus(HttpServletResponse httpServletResponse, AuthStatus authStatus) {
    	switch (authStatus) {
		case UNAUTHORIZED:
			httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		case FORBIDDEN:
			httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return false;
		case OK:
		default:
			return true;
		}
    }

    private AuthUser getPrincipal(HttpServletRequest httpServletRequest) {
		try {
			return (AuthUser) httpServletRequest.getAttribute(AuthAttributes.AUTH_USER);
		}
		catch (ClassCastException ignored) {
			return null;
		}
	}

	private boolean isUserInRole(HttpServletRequest httpServletRequest, String role) {
		AuthUser authUser = getPrincipal(httpServletRequest);

		if (authUser == null)
			return false;

		return authUser.authRoles()
				.stream()
				.anyMatch(r -> r.getName().equals(role));
	}
	
}
