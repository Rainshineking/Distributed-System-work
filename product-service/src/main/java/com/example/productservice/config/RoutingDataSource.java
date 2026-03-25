package com.example.productservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 读写分离数据源配置
 */
@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {
    
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static final String MASTER = "master";
    public static final String SLAVE = "slave";
    
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceType = CONTEXT_HOLDER.get();
        if (dataSourceType == null) {
            return MASTER;
        }
        log.debug("当前使用数据源：{}", dataSourceType);
        return dataSourceType;
    }
    
    public static void useMaster() {
        CONTEXT_HOLDER.set(MASTER);
    }
    
    public static void useSlave() {
        CONTEXT_HOLDER.set(SLAVE);
    }
    
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
