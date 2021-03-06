package it.mgt.util.spring.web.jpa;

import it.mgt.util.jpa.JpaUtils;
import it.mgt.util.jpa.ParamHint;
import it.mgt.util.jpa.ParamHints;
import it.mgt.util.spring.web.exception.BadRequestException;
import it.mgt.util.spring.web.exception.ForbiddenException;
import it.mgt.util.spring.web.exception.NotFoundException;
import it.mgt.util.spring.web.resolver.BaseResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class JpaResolver extends BaseResolver {

    private final static Logger LOGGER = LoggerFactory.getLogger(JpaResolver.class);

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

    public String getPageParamName() {
        return pageParamName;
    }

    public void setPageParamName(String pageParamName) {
        this.pageParamName = pageParamName;
    }

    public String getPageSizeParamName() {
        return pageSizeParamName;
    }

    public void setPageSizeParamName(String pageSizeParamName) {
        this.pageSizeParamName = pageSizeParamName;
    }

    protected Object resolveSingleByPrimaryKey(String primaryKeyParam, Map<String, List<String>> params, Class<?> type, Class<?> hintSource, boolean notFound) {
        Map<String, Class<?>> hints = getHints(hintSource);
        hints.put(primaryKeyParam, JpaUtils.getIdClass(type));
        
        List<String> primaryKeyValues = params.get(primaryKeyParam);
        
        Object primaryKey = buildParams(primaryKeyParam, primaryKeyValues, hints);
        Object result = em.find(type, primaryKey);

        if (result == null && notFound) {
            LOGGER.debug("Primary-key find produced null result, but non-null value was expected");
            throw new NotFoundException();
        }

        return result;
    }
    
    protected List resolveListByPrimaryKey(String primaryKeyParam, Map<String, List<String>> params, Class<?> type, Class<?> hintSource) {
        List<String> primaryKeys = params.get(primaryKeyParam);

        return primaryKeys.stream()
                .map(pk -> {
                    Map<String, List<String>> p = Collections.singletonMap(primaryKeyParam, Collections.singletonList(pk));

                    return resolveSingleByPrimaryKey(primaryKeyParam, p, type, hintSource, false);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected Object resolveSingleByQuery(String queryName, Map<String, List<String>> params, Class<?> type, Class<?> hintSource, boolean notFound) {
        Object result = resolveListByQuery(queryName, params, type, hintSource)
                .stream()
                .findFirst()
                .orElse(null);

        if (result == null && notFound) {
            LOGGER.debug("Query produced null result, but non-null value was expected");
            throw new NotFoundException();
        }

        return result;
    }

    protected List resolveListByQuery(String queryName, Map<String, List<String>> params, Class<?> type, Class<?> hintSource) {
        Map<String, Class<?>> hints = getHints(hintSource);

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
                return values.stream().map(v -> buildParam(name, v, hints)).collect(Collectors.toList());
        }
    }
    
    protected Object buildParam(String name, String value, Map<String, Class<?>> hints) {
        Object result = value;
        
        Class<?> hint = hints.get(name);
        return JpaUtils.parseParam(value, hint);
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(JpaInj.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        JpaInj ann = methodParameter.getParameterAnnotation(JpaInj.class);
        Map<String, List<String>> params = getParameters(nativeWebRequest, ann.pathParams(), ann.queryParams());
        
        if (ann.query().length() > 0 && ann.primaryKey().length() > 0) {
            LOGGER.error("Parameters query and primaryKey are mutually exclusive");
            throw new UnsupportedOperationException("query and primaryKey parameters are mutually exclusive");
        }
        
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
            
            if (queryName == null || queryName.length() == 0) {
                LOGGER.warn("Query name not specified");
                throw new BadRequestException();
            }
            
            if (ann.allowedQuery().length > 0) {
                boolean allowed = false;
                for (String allowedQuery : ann.allowedQuery()) {
                    if (allowedQuery.equals(queryName)) {
                        allowed = true;
                        break;
                    }
                }
                               
                if (!allowed) {
                    LOGGER.warn("Query " + queryName + " is not in allowed list");
                    throw new ForbiddenException();
                }
            }
            
            if (ann.forbiddenQuery().length > 0) {
                for (String forbiddenQuery : ann.forbiddenQuery()) {
                    if (forbiddenQuery.equals(queryName)) {
                        LOGGER.warn("Query " + queryName + " is in forbidden list");
                        throw new ForbiddenException();
                    }
                }
            }
        }
        
        params.remove(queryParamName);

        Class<?> hintSource = null;
        if (!ann.hintSource().equals(Class.class))
            hintSource = ann.hintSource();

        try {
            if (List.class.isAssignableFrom(methodParameter.getParameterType())) {
                ParameterizedType type = (ParameterizedType) methodParameter.getGenericParameterType();
                Class<?> clazz = Class.forName(type.getActualTypeArguments()[0].getTypeName());

                if (hintSource == null)
                    hintSource = clazz;

                if (ann.primaryKey().length() > 0)
                    return resolveListByPrimaryKey(ann.primaryKey(), params, clazz, hintSource);
                else
                    return resolveListByQuery(queryName, params, clazz, hintSource);
            }
            else if (Collection.class.isAssignableFrom(methodParameter.getParameterType())) {
                LOGGER.error(methodParameter.getParameterType().getName() + "is not a supported collection");
                throw new UnsupportedOperationException(methodParameter.getParameterType().getName() + "is not a supported collection");
            }
            else if (ann.primaryKey().length() > 0) {
                if (hintSource == null)
                    hintSource = methodParameter.getParameterType();

                return resolveSingleByPrimaryKey(ann.primaryKey(), params, methodParameter.getParameterType(), hintSource, ann.notFoundException());
            }
            else {
                if (hintSource == null)
                    hintSource = methodParameter.getParameterType();

                return resolveSingleByQuery(queryName, params, methodParameter.getParameterType(), hintSource, ann.notFoundException());
            }
        }
        catch(IllegalArgumentException e) {
            LOGGER.warn("Query invocation failed", e);
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

}
