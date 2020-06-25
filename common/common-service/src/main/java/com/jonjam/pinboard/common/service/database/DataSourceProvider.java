package com.jonjam.pinboard.common.service.database;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceProvider implements Provider<DataSource> {

    private final ConnectionInfo connectionInfo;

    @Inject
    public DataSourceProvider(final ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public DataSource get() {
        // TODO See jdbi docs for high availability

        // Other things that could setup are:
        // - Driver doesn't support slow query logging: https://github.com/brettwooldridge/HikariCP#log-statement-text--slow-query-logging
        // - Caching already enabled by default: https://github.com/brettwooldridge/HikariCP/wiki/FAQ#q-how-to-i-properly-enable-preparedstatement-caching-for-postgresql
        final HikariConfig config = new HikariConfig();
        // Using preferred datasource class name. See: https://github.com/brettwooldridge/HikariCP#essentials
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.setMaxLifetime(connectionInfo.getMaxLifetime());
        config.setMaximumPoolSize(connectionInfo.getMaximumPoolSize());
        config.setPoolName(connectionInfo.getDatabaseName());
        config.setLeakDetectionThreshold(connectionInfo.getLeakDetectionThreshold());

        config.setUsername(connectionInfo.getUsername());
        config.setPassword(connectionInfo.getPassword());
        // See https://jdbc.postgresql.org/documentation/head/connect.html for details
        config.addDataSourceProperty("serverName", connectionInfo.getHost());
        config.addDataSourceProperty("databaseName", connectionInfo.getDatabaseName());
        config.addDataSourceProperty("portNumber", connectionInfo.getPort());
        config.addDataSourceProperty("ssl", connectionInfo.getUseSSL());
        // See https://github.com/brettwooldridge/HikariCP/wiki/JDBC-Logging about logging
        config.addDataSourceProperty("loggerLevel", connectionInfo.getLogLevel());
        config.addDataSourceProperty("logUnclosedConnections", connectionInfo.getLogUnclosedConnections());
        config.addDataSourceProperty("reWriteBatchedInserts", true);

        // Setting per guidance on https://jdbc.postgresql.org/documentation/head/callproc.html
        config.addDataSourceProperty("escapeSyntaxCallMode", "callIfNoReturn");

        // Setting per guidance on https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery#tcp-timeouts
        config.addDataSourceProperty("socketTimeout", connectionInfo.getSocketTimeout());

        return new HikariDataSource(config);
    }
}