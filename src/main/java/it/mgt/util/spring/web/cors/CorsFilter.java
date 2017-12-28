package it.mgt.util.spring.web.cors;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class CorsFilter extends OncePerRequestFilter {

	private boolean allowWithoutOrigin = true;
	private boolean onlyAllowedOrigins = false;
	private Set<String> allowedOrigins = new HashSet<>();
	private String nullOrigin = "null";

	public boolean isAllowWithoutOrigin() {
		return allowWithoutOrigin;
	}

	public void setAllowWithoutOrigin(boolean allowWithoutOrigin) {
		this.allowWithoutOrigin = allowWithoutOrigin;
	}

	public boolean isOnlyAllowedOrigins() {
		return onlyAllowedOrigins;
	}

	public void setOnlyAllowedOrigins(boolean onlyAllowedOrigins) {
		this.onlyAllowedOrigins = onlyAllowedOrigins;
	}

	public Set<String> getAllowedOrigins() {
		return allowedOrigins;
	}

	public void setAllowedOrigins(Set<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	public String getNullOrigin() {
		return nullOrigin;
	}

	public void setNullOrigin(String nullOrigin) {
		this.nullOrigin = nullOrigin;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("origin");
        if (origin != null && !nullOrigin.equals(origin)) {
			if (!onlyAllowedOrigins || allowedOrigins.contains(origin))
            	response.addHeader("Access-Control-Allow-Origin", origin);
        }
        else {
			if (allowWithoutOrigin) {
				if (origin == null)
					response.addHeader("Access-Control-Allow-Origin", "*");
				else
					response.addHeader("Access-Control-Allow-Origin", origin);
			}
        }

        response.addHeader("Access-Control-Allow-Credentials", "true");

		String reqHead = request.getHeader("Access-Control-Request-Headers");
		String reqMethod = request.getHeader("Access-Control-Request-Method");
		
		if (reqHead != null)
			response.addHeader("Access-Control-Allow-Headers", reqHead);
		
		if (reqMethod != null)
			response.addHeader("Access-Control-Allow-Methods", reqMethod);
		
		if ("OPTIONS".equals(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		
		filterChain.doFilter(request, response);
	}

}
