package it.mgt.util.spring.web.util;

import javax.servlet.http.HttpServletRequest;

public abstract class RequestURL {

    public static String getCompleteURL(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL;
        } else {
            return requestURL + '?' + queryString;
        }
    }

    public static String getCompleteResource(HttpServletRequest request) {
        String resource = request.getPathInfo();

        String queryString = request.getQueryString();

        if (queryString == null) {
            return resource;
        } else {
            return resource + '?' + queryString;
        }
    }

}
