package com.wms.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
String methodName = joinPoint.getSignature().toShortString();
        Object result = null;
String errorMsg = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long elapsed = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());

            // 获取方法参数（简化处理，不超过 1024 字符）
            Object[] args = joinPoint.getArgs();
String params = "[]";
            try {
                // 过滤 HttpServletRequest 等不可序列化参数
                Object[] filtered = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof HttpServletRequest) {
                        filtered[i] = "[HttpServletRequest]";
                    } else {
                        filtered[i] = args[i];
                    }
                }
                params = objectMapper.writeValueAsString(filtered);
                if (params.length() > 1024) {
                    params = params.substring(0, 1021) + "...";
                }
            } catch (Exception ignored) {}

            // 日志记录（后续可写入 wms_sys_operation_log 表）
            log.info("[OP-LOG] module={}, action={}, desc={}, method={}, params={}, " +
                     "result={}, elapsed={}ms, user={}",
                    operationLog.module(),
                    operationLog.action(),
                    operationLog.desc(),
                    methodName,
                    params,
                    errorMsg != null ? "ERROR:" + errorMsg : "SUCCESS",
                    elapsed,
                    UserContext.getUsername());
        }
    }
}
