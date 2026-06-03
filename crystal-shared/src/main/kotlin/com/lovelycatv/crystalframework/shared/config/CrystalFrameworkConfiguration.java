package com.lovelycatv.crystalframework.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Unified configuration class for all crystalframework.* properties.
 * Each top-level key under crystalframework: in application.yaml maps to a nested static class here.
 */
@Configuration
@ConfigurationProperties("crystalframework")
public class CrystalFrameworkConfiguration {

    private Auth auth = new Auth();
    private Resource resource = new Resource();
    private Monitor monitor = new Monitor();
    private Sharding sharding = new Sharding();

    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public Monitor getMonitor() { return monitor; }
    public void setMonitor(Monitor monitor) { this.monitor = monitor; }

    public Sharding getSharding() { return sharding; }
    public void setSharding(Sharding sharding) { this.sharding = sharding; }

    // -------------------------------------------------------------------------
    // crystalframework.auth
    // -------------------------------------------------------------------------
    public static class Auth {
        private Jwt jwt = new Jwt();

        public Jwt getJwt() { return jwt; }
        public void setJwt(Jwt jwt) { this.jwt = jwt; }

        public static class Jwt {
            private Duration expiration = Duration.ofDays(7);

            public Duration getExpiration() { return expiration; }
            public void setExpiration(Duration expiration) { this.expiration = expiration; }
        }
    }

    // -------------------------------------------------------------------------
    // crystalframework.resource
    // -------------------------------------------------------------------------
    public static class Resource {
        private ResourceConfig avatar = new ResourceConfig();
        private ResourceConfig tenantIcon = new ResourceConfig();

        public ResourceConfig getAvatar() { return avatar; }
        public void setAvatar(ResourceConfig avatar) { this.avatar = avatar; }

        public ResourceConfig getTenantIcon() { return tenantIcon; }
        public void setTenantIcon(ResourceConfig tenantIcon) { this.tenantIcon = tenantIcon; }

        public static class ResourceConfig {
            private String[] supportedContentTypes = new String[0];

            public String[] getSupportedContentTypes() { return supportedContentTypes; }
            public void setSupportedContentTypes(String[] supportedContentTypes) {
                this.supportedContentTypes = supportedContentTypes;
            }
        }
    }

    // -------------------------------------------------------------------------
    // crystalframework.monitor
    // -------------------------------------------------------------------------
    public static class Monitor {
        /** Flush interval in milliseconds. Default 30000 (30 seconds). */
        private long flushIntervalMs = 30000;

        public long getFlushIntervalMs() { return flushIntervalMs; }
        public void setFlushIntervalMs(long flushIntervalMs) { this.flushIntervalMs = flushIntervalMs; }
    }

    // -------------------------------------------------------------------------
    // crystalframework.sharding
    // -------------------------------------------------------------------------
    public static class Sharding {
        private Snowflake snowflake = new Snowflake();

        public Snowflake getSnowflake() { return snowflake; }
        public void setSnowflake(Snowflake snowflake) { this.snowflake = snowflake; }

        public static class Snowflake {
            private long startPoint = 0;
            private int timestampLength = 41;
            private int dataCenterIdLength = 5;
            private int workerIdLength = 5;
            private int sequenceIdLength = 12;
            private int geneIdLength = 0;
            private long dataCenterId = 0;
            private long workerId = 0;
            private int actualGeneLength = 0;

            public long getStartPoint() { return startPoint; }
            public void setStartPoint(long startPoint) { this.startPoint = startPoint; }

            public int getTimestampLength() { return timestampLength; }
            public void setTimestampLength(int timestampLength) { this.timestampLength = timestampLength; }

            public int getDataCenterIdLength() { return dataCenterIdLength; }
            public void setDataCenterIdLength(int dataCenterIdLength) { this.dataCenterIdLength = dataCenterIdLength; }

            public int getWorkerIdLength() { return workerIdLength; }
            public void setWorkerIdLength(int workerIdLength) { this.workerIdLength = workerIdLength; }

            public int getSequenceIdLength() { return sequenceIdLength; }
            public void setSequenceIdLength(int sequenceIdLength) { this.sequenceIdLength = sequenceIdLength; }

            public int getGeneIdLength() { return geneIdLength; }
            public void setGeneIdLength(int geneIdLength) { this.geneIdLength = geneIdLength; }

            public long getDataCenterId() { return dataCenterId; }
            public void setDataCenterId(long dataCenterId) { this.dataCenterId = dataCenterId; }

            public long getWorkerId() { return workerId; }
            public void setWorkerId(long workerId) { this.workerId = workerId; }

            public int getActualGeneLength() { return actualGeneLength; }
            public void setActualGeneLength(int actualGeneLength) { this.actualGeneLength = actualGeneLength; }
        }
    }
}
