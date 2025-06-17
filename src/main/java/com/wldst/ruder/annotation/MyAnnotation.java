package com.wldst.ruder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @自定义注解
 */
@Retention(RetentionPolicy.RUNTIME) // 注解运行在哪一个时期的
@Target(ElementType.METHOD) // 注解用在哪上边？
public @interface MyAnnotation {
}
