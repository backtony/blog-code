package com.example.springmasterslave.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class JpaConfig {

    @Primary
    @Bean("dataSource")
    public DataSource dataSource(DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean("routingDataSource")
    public DataSource routingDataSource(
        @Qualifier("masterDataSource") DataSource masterDataSource,
        @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        final Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(RoutingDataSource.MASTER, masterDataSource);
        dataSourceMap.put(RoutingDataSource.SLAVE, slaveDataSource);

        final RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(dataSourceMap);

        // default는 slave쪽으로 가도록
        routingDataSource.setDefaultTargetDataSource(slaveDataSource);

        return routingDataSource;
    }

    @Bean("masterDataSource")
    @ConfigurationProperties(prefix = "database.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("slaveDataSource")
    @ConfigurationProperties(prefix = "database.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
}
