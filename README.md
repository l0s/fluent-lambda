fluent-lambda
=============

A *concise and declarative* way to define lambda expressions in Java. It builds on top of [Guava][1] and provides an alternative to anonymous inner classes for defining [Functions][2] and [Predicates][3] when those functors just wrap single object-level method invocations.

## Examples

These examples are completely fleshed out in the [unit tests][4].

### Defining Predicates.

Let's say you have a collection of matrices and you want to retain only the ones that are square. Guava makes this easy:

    com.google.common.collect.Collections2.filter(matrices, matrixIsSquare);

Typically the *matrixIsSquare* Predicate would be defined like this:

    Predicate<AnyMatrix> matrixIsSquare = new Predicate<AnyMatrix>() {
        public boolean apply(AnyMatrix input) {
            return input.isSquare();
        }
    };

It seems excessive for just a simple method invocation. fluent-lambda provides a concise and declarative way of defining the same Predicate:

    Predicate<AnyMatrix> matrixIsSquare = forMethod(ofClass(AnyMatrix.class).isSquare());

It uses an example method invocation to define the Predicate. There is no need for referencing classes or methods by name, so it is refactoring-friendly.

### Defining Functions

Let's say you have a collection of vectors that you would like to get the [norms][5] of those vectors. Again, Guava makes this easy:

    com.google.common.collect.Lists.transform(vectors, normCalculator);

Typically, the *normCalculator* Function would be defined like this:

    Function<RealVector, Double> normCalculator = new Function<RealVector, Double>() {
        public Double apply(RealVector input) {
            return input.getNorm();
        }
    };

This also seems excessive for just a simple method invocation. fluent-lambda provides a concise and declarative way of defining Functions as well:

    Function<RealVector, Double> normCalculator = forMethod(ofClass(RealVector.class).getNorm());

## Getting the Library

fluent-lambda is available in [The Central Repository][6].

### Maven

To include the library in your Maven project, add the following dependency to your POM:

    <dependency>
      <groupId>com.macasaet.lambda</groupId>
      <artifactId>fluent-lambda</artifactId>
      <version>0.1.0</version>
    </dependency>

### Gradle

To include the library in your Gradle project, add the following dependency to build.gradle:

    compile group: 'com.macasaet.lambda', name: 'fluent-lambda', version: '0.1.0'

### Manual

You can download the library directly from [The Central Repository][6].

## Building the Project

fluent-lambda uses the [Gradle][7] build system. To build from the command line, type:

    gradle install

### Known Issues

fluent-lambda currently does not build with JDK 6.

### Continuous Integration

[![Build Status](https://travis-ci.org/l0s/fluent-lambda.png?branch=master)](https://travis-ci.org/l0s/fluent-lambda)

### Releasing Deployments to the Central Repository

Before releasing to Central, you will need to define the following properties in your *build.gradle* file:

    signing.keyId=[your eight-character public key ID]
    signing.secretKeyRingFile=[full path to the secret key ring that contains the keyId]
    nexusUsername=[your username for Central]

To build and release to Central, type:

    gradle uploadArchives

You will be prompted for the password to your secret key and for your Central password.

  [1]: https://code.google.com/p/guava-libraries/
  [2]: http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/base/Function.html
  [3]: http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/base/Predicate.html
  [4]: https://github.com/l0s/fluent-lambda/blob/master/src/test/java/com/macasaet/lambda/fluent/Examples.java
  [5]: https://en.wikipedia.org/wiki/Norm_%28mathematics%29
  [6]: http://search.maven.org/#browse%7C523643277
  [7]: http://www.gradle.org/
