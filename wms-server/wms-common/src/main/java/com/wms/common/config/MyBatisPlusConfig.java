package com.wms.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.wms.common.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Statement;
import java.util.Properties;

@Configuration
public class MyBatisPlusConfig {

    private static final Logger log = LoggerFactory.getLogger(MyBatisPlusConfig.class);

    /**
     * MyBatis-Plus 拦截器：多租户 + 分页 + 慢 SQL 监控
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 多租户拦截器
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor(
                new com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler() {
                    @Override
                    public Expression getTenantId() {
                        Long tenantId = TenantContext.get();
                        if (tenantId == null) {
                            tenantId = 0L;
                        }
                        return new LongValue(tenantId);
                    }

                    @Override
                    public String getTenantIdColumn() {
                        return "tenant_id";
                    }

                    @Override
                    public boolean ignoreTable(String tableName) {
                        return "wms_sys_tenant".equalsIgnoreCase(tableName)
                            || "wms_sys_menu".equalsIgnoreCase(tableName);
                    }
                });
        interceptor.addInnerInterceptor(tenantInterceptor);

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    /**
     * 慢 SQL 监控拦截器
     * 记录执行时间超过 1000ms 的 SQL，便于性能排查
     */
    @Bean
    public SlowSqlInterceptor slowSqlInterceptor() {
        return new SlowSqlInterceptor(1000);
    }

    /**
     * 慢 SQL 拦截器实现
     */
    @Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
    })
    public static class SlowSqlInterceptor implements Interceptor {
        private final long thresholdMs;

        public SlowSqlInterceptor(long thresholdMs) {
            this.thresholdMs = thresholdMs;
        }

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            long start = System.currentTimeMillis();
            try {
                return invocation.proceed();
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > thresholdMs) {
                    Object target = invocation.getTarget();
                    log.warn("[SLOW-SQL] {}ms — target={}", elapsed, target);
                }
            }
        }

        @Override
        public Object plugin(Object target) {
            return Plugin.wrap(target, this);
        }

        @Override
        public void setProperties(Properties properties) { /* noop */ }
    }
}
