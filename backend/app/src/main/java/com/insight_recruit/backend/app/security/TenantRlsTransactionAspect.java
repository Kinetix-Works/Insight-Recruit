package com.insight_recruit.backend.app.security;

import com.insight_recruit.backend.app.auth.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantRlsTransactionAspect {

    private final JdbcTemplate jdbcTemplate;

    public TenantRlsTransactionAspect(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Around(
        "@annotation(org.springframework.transaction.annotation.Transactional) "
            + "|| @within(org.springframework.transaction.annotation.Transactional)"
    )
    public Object bindTenantForTransactionalBoundary(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser principal)) {
            return joinPoint.proceed();
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            jdbcTemplate.update("SET LOCAL app.current_tenant = ?", principal.tenantId().toString());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    try {
                        jdbcTemplate.execute("RESET app.current_tenant");
                    } catch (Exception ex) {
                        log.debug("Unable to reset app.current_tenant after tx completion", ex);
                    }
                }
            });
        }

        return joinPoint.proceed();
    }
}
