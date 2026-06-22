package com.wms.common.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，标注在 Controller 方法上，由 OperationLogAspect 自动记录。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作模块，如 "入库管理" */
String module() default "";

    /** 操作类型，如 "ASN创建" */
String action() default "";

    /** 描述，支持 SpEL 表达式，如 "创建ASN单：[#request.asnNo]" */
String desc() default "";
}
