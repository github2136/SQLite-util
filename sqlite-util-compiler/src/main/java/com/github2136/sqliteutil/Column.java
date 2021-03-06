package com.github2136.sqliteutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yubin on 2017/7/20.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    //版本号
    int version() default 1;

    //列明
    String columnName() default "";

    enum Type {STRING, BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BOOLEAN, BYTES, DATE, UNKNOW}

    Type columnType() default Type.UNKNOW;

    //主键
    boolean primaryKey() default false;

    //非空
    boolean notNull() default false;

    //唯一
    boolean unique() default false;

    //默认值
    String defaultVal() default "";
}
