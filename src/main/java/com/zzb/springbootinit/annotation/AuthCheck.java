package com.zzb.springbootinit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验 自定义注解 AuthCheck
 *
 * @author <a href="https://github.com/Oxidaner">Oxidaner</a>
 */
@Target(ElementType.METHOD)
/*
* Target翻译中文为目标，即该注解可以声明在哪些目标元素之前，也可理解为注释类型的程序元素的种类。
* */
@Retention(RetentionPolicy.RUNTIME)
/*
* @Retention ：Retention 翻译成中文为保留，可以理解为如何保留，即告诉编译程序如何处理，也可理解为注解类的生命周期。
                RetentionPolicy.SOURCE  : 注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；
                RetentionPolicy.CLASS  : 注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期；
                RetentionPolicy.RUNTIME  : 注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
* */
public @interface AuthCheck {

    /**
     * 必须有某个角色
     *
     * @return
     */
    String mustRole() default "";

}

