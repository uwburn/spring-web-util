package it.mgt.util.spring.web.jpa;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import it.mgt.util.spring.web.exception.BadRequestException;
import it.mgt.util.spring.web.exception.ForbiddenException;
import it.mgt.util.spring.web.exception.NotFoundException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class JpaResolver implements HandlerMethodArgumentResolver {

    @PersistenceContext
    protected EntityManager em;
    
    protected String queryParamName = "query";
    protected String pageParamName = "page";
    protected String pageSizeParamName = "pageSize";

    public String getQueryParamName() {
        return queryParamName;
    }

    public void setQueryParamName(String queryParamName) {
        this.queryParamName = queryParamName;
    }
    
    protected Object resolveSingleByPrimaryKey(String primaryKeyParam, Map<String, List<String>> params, Class<?> type, boolean notFound) {
        Map<String, Class<?>> hints = getHints(type);
        
        List<String> primaryKeyValues = params.get(primaryKeyParam);
        
        Object primaryKey = buildParams(primaryKeyParam, primaryKeyValues, hints);
        Object result = em.find(type, primaryKey);

        if (result == null && notFound)
            throw new NotFoundException();

        return result;
    }
    
    protected List resolveListByPrimaryKey(String primaryKeyParam, Map<String, List<String>> params, Class<?> type) {
        Object result = resolveSingleByPrimaryKey(primaryKeyParam, params, type, false);
        
        if (result == null)
            return Collections.EMPTY_LIST;
        else
            return Collections.singletonList(result);
    }

    protected Object resolveSingleByQuery(String queryName, Map<String, List<String>> params, Class<?> type, boolean notFound) {
        Object result = resolveListByQuery(queryName, params, type)
                .stream()
                .findFirst()
                .orElse(null);

        if (result == null && notFound)
            throw new NotFoundException();

        return result;
    }

    protected List resolveListByQuery(String queryName, Map<String, List<String>> params, Class<?> type) {
        Map<String, Class<?>> hints = getHints(type);

        Query query = em.createNamedQuery(queryName);
        
        Integer page = (Integer) buildParams(pageParamName, params.get(pageParamName), hints);
        Integer pageSize = (Integer) buildParams(pageSizeParamName, params.get(pageSizeParamName), hints);
        params.remove(pageParamName);
        params.remove(pageSizeParamName);
        if (page != null && pageSize != null) {
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
        }
        
        params.entrySet().forEach((e) -> {
            query.setParameter(e.getKey(), buildParams(e.getKey(), e.getValue(), hints));
        });

        return query.getResultList();
    }
    
    protected Object buildParams(String name, List<String> values, Map<String, Class<?>> hints) {
        if (values == null)
            return null;
        
        switch (values.size()) {
            case 0:
                return null;
            case 1:
                return buildParam(name, values.get(0), hints);
            default:
                return values.stream().map(v -> buildParam(name, v, hints));
        }
    }
    
    protected Object buildParam(String name, String value, Map<String, Class<?>> hints) {
        Object result = value;
        
        Class<?> hint = hints.get(name);
        if (hint != null) {            
            if (Boolean.class.isAssignableFrom(hint)) {
                result = Boolean.parseBoolean(value);
            }
            else if (Byte.class.isAssignableFrom(hint)) {
                result = Byte.parseByte(value);
            }
            else if (Short.class.isAssignableFrom(hint)) {
                result = Short.parseShort(value);
            }
            else if (Integer.class.isAssignableFrom(hint)) {
                result = Integer.parseInt(value);
            }
            else if (Long.class.isAssignableFrom(hint)) {
                result = Long.parseLong(value);
            }
            else if (Float.class.isAssignableFrom(hint)) {
                result = Float.parseFloat(value);
            }
            else if (Double.class.isAssignableFrom(hint)) {
                result = Double.parseDouble(value);
            }
            else if (BigInteger.class.isAssignableFrom(hint)) {
                result = new BigInteger(value);
            }
            else if (BigDecimal.class.isAssignableFrom(hint)) {
                result = new BigDecimal(value);
            }
            else if (Date.class.isAssignableFrom(hint)) {
                result = new Date(Long.parseLong(value));
            }
            else if (Enum.class.isAssignableFrom(hint))  {
                try {
                    Method valueOf = hint.getMethod("valueOf", String.class);
                    result = valueOf.invoke(null, value);
                }
                catch (Exception ignored) {
                }
            }
        }
        
        return result;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(JpaInj.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        JpaInj ann = methodParameter.getParameterAnnotation(JpaInj.class);
        Map<String, List<String>> params = getParameters(nativeWebRequest, ann);
        
        if (ann.query().length() > 0 && ann.primaryKey().length() > 0)
            throw new UnsupportedOperationException("query and primaryKey parameters are mutually exclusive");
        
        String queryName;
        if (ann.primaryKey().length() > 0) {
            queryName = null;
        }
        else if (ann.query().length() > 0) {
            queryName = ann.query();
        }
        else {
            queryName = ann.defaultQuery();
            List<String> queryParamValue = params.get(queryParamName);
            if (queryParamValue != null && queryParamValue.size() > 0)
                queryName = queryParamValue.get(0);
            
            if (queryName == null || queryName.length() == 0)
                throw new BadRequestException();
            
            if (ann.allowedQuery().length > 0) {
                boolean allowed = false;
                for (String allowedQuery : ann.allowedQuery()) {
                    if (allowedQuery.equals(queryName)) {
                        allowed = true;
                        break;
                    }
                }
                               
                if (!allowed)
                    throw new ForbiddenException();
            }
            
            if (ann.forbiddenQuery().length > 0) {
                for (String forbiddenQuery : ann.forbiddenQuery())
                    if (forbiddenQuery.equals(queryName))
                        throw new ForbiddenException();
            }
        }
        
        params.remove(queryParamName);

        try {
            if (List.class.isAssignableFrom(methodParameter.getParameterType())) {
                ParameterizedType type = (ParameterizedType) methodParameter.getGenericParameterType();
                Class<?> clazz = Class.forName(type.getActualTypeArguments()[0].getTypeName());

                if (ann.primaryKey().length() > 0)
                    return resolveListByPrimaryKey(ann.primaryKey(), params, clazz);
                else
                    return resolveListByQuery(queryName, params, clazz);
            }
            else if (Collection.class.isAssignableFrom(methodParameter.getParameterType())) {
                throw new UnsupportedOperationException(methodParameter.getParameterType().getName() + "is not a supported collection");
            }
            else if (ann.primaryKey().length() > 0) {
                return resolveSingleByPrimaryKey(ann.primaryKey(), params, methodParameter.getParameterType(), ann.notFoundException());
            }
            else {
                return resolveSingleByQuery(queryName, params, methodParameter.getParameterType(), ann.notFoundException());
            }
        }
        catch(IllegalArgumentException ignored) {
            throw new BadRequestException();
        }
    }
    
    protected Map<String, Class<?>> getHints(Class<?> type) {
        Map<String, Class<?>> hints = new HashMap<>();
        ParamHints paramHints = type.getAnnotation(ParamHints.class);
        if (paramHints != null) {
            ParamHint[] hint = paramHints.value();
            for (ParamHint ph : hint)
                hints.put(ph.value(), ph.type());
        }
        
        hints.put(pageParamName, Integer.class);
        hints.put(pageSizeParamName, Integer.class);
        
        return hints;
    }

    protected Map<String, List<String>> getParameters(NativeWebRequest nativeWebRequest, JpaInj ann) {
        HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Map<String, String> pathVariables = (Map<String, String>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        Map<String, List<String>> params = new HashMap<>();

        for (PathParam pp : ann.pathParams()) {
            String value = pathVariables.get(pp.path());
            if (value != null) {
                List<String> list = new LinkedList<>();
                list.add(value);
                params.put(pp.name(), list);
            }
        }
        
        Map<String, String> queryParamsMapping = Arrays.stream(ann.queryParams())
                .collect(Collectors.toMap(QueryParam::queryStringParam, QueryParam::jpaQueryParam));
                
        Map<String, String[]> queryParams = nativeWebRequest.getParameterMap();
        queryParams.entrySet().forEach((e) -> {
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
