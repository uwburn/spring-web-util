package it.mgt.util.spring.web.controller;

import it.mgt.util.spring.web.exception.BadRequestException;
import it.mgt.util.spring.web.exception.ConflictException;
import it.mgt.util.spring.web.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseCrudCntrl<T, K> extends BaseReadCntrl<T, K> {

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@Transactional
	public T post(@RequestBody T t, HttpServletRequest httpServletRequest) {
        checkPermissions(httpServletRequest, RequestMethod.POST);

		if (getRepository().getKey(t) == null) {
			getRepository().persist(t);
			return t;
		}

		if (getRepository().find(getRepository().getKey(t)) != null)
			throw new ConflictException();

		return getRepository().merge(t);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void put(@PathVariable("id") K id, @RequestBody T t, HttpServletRequest httpServletRequest) {
        checkPermissions(httpServletRequest, RequestMethod.PUT);

		if (getRepository().getKey(t) == null)
			getRepository().setKey(t, id);

		if (!getRepository().getKey(t).equals(id))
			throw new BadRequestException();

		getRepository().merge(t);
	}

	@RequestMapping(method = RequestMethod.PATCH, value = "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void patch(@PathVariable("id") K id, @RequestBody T t, HttpServletRequest httpServletRequest) {
		checkPermissions(httpServletRequest, RequestMethod.PATCH);

		if (getRepository().getKey(t) == null)
			getRepository().setKey(t, id);

		if (!getRepository().getKey(t).equals(id))
			throw new BadRequestException();

		if (getRepository().find(id) == null)
			throw new NotFoundException();

		getRepository().merge(t);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void delete(@PathVariable("id") K id, HttpServletRequest httpServletRequest) {
        checkPermissions(httpServletRequest, RequestMethod.DELETE);

		T t = getRepository().find(id);

		if (t == null)
			throw new NotFoundException();

		getRepository().remove(t);
	}
	
}
