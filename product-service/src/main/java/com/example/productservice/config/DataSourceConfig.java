package com.example.productservice.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 读写分离数据源配置
 */
@Slf4j
@Configuration
public class DataSourceConfig {
    
    @Value("${spring.datasource.write.url}")
    private String writeUrl;
    
    @Value("${spring.datasource.write.username}")
    private String writeUsername;
    
    @Value("${spring.datasource.write.password}")
    private String writePassword;
    
    @Value("${spring.datasource.write.driver-class-name}")
    private String writeDriverClassName;
    
    @Value("${spring.datasource.read.url}")
    private String readUrl;
    
    @Value("${spring.datasource.read.username}")
    private String readUsername;
    
    @Value("${spring.datasource.read.password}")
    private String readPassword;
    
    @Value("${spring.datasource.read.driver-class-name}")
    private String readDriverClassName;
    
    @Bean(name = "writeDataSource")
    public DataSource writeDataSource() {
        log.info("初始化写数据源：{}", writeUrl);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(writeUrl);
        dataSource.setUsername(writeUsername);
        dataSource.setPassword(writePassword);
        dataSource.setDriverClassName(writeDriverClassName);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        return dataSource;
    }
    
    @Bean(name = "readDataSource")
    public DataSource readDataSource() {
        log.info("初始化读数据源：{}", readUrl);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(readUrl);
        dataSource.setUsername(readUsername);
        dataSource.setPassword(readPassword);
        dataSource.setDriverClassName(readDriverClassName);
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);
        return dataSource;
    }
    
    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource) {
        log.info("初始化路由数据源（读写分离）");
        
        RoutingDataSource routingDataSource = new RoutingDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(RoutingDataSource.MASTER, writeDataSource);
        targetDataSources.put(RoutingDataSource.SLAVE, readDataSource);
        
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);
        
        return routingDataSource;
    }
}
