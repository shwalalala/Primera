package cit.edu.primera.common

/**
 * Category marker interface for JUnit 4: @Category(RegressionCategory::class)
 */
interface RegressionCategory

/**
 * Annotation tag for regression test classes or methods:
 * Usage: @RegressionTest
 * Can also be used as a JUnit 4 category: @Category(RegressionTest::class)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegressionTest
