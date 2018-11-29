package it.mgt.util.spring.web.resolver;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseResolver implements HandlerMethodArgumentResolver {

    protected Map<String, List<String>> getParameters(NativeWebRequest nativeWebRequest, PathParam[] pathParams, QueryParam[] queryParams) {
        HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Map<String, String> pathVariables = (Map<String, String>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        Map<String, List<String>> params = new HashMap<>();

        for (PathParam pp : pathParams) {
            String value = pathVariables.get(pp.path());
            if (value != null) {
                List<String> list = new LinkedList<>();
                list.add(value);
                params.put(pp.name(), list);
            }
        }

        Map<String, String> queryParamsMapping = Arrays.stream(queryParams)
                .collect(Collectors.toMap(QueryParam::queryStringParam, QueryParam::jpaQueryParam));

        Map<String, String[]> queryParamsMap = nativeWebRequest.getParameterMap();
        queryParamsMap.entrySet().forEach((e) -> {
            List<String> list = params.get(e.getKey());
            if (list == null)
                list = new ArrayList<>();

            list.addAll(Arrays.asList(e.getValue()));

            String paramName = queryParamsMapping.get(e.getKey());
            if (paramName == null)
                paramName = e.getKey();

            params.put(paramName, list);
        });

        return params;
    }

}
