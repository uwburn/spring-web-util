package it.mgt.util.spring.web.jaxb;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.xml.Jaxb2CollectionHttpMessageConverter;
import org.springframework.util.Assert;

public class Jaxb2WrappedCollectionHttpMessageConverter<T extends Collection<?>> extends Jaxb2CollectionHttpMessageConverter<T> {
	
	private final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<>(64);
	
	private boolean simpleClassScanning = false;
	private boolean defaultCollectionWrapper = false;
	
	public boolean isSimpleClassScanning() {
		return simpleClassScanning;
	}

	public void setSimpleClassScanning(boolean simpleClassScanning) {
		this.simpleClassScanning = simpleClassScanning;
	}

	public boolean isDefaultCollectionWrapper() {
		return defaultCollectionWrapper;
	}

	public void setDefaultCollectionWrapper(boolean defaultCollectionWrapper) {
		this.defaultCollectionWrapper = defaultCollectionWrapper;
	}

	@XmlRootElement(name = "collection")
	public static class Wrapper<T> {
		 
	    private Collection<T> items;
	 
	    public Wrapper() {
	    	items = new ArrayList<>();
	    }
	    
	    public Wrapper(Collection<T> items) {
	        this.items = items;
	    }
	 
	    @XmlAnyElement(lax=true)
	    public Collection<T> getItems() {
	        return items;
	    }
	 
	}
	
	@XmlRootElement(name = "list")
	public static class ListWrapper<T> extends Wrapper<T> {
		
		public ListWrapper() {
			super();
		}
	 
	    public ListWrapper(Collection<T> items) {
	        super(items);
	    }
	 
	}
	
	@XmlRootElement(name = "set")
	public static class SetWrapper<T> extends Wrapper<T> {
		
		public SetWrapper() {
			super();
		}
	 
	    public SetWrapper(Collection<T> items) {
	        super(items);
	    }
	 
	}
	
	@XmlRootElement(name = "queue")
	public static class QueueWrapper<T> extends Wrapper<T> {
		
		public QueueWrapper() {
			super();
		}
	 
	    public QueueWrapper(Collection<T> items) {
	        super(items);
	    }
	 
	}
	
	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return Collection.class.isAssignableFrom(clazz) && canRead(mediaType);
	}
	
	@SuppressWarnings("rawtypes")
	protected void writeToResult(T collection, HttpHeaders headers, Result result) throws IOException {
		Wrapper wrapper = getWrapper(collection);
		
		Class<?> clazz = getClass(collection);
		
		try {
			Marshaller marshaller;
			if (clazz == null)
				marshaller = createMarshaller(wrapper.getClass());
			else
				marshaller = createMarshaller(wrapper.getClass(), clazz);
			setCharset(headers.getContentType(), marshaller);
			marshaller.marshal(wrapper, result);
		}
		catch (MarshalException ex) {
			throw new HttpMessageNotWritableException("Could not marshal [" + collection + "]: " + ex.getMessage(), ex);
		}
		catch (JAXBException ex) {
			throw new HttpMessageConversionException("Could not instantiate JAXBContext: " + ex.getMessage(), ex);
		}
	}
	
	private void setCharset(MediaType contentType, Marshaller marshaller) throws PropertyException {
		if (contentType != null && contentType.getCharSet() != null) {
			marshaller.setProperty(Marshaller.JAXB_ENCODING, contentType.getCharSet().name());
		}
	}
	
	private Marshaller createMarshaller(Class<?> wrapperClazz, Class<?> clazz) {
		try {
			JAXBContext jaxbContext = getJaxbContext(wrapperClazz, clazz);
			Marshaller marshaller = jaxbContext.createMarshaller();
			customizeMarshaller(marshaller);
			return marshaller;
		}
		catch (JAXBException ex) {
			throw new HttpMessageConversionException(
					"Could not create Marshaller for class [" + clazz + "]: " + ex.getMessage(), ex);
		}
	}
	
	private JAXBContext getJaxbContext(Class<?> wrapperClazz, Class<?> clazz) {
		Assert.notNull(clazz, "'clazz' must not be null");
		JAXBContext jaxbContext = this.jaxbContexts.get(clazz);
		if (jaxbContext == null) {
			try {
				jaxbContext = JAXBContext.newInstance(wrapperClazz, clazz);
				this.jaxbContexts.putIfAbsent(clazz, jaxbContext);
			}
			catch (JAXBException ex) {
				throw new HttpMessageConversionException(
						"Could not instantiate JAXBContext for class [" + clazz + "]: " + ex.getMessage(), ex);
			}
		}
		return jaxbContext;
	}
	
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private Wrapper getWrapper(Collection<?> collection) {
		if (defaultCollectionWrapper)
			return new Wrapper(collection);

		if (collection instanceof List)
			return new ListWrapper(collection);
		else if (collection instanceof Set)
			return new SetWrapper(collection);
		else if (collection instanceof Queue)
			return new QueueWrapper(collection);
		else
			return new Wrapper(collection);
	}
	
	private Class<?> getClass(Collection<?> collection) {
        if (collection.size() == 0)
            return null;

		if (simpleClassScanning)
			return getClassSimple(collection);
			
		return getClassFull(collection);
	}
	
	private Class<?> getClassSimple(Collection<?> collection) {
		return collection.iterator().next().getClass();
	}

    private Class<?> getClassFull(Collection<?> collection) {
        Set<Class<?>> classes = new HashSet<>();
        for (Object o : collection)
            classes.add(o.getClass());

        return commonSuperClass(classes).get(0);
    }

    private Set<Class<?>> getClassesBfs(Class<?> clazz, boolean includeInterfaces) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        Set<Class<?>> nextLevel = new LinkedHashSet<Class<?>>();
        nextLevel.add(clazz);
        do {
            classes.addAll(nextLevel);
            Set<Class<?>> thisLevel = new LinkedHashSet<Class<?>>(nextLevel);
            nextLevel.clear();
            for (Class<?> each : thisLevel) {
                Class<?> superClass = each.getSuperclass();
                if (superClass != null) {
                    nextLevel.add(superClass);
                }

                if (!includeInterfaces)
                    continue;

                for (Class<?> eachInt : each.getInterfaces()) {
                    nextLevel.add(eachInt);
                }
            }
        } while (!nextLevel.isEmpty());
        return classes;
    }

    private List<Class<?>> commonSuperClass(Collection<Class<?>> classes) {
        if (classes.size() == 0)
            return new LinkedList<>();

        Set<Class<?>> rollingIntersect = new LinkedHashSet<Class<?>>(
                getClassesBfs(classes.iterator().next(), false));

        for (Class<?> clazz : classes)
            rollingIntersect.retainAll(getClassesBfs(clazz, false));


        return new LinkedList<Class<?>>(rollingIntersect);
    }

}
