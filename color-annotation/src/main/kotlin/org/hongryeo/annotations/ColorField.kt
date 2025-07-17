package org.hongryeo.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class ColorField(val light: String, val dark: String)
