package it.mgt.util.spring.web.bean;

import it.mgt.util.spring.web.exception.BadRequestException;
import it.mgt.util.spring.web.exception.ForbiddenException;
import it.mgt.util.spring.web.exception.NotFoundException;
import it.mgt.util.spring.web.resolver.BaseResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BeanMethodResolver extends BaseResolver {

    @Autowired
    ApplicationContext applicationContext;
    
    protected String methodParamName = "method";

    public String getMethodParamName() {
        return methodParamName;
    }

    public void setMethodParamName(String methodParamName) {
        this.methodParamName = methodParamName;
    }
    
    protected Object buildParams(List<String> values, Class<?> type) {
        if (values == null)
            return null;
        
        switch (values.size()) {
            case 0:
                return null;
            case 1:
                return parseParam(values.get(0), type);
            default:
                return values.stream().map(v -> parseParam(v, type));
        }
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(BeanMethodInj.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        BeanMethodInj ann = methodParameter.getParameterAnnotation(BeanMethodInj.class);
        Map<String, List<String>> params = getParameters(nativeWebRequest, ann.pathParams(), ann.queryParams());

        Object bean = applicationContext.getBean(ann.bean());

        String methodName = ann.defaultMethod();
        List<String> methodParamValue = params.get(methodParamName);
        if (methodParamValue != null && methodParamValue.size() > 0)
            methodName = methodParamValue.get(0);
            
        if (methodName == null || methodName.length() == 0)
            throw new BadRequestException();
            
        if (ann.allowedMethod().length > 0) {
            boolean allowed = false;
            for (String allowedQuery : ann.allowedMethod()) {
                if (allowedQuery.equals(methodName)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed)
                throw new ForbiddenException();
        }
            
        if (ann.forbiddenMethod().length > 0) {
            for (String forbiddenQuery : ann.forbiddenMethod())
                if (forbiddenQuery.equals(methodName))
                    throw new ForbiddenException();
        }
        
        params.remove(methodParamName);

        String finalMethodName = methodName;
        List<Method> methods = Arrays.stream(ann.bean().getMethods())
                .filter(m -> m.getName().equals(finalMethodName))
                .filter(m -> m.getParameters().length == params.size())
                .filter(m -> {
                    for (Parameter p : m.getParameters()) {
                        if (!params.containsKey(p.getName()))
                            return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        Object result;
        Method method;
        try {
            if (methods.size() > 1)
                throw new IllegalArgumentException();

            method = methods.stream()
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);

            if (!method.getReturnType().isAssignableFrom(methodParameter.getParameterType()))
                throw new BadRequestException();

                Object[] arguments = Arrays.stream(method.getParameters())
                        .map(m -> buildParams(params.get(m.getName()), m.getType()))
                        .toArray();

                result = method.invoke(bean, arguments);
        }
        catch(IllegalArgumentException ignored) {
            throw new BadRequestException();
        }

        if (!Collection.class.isAssignableFrom(method.getReturnType())) {
            if (result == null && ann.notFoundException())
                throw new NotFoundException();
        }

        return result;
    }

}
