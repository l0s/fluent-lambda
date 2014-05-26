package com.macasaet.lambda.fluent;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.transform;
import static com.macasaet.lambda.fluent.FluentFunctions.forMethod;
import static com.macasaet.lambda.fluent.FluentFunctions.ofClass;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math.linear.AnyMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.RealVector;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Example usage of the {@link FluentFunctions} class.
 *
 * <p>Copyright &copy; 2014 Carlos Macasaet</p>
 *
 * @author Carlos Macasaet &lt;cmacasaet@edmunds.com&gt;
 */
public class Examples {

    private final Array2DRowRealMatrix squareMatrix = new Array2DRowRealMatrix(new double[][] { {0, 1}, {2, 3}});
    private final Array2DRowRealMatrix nonSquareMatrix = new Array2DRowRealMatrix(new double[][] { {0, 1, 3}, {4, 5, 6}});
    private final Collection<? extends AnyMatrix> matrices = asList(squareMatrix, nonSquareMatrix);
    private final RealVector threeFour = new ArrayRealVector(new double[]{ 3, 4 });
    private final RealVector sixEight = new OpenMapRealVector(new double[] {6, 8});
    private final List<? extends RealVector> vectors = asList(threeFour, sixEight);

    /**
     * This is the standard way of defining {@link Predicate Predicates} in Guava. Notice that even in this simple case
     * of a single method invocation, the Predicate definition uses five lines.
     */
    @Test
    public final void definePredicateWithAnonymousInnerClass() {
        final Predicate<? super AnyMatrix> matrixIsSquare = new Predicate<AnyMatrix>() {
            public boolean apply(final AnyMatrix input) {
                return input.isSquare();
            }
        };

        final Collection<? extends AnyMatrix> result = filter(matrices, matrixIsSquare);

        assertTrue(result.contains(squareMatrix));
        assertFalse(result.contains(nonSquareMatrix));
    }

    /**
     * This is a more concise and declarative way of defining a {@link Predicate}. It uses an example method invocation
     * to create the functor.
     */
    @Test
    public final void definePredicateWithFluentInterface() {
        final Predicate<? super AnyMatrix> matrixIsSquare = forMethod(ofClass(AnyMatrix.class).isSquare());

        final Collection<? extends AnyMatrix> result = filter(matrices, matrixIsSquare);
        assertTrue(result.contains(squareMatrix));
        assertFalse(result.contains(nonSquareMatrix));
    }

    /**
     * This is the standard way of defining {@link Function Functions} in Guava. Even though it only invokes a single
     * method on the input, it still requires five lines.
     */
    @Test
    public final void defineFunctionWithAnonymousInnerClass() {
        final Function<? super RealVector, ? extends Double> normCalculator = new Function<RealVector, Double>() {
            public Double apply(final RealVector input) {
                return input.getNorm();
            }
        };

        final List<? extends Double> result = transform(vectors, normCalculator);
        assertEquals(asList(5.0d, 10.0d), result);
    }

    /**
     * This is a more concise and declarative way of defining a {@link Function}. It uses an example invocation to build
     * the functor.
     */
    @Test
    public final void defineFunctionWithFluentInterface() {
        final Function<? super RealVector, ? extends Double> normCalculator = forMethod(ofClass(RealVector.class).getNorm());

        final List<? extends Double> result = transform(vectors, normCalculator);
        assertEquals(asList(5.0d, 10.0d), result);
    }

}