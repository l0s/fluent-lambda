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

import static com.macasaet.lambda.fluent.FluentLambda.forMethod;
import static com.macasaet.lambda.fluent.FluentLambda.ofClass;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.apache.commons.math.linear.AnyMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * Test class for {@link FluentLambda}.
 *
 * <p>Copyright &copy; 2017 Carlos Macasaet.</p>
 *
 * @author Carlos Macasaet
 */
public class FluentLambdaTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void verifyMethodCaptured() {
        // given
        final Function<? super Logger, ? extends String> f = forMethod(ofClass(Logger.class).getName());
        final Logger x = LoggerFactory.getLogger("name");

        // when
        final String y = f.apply(x);

        // then
        assertEquals("name", y);
    }

    @Test
    public final void verifyMultipleCapturesThrowsException() {
        // given
        final Logger stub = ofClass(Logger.class);
        stub.getName();

        // when
        try {
            stub.getName();

            // then
            fail("Expected RuntimeException.");
        } catch (final RuntimeException re) {
        }
    }

    @Test
    public final void verifyForMethodWithoutOfClassThrowsException() {
        // given
        final Logger logger = LoggerFactory.getLogger(getClass());

        // when
        try {
            forMethod(logger.getName());

            // then
            fail("Expected RuntimeException.");
        } catch (final RuntimeException re) {
        }
    }

    @Test
    public final void verifyPredicateEvaluatesTrue() {
        // given
        final Array2DRowRealMatrix squareMatrix = new Array2DRowRealMatrix(new double[][] { {0, 1}, {2, 3}});

        // when
        final Predicate<? super AnyMatrix> predicate = forMethod(ofClass(AnyMatrix.class).isSquare());

        // then
        assertTrue(predicate.apply(squareMatrix));
    }

    @Test
    public final void verifyPredicateEvaluatesFalse() {
        // given
        final Array2DRowRealMatrix nonSquareMatrix = new Array2DRowRealMatrix(new double[][] { {0, 1, 3}, {4, 5, 6}});

        // when
        final Predicate<? super AnyMatrix> predicate = forMethod(ofClass(AnyMatrix.class).isSquare());

        // then
        assertFalse(predicate.apply(nonSquareMatrix));
    }

    @Test
    public final void testFunction() {
        // given
        final RealVector instance = new ArrayRealVector(new double[] {3, 4});

        // when
        final Function<? super RealVector, ? extends Double> function = forMethod(ofClass(RealVector.class).getNorm());

        // then
        final double result = function.apply(instance);
        assertEquals(5.0d, result, 0);
    }

    protected static class Pojo {

        private String id;
        private String username;

        public Pojo() {
        }

        public Pojo(String id, String username) {
            setId(id);
            setUsername(username);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

    }

    @Test
    public final void verifyAbilityToGenerateMap() {
        // given
        final List<Pojo> users =
                asList(new Pojo("56cfbdad-9209-42d9-9761-96e6b8f04887", "alice"),
                        new Pojo("ef24742f-2345-4e5f-bda0-f9b6ae0955cd", "bob"));

        // when
        final Function<? super Pojo, String> function = forMethod(ofClass(Pojo.class).getId());

        // then
        final Map<String, Pojo> map = Maps.uniqueIndex(users, function);
        assertEquals("alice", map.get("56cfbdad-9209-42d9-9761-96e6b8f04887").getUsername());
        assertEquals("bob", map.get("ef24742f-2345-4e5f-bda0-f9b6ae0955cd").getUsername());
    }

}