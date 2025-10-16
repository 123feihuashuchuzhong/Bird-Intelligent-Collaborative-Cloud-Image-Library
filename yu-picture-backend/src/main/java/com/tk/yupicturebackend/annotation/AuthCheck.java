package com.tk.yupicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//表示这个注解只能用于方法上，设计用于控制方法级别的访问权限。
@Retention(RetentionPolicy.RUNTIME)//表示注解在运行时依然有效，可以通过反射获取注解信息。
public @interface AuthCheck {
    /**
     * 必须有某个角色
     * @return
     */
    String mustRole() default ""; // 默认值为空字符串
}
//注解的属性设计
//mustRole() 属性用于指定访问方法必须拥有的角色，默认值为空字符串。
//这种设计允许灵活地为不同方法设置不同的角色访问要求。
//与系统错误码的关联
//从 ErrorCode.java 中可以看到 NO_AUTH_ERROR(40101, "无权限") 错误码，这与 AuthCheck 注解的功能直接对应。
//当用户不具备指定角色时，系统会抛出无权限错误。
//权限控制设计模式
//这是一种典型的声明式权限控制设计，通过注解将权限要求与业务方法解耦。
//通常会配合 AOP（面向切面编程）实现，在方法执行前检查用户是否拥有指定角色