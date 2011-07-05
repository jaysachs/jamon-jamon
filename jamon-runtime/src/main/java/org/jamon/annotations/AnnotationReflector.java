package org.jamon.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * A reflection tool to convert annotations loaded under a different class loader into an annotation
 * implementing a specified annotation class. Because TemplateDescription is working with classes
 * loaded from a client-defined ClassLoader, it is possible that said ClassLoader will contain it's
 * own copy of jamon.jar, meaning that annotations we load from classes it returns will not be class
 * compatible with our copies of the annotation classes.
 */
public class AnnotationReflector
{
    private Map<String, Annotation> m_annotations =
        new HashMap<String, Annotation>();

    public AnnotationReflector(Class<?> p_class)
    {
        for (Annotation annotation: p_class.getAnnotations())
        {
            m_annotations.put(annotation.annotationType().getName(),
                              annotation);
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> p_class)
    {
        final Annotation annotation = m_annotations.get(p_class.getName());
        return p_class.cast(proxyAnnotation(p_class, annotation));
    }

    private Object proxyAnnotation(Class<?> p_class, final Object p_annotation)
    {
        return p_class.cast(Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] {p_class},
            new InvocationHandler()
            {
                @Override
                public Object invoke(Object p_proxy,
                                     Method p_method,
                                     Object[] p_args) throws Throwable
                {
                    Object result =
                        p_annotation.getClass().getMethod(p_method.getName())
                            .invoke(p_annotation);
                    return maybeProxyAnnotation(p_method.getReturnType(),
                                                result);
                }

            }));
    }

    private Object maybeProxyAnnotation(Class<?> p_type, Object p_object)
    {
        if (p_object == null)
        {
            return null;
        }

        if (p_type.isAnnotation())
        {
            return proxyAnnotation(p_type, p_object);
        }
        if (p_type.isArray() && p_type.getComponentType().isAnnotation())
        {
            int arrayLength = Array.getLength(p_object);
            Object array =
                Array.newInstance(p_type.getComponentType(), arrayLength);
            for (int i = 0; i < arrayLength; i++)
            {
                Array.set(array,
                          i,
                          proxyAnnotation(p_type.getComponentType(),
                                          Array.get(p_object, i)));
            }
            return array;
        }
        else
        {
            return p_object;
        }
    }
}
