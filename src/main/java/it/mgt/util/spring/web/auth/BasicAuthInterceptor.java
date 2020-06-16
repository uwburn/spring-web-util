package it.mgt.util.spring.web.auth;

import it.mgt.util.spring.auth.AuthUser;
import it.mgt.util.spring.auth.AuthSvc;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.Base64;

public class BasicAuthInterceptor extends HandlerInterceptorAdapter {

	private final static Logger LOGGER = LoggerFactory.getLogger(BasicAuthInterceptor.class);
	
	private final static String AUTH_TYPE = "Basic";
	
	@Autowired(required = false)
	AuthSvc authUserService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (request.getAuthType() != null)
			return true;

		if (authUserService == null)
			return true;

		String header = request.getHeader("Authorization");

		if (header == null) {
			LOGGER.trace("No authorization header found");
			return true;
		}

		if (header.indexOf(AUTH_TYPE) != 0) {
			LOGGER.trace(header + " not matching with " + AUTH_TYPE);
			return true;
		}

		String base64 = header.substring(AUTH_TYPE.length() + 1);
		String plain = new String(Base64.getDecoder().decode(base64));
		String[] parts = plain.split(":", 2);

		if (parts.length != 2) {
			LOGGER.trace("Unparsable authorization header");
			return true;
		}

		AuthUser authUser = authUserService.getAuthUser(parts[0], parts[1]);
		if (authUser == null) {
			LOGGER.trace("User " + parts[0] + " not found or password not matching");
			return true;
		}

		request.setAttribute(AuthAttributes.AUTH_TYPE, AUTH_TYPE);
		request.setAttribute(AuthAttributes.AUTH_USER, authUser);

		return true;
	}
}
