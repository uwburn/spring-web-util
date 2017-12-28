package it.mgt.util.spring.web.controller;

import it.mgt.util.spring.repository.BaseRepository;
import it.mgt.util.spring.web.exception.ForbiddenException;
import it.mgt.util.spring.web.exception.NotFoundException;
import it.mgt.util.spring.web.exception.UnauthorizedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseReadCntrl<T, K> {

    private final Map<RequestMethod, Boolean> denyAll = new HashMap<>();
    private final Map<RequestMethod, String[]> rolesAllowed = new HashMap<>();
    private final Map<RequestMethod, Boolean> allowAll = new HashMap<>();

    public BaseReadCntrl() {
        denyAll.put(RequestMethod.GET, false);
        denyAll.put(RequestMethod.POST, false);
        denyAll.put(RequestMethod.PUT, false);
        denyAll.put(RequestMethod.PATCH, false);
        denyAll.put(RequestMethod.DELETE, false);

        rolesAllowed.put(RequestMethod.GET, null);
        rolesAllowed.put(RequestMethod.POST, null);
        rolesAllowed.put(RequestMethod.PUT, null);
        rolesAllowed.put(RequestMethod.PATCH, null);
        rolesAllowed.put(RequestMethod.DELETE, null);

        allowAll.put(RequestMethod.GET, true);
        allowAll.put(RequestMethod.POST, true);
        allowAll.put(RequestMethod.PUT, true);
        allowAll.put(RequestMethod.PATCH, true);
        allowAll.put(RequestMethod.DELETE, true);

        initPermissions();
    }

    protected void initPermissions() {
        return;
    }

    protected void allowAll(RequestMethod method, boolean value) {
        allowAll.put(method, value);
    }

    protected void allowAll(boolean value) {
        allowAll.put(RequestMethod.GET, value);
        allowAll.put(RequestMethod.POST, value);
        allowAll.put(RequestMethod.PUT, value);
        allowAll.put(RequestMethod.PATCH, value);
        allowAll.put(RequestMethod.DELETE, value);
    }

    protected void rolesAllowed(RequestMethod method, String... roles) {
        rolesAllowed.put(method, roles);
    }

    protected void rolesAllowed(String... roles) {
        rolesAllowed.put(RequestMethod.GET, roles);
        rolesAllowed.put(RequestMethod.POST, roles);
        rolesAllowed.put(RequestMethod.PUT, roles);
        rolesAllowed.put(RequestMethod.PATCH, roles);
        rolesAllowed.put(RequestMethod.DELETE, roles);
    }

    protected void denyAll(RequestMethod method, boolean value) {
        denyAll.put(method, value);
    }

    protected void denyAll(boolean value) {
        denyAll.put(RequestMethod.GET, value);
        denyAll.put(RequestMethod.POST, value);
        denyAll.put(RequestMethod.PUT, value);
        denyAll.put(RequestMethod.PATCH, value);
        denyAll.put(RequestMethod.DELETE, value);
    }

    protected void checkPermissions(HttpServletRequest httpServletRequest, RequestMethod method) {
        if (denyAll.get(method))
            throw new ForbiddenException();

        if (allowAll.get(method))
            return;

        if (rolesAllowed.get(method) == null || rolesAllowed.get(method).length == 0)
            return;

        if (httpServletRequest.getUserPrincipal() == null)
            throw new UnauthorizedException();

        for (String role : rolesAllowed.get(method))
            if (httpServletRequest.isUserInRole(role))
                return;

        throw new ForbiddenException();
    }

    protected abstract BaseRepository<T, K> getRepository();

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<T> get(@RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize, HttpServletRequest httpServletRequest) {
        checkPermissions(httpServletRequest, RequestMethod.GET);

        return getRepository().find(page, pageSize);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    @ResponseBody
    public T get(@PathVariable("id") K id, HttpServletRequest httpServletRequest) {
        checkPermissions(httpServletRequest, RequestMethod.GET);

        T t = getRepository().find(id);

        if (t == null)
            throw new NotFoundException();

        return  t;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/count")
    @ResponseBody
    public Integer count(HttpServletRequest httpServletRequest) {
        checkPermissions(httpServletRequest, RequestMethod.GET);

        return getRepository().count().intValue();
    }
	
}
