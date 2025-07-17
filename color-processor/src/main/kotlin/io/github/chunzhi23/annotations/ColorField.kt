package io.github.chunzhi23.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class ColorField(val light: String, val dark: String)