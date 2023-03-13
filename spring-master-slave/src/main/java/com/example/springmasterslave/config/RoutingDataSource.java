package com.example.springmasterslave.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {
    public static final String MASTER = "MASTER";
    public static final String SLAVE = "SLAVE";

    @Override
    protected Object determineCurrentLookupKey() {
        final boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        final boolean actuallyActive = TransactionSynchronizationManager.isActualTransactionActive();
        final String dbKey = actuallyActive && !readOnly ? MASTER : SLAVE;

        log.info("transaction: db={} (readonly={}, actuallyActive={})", dbKey, readOnly, actuallyActive);

        return dbKey;
    }
}
