package io.github.chunzhi23.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class ColorField(val light: String, val dark: String)