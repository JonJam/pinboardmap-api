package com.jonjam.pinboard.common.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

public class ConfigurationLoader<T> {
    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static final String HOSTNAME = "HOSTNAME";

    private static final String COMMON_CONFIGURATION_NAME = "common";

    private final ConfigurationFactory<T> configurationFactory;

    public ConfigurationLoader(final ConfigurationFactory<T> configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    public T readConfig() {
        // Hostname config override e.g. local development
        final String hostname = getEnvironmentVariable(HOSTNAME);
        final Config hostnameConfig = ConfigFactory.load(hostname);

        // Environment config override e.g dev, rc, prod
        final String environment = getEnvironmentVariable(ENVIRONMENT);
        final Config environmentConfig = ConfigFactory.load(environment);

        // Common config
        final Config commonConfig = ConfigFactory.load(COMMON_CONFIGURATION_NAME);

        final Config mergedConfig = hostnameConfig.withFallback(environmentConfig)
                                                  .withFallback(commonConfig);

        return configurationFactory.getConfiguration(mergedConfig);
    }

    private String getEnvironmentVariable(final String environmentVariableName) {
        final String value = System.getenv(environmentVariableName);

        if (StringUtils.isBlank(value)) {
           throw new IllegalStateException(String.format("Environment variable \"%s\" is not set.", environmentVariableName));
        }

        return value;
    }
}