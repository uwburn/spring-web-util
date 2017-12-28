package it.mgt.util.spring.web.jsonview;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

@ControllerAdvice
public class DynamicJsonViewAdvice extends AbstractMappingJacksonResponseBodyAdvice {

    private Map<String, Class<?>> viewsMap = new HashMap<>();
    private Class<?> defaultView;

    public DynamicJsonViewAdvice() {
    }

    public DynamicJsonViewAdvice addView(String name, Class<?> view) {
        viewsMap.put(name, view);
        return this;
    }

    public DynamicJsonViewAdvice removeView(String name) {
        viewsMap.remove(name);
        return this;
    }

    public DynamicJsonViewAdvice defaultView(Class<?> view) {
        defaultView = view;
        return this;
    }

    protected static Map<String, List<String>> splitQuery(URI uri) throws UnsupportedEncodingException {
        final Map<String, List<String>> params = new LinkedHashMap<>();
        String query = uri.getQuery();

        if (query == null) {
            return params;
        }

        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!params.containsKey(key)) {
                params.put(key, new LinkedList<>());
            }

            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            params.get(key).add(value);
        }

        return params;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return (super.supports(returnType, converterType) && returnType.getMethodAnnotation(DynamicJsonView.class) != null);
    }

    @Override
    protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType, MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            Class<?> jsonView = null;

            DynamicJsonView ann = returnType.getMethodAnnotation(DynamicJsonView.class);

            Map<String, List<String>> queryParams = splitQuery(request.getURI());
            List<String> viewName = queryParams.get("view");
            if (viewName != null && !viewName.isEmpty()) {
                jsonView = viewsMap.get(viewName.get(0));
            }

            if (jsonView == null) {
                jsonView = ann.defaultView();
            }

            if (jsonView == null) {
                jsonView = defaultView;
            }

            if (ann.allowedView().length > 0) {
                boolean allowed = false;
                for (Class<?> allowedView : ann.allowedView()) {
                    if (allowedView.equals(jsonView)) {
                        allowed = true;
                        break;
                    }
                }

                if (!allowed) {
                    jsonView = null;
                }
            }

            if (ann.forbiddenView().length > 0) {
                for (Class<?> forbiddenView : ann.forbiddenView()) {
                    if (forbiddenView.equals(jsonView)) {
                        jsonView = null;
                        break;
                    }
                }
            }

            if (jsonView != null) {
                bodyContainer.setSerializationView(jsonView);
            }
        } catch (Exception ignored) {
        }
    }

    public Map<String, Class<?>> getViewsMap() {
        return viewsMap;
    }

    public void setViewsMap(Map<String, Class<?>> viewsMap) {
        this.viewsMap = viewsMap;
    }

    public Class<?> getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(Class<?> defaultView) {
        this.defaultView = defaultView;
    }

}
