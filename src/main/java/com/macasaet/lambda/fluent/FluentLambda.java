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
 * Static methods for concisely defining {@link Predicate Predicates} and {@link Function Functions}.
 *
 * <p>Copyright &copy; 2017 Carlos Macasaet.</p>
 *
 * @author Carlos Macasaet
 */
public class FluentLambda {

    private static final Logger logger = LoggerFactory.getLogger(FluentLambda.class);
    private static final ThreadLocal<Method> methodHolder = new ThreadLocal<Method>();

    /**
     * <p>Specify the Class from which you would like to capture a method invocation. This must be called prior to
     * calling {@link #forMethod(boolean)} or {@link #forMethod(Object)}.</p>
     *
     * <p>See the documentation for {@link #forMethod(boolean)} and {@link #forMethod(Object)} for example usage.</p>
     *
     * @param <T> The type from which a member reference will be generated
     * @param c the class from which a {@link Predicate} or {@link Function} will be created.
     * @return a dummy instance of <em>c</em> that will record the next method invocation.
     */
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

    /**
     * <p>Record a method invocation. {@link #ofClass(Class)} must be called first. The invocation must have zero
     * arguments and it must have a non-void return type.</p>
     *
     * <p>Example:</p>
     * <blockquote>
     * Function&lt;User, String&gt; idExtractor = forMethod( ofClass( User.class ).getId() );
     * </blockquote>
     *
     * @param <X> The input type to the Function and the type passed into {@link #ofClass(Class)}
     * @param <Y> The output type of the Function and the type passed into {@link #forMethod(Object)}
     * @param invocation The result of calling a no-arg non-void method on an object generated by
     *  {@link #ofClass(Class)}.
     * @return a Function that will invoke the recorded method to convert from instances of the parameter to
     *         {@link #ofClass(Class)} to the return type of the recorded method.
     */
    public static <X, Y> Function<? super X, Y> forMethod(final Y invocation) {
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

    /**
     * <p>Record a method invocation. {@link #ofClass(Class)} must be called first. The invocation must have zero
     * arguments and it must return a boolean.</p>
     *
     * <p>Example:</p>
     * <blockquote>
     * Predicate&lt;User&gt; activeUserIdentifier = forMethod( ofClass( User.class ).isActive() );
     * </blockquote>
     *
     * @param <X> The input type to the Predicate and the type passed into {@link #ofClass(Class)}
     * @param invocation The result of calling a no-arg boolean method on an object generated by
     *  {@link #ofClass(Class)}.
     * @return a Predicate that will invoke the recorded method to evaluate instances of the Class passed to
     *  {@link #ofClass(Class)}.
     */
    public static <X> Predicate<? super X> forMethod(final boolean invocation) {
        final Method method = methodHolder.get();
        methodHolder.remove();
        checkState(method != null, "Improper usage: ofClass has not been called");
        checkState(boolean.class.equals(method.getReturnType()), "Method does not return a boolean.");
        return new Predicate<X>() {

            public boolean apply(@Nullable X x) {
                try {
                    final Boolean result = (Boolean) method.invoke(x, new Object[0]);
                    return (boolean)result;
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

    /**
     * <p>A drop-in replacement for {@link com.google.common.base.Preconditions#checkState(boolean, Object)} except this
     * method performs necessary clean up work in the case the API is used incorrectly.</p>
     *
     * @param state true if and only if the preconditions are met
     * @param message the error message to include in the {@link IllegalArgumentException} if the preconditions are not
     *  met.
     */
    protected static void checkState(final boolean state, final String message) {
        if (!state) {
            methodHolder.remove();
            throw new IllegalArgumentException(message);
        }
    }

}