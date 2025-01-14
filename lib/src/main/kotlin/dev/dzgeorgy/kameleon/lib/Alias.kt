package dev.dzgeorgy.kameleon.lib

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Alias(
    vararg val value: String
)
