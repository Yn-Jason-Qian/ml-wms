package com.wms.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.wms.common.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器：多租户 + 分页
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 多租户拦截器：自动在 SQL 中添加 tenant_id = ? 条件
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

        // 分页插件（兼容 MySQL）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }
}
