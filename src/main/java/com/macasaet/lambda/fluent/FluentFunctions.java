/*******************************************************************************
 * Copyright 2014 Carlos Macasaet
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.macasaet.lambda.fluent;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * 
 * <p>Copyright &copy; 2014 Carlos Macasaet.</p>
 *
 * @author Carlos Macasaet
 */
public class FluentFunctions {

    private static final Logger logger = LoggerFactory.getLogger(FluentFunctions.class);
    private static final ThreadLocal<Method> methodHolder = new ThreadLocal<Method>();

    @SuppressWarnings("unchecked")
    public static <T> T ofClass(final Class<T> c) {
        final MethodInterceptor interceptor = new MethodInterceptor() {
            public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) {
                final Class<?> returnType = method.getReturnType();

                checkState(methodHolder.get() == null,
                        "Improper usage: multiple calls to forMethod for a single call to ofClass.");
                checkArgument(c.isInstance(obj), "Expected object of type: %s", c);                
                checkArgument(returnType != void.class, "Method must return an Object: %s", method);
                checkArgument(args == null || args.length == 0, "Method must have no parameters: %s", method);

                methodHolder.set(method);

                if (!returnType.isPrimitive()) {
                    return null;
                } else if (boolean.class.equals(returnType)) {
                    return false;
                }
                return 0;
            }
        };
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(c);
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }

    public static <X, Y> Function<? super X, ? extends Y> forMethod(final Y invocation) {
        final Method method = methodHolder.get();
        methodHolder.remove();
        checkState(method != null, "Improper usage: ofClass has not been called");
        return new Function<X, Y>() {
            @SuppressWarnings("unchecked")
            public Y apply(final X x) {
                try {
                    return (Y) method.invoke(x, new Object[0]);
                } catch (final IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                    final Throwable cause = e.getCause();
                    throw new RuntimeException(cause.getMessage(), cause);
                }
            }
        };
    }

    public static <X> Predicate<? super X> forMethod(final boolean invocation) {
        final Method method = methodHolder.get();
        methodHolder.remove();
        checkState(method != null, "Improper usage: ofClass has not been called");
        checkState(boolean.class.equals( method.getReturnType() ), "Method does not return a boolean.");
        return new Predicate<X>() {

            public boolean apply(@Nullable X x) {
                try {
                    return (boolean) method.invoke(x, new Object[0]);
                } catch (final IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                    final Throwable cause = e.getCause();
                    throw new RuntimeException(cause.getMessage(), cause);
                }
            }
        };
    }

    protected static void checkState(final boolean state, final String message) {
        if (!state) {
            methodHolder.remove();
            throw new IllegalArgumentException(message);
        }
    }

}