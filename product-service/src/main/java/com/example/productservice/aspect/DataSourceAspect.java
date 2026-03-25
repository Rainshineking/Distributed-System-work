package com.example.productservice.aspect;

import com.example.productservice.config.RoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 读写分离 AOP 切面
 * 自动切换数据源：写操作使用主库，读操作使用从库
 */
@Slf4j
@Aspect
@Component
@Order(1) // 保证在事务之前执行
public class DataSourceAspect {
    
    /**
     * 写操作切点（增删改）
     */
    @Pointcut("execution(* com.example.productservice.service.*.create*(..)) || " +
              "execution(* com.example.productservice.service.*.save*(..)) || " +
              "execution(* com.example.productservice.service.*.add*(..)) || " +
              "execution(* com.example.productservice.service.*.update*(..)) || " +
              "execution(* com.example.productservice.service.*.delete*(..)) || " +
              "execution(* com.example.productservice.service.*.remove*(..))")
    public void writeOperation() {
    }
    
    /**
     * 读操作切点（查询）
     */
    @Pointcut("execution(* com.example.productservice.service.*.get*(..)) || " +
              "execution(* com.example.productservice.service.*.find*(..)) || " +
              "execution(* com.example.productservice.service.*.query*(..)) || " +
              "execution(* com.example.productservice.service.*.list*(..)) || " +
              "execution(* com.example.productservice.service.*.search*(..))")
    public void readOperation() {
    }
    
    /**
     * 写操作前使用主库
     */
    @Before("writeOperation()")
    public void beforeWrite() {
        log.debug("切换到写库（主库）");
        RoutingDataSource.useMaster();
    }
    
    /**
     * 读操作前使用从库
     */
    @Before("readOperation()")
    public void beforeRead() {
        log.debug("切换到读库（从库）");
        RoutingDataSource.useSlave();
    }
    
    /**
     * 操作完成后清理上下文
     */
    @After("writeOperation() || readOperation()")
    public void afterOperation() {
        RoutingDataSource.clear();
        log.debug("清理数据源上下文");
    }
}
