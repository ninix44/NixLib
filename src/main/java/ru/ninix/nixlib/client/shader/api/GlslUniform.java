package ru.ninix.nixlib.client.shader.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be automatically sent to the GLSL Shader.
 * The value inside the annotation is the uniform name in the .json/.fsh file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GlslUniform {
    String value();
}
