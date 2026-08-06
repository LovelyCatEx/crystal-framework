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
    private Database database = new Database();
    private Test test = new Test();
    private MessageChannel messageChannel = new MessageChannel();

    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public Monitor getMonitor() { return monitor; }
    public void setMonitor(Monitor monitor) { this.monitor = monitor; }

    public Sharding getSharding() { return sharding; }
    public void setSharding(Sharding sharding) { this.sharding = sharding; }

    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }

    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }

    public MessageChannel getMessageChannel() { return messageChannel; }
    public void setMessageChannel(MessageChannel messageChannel) { this.messageChannel = messageChannel; }

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
        private ResourceConfig tenantMemberAvatar = new ResourceConfig();

        public ResourceConfig getAvatar() { return avatar; }
        public void setAvatar(ResourceConfig avatar) { this.avatar = avatar; }

        public ResourceConfig getTenantIcon() { return tenantIcon; }
        public void setTenantIcon(ResourceConfig tenantIcon) { this.tenantIcon = tenantIcon; }

        public ResourceConfig getTenantMemberAvatar() { return tenantMemberAvatar; }
        public void setTenantMemberAvatar(ResourceConfig tenantMemberAvatar) {
            this.tenantMemberAvatar = tenantMemberAvatar;
        }

        public static class ResourceConfig {
            private String[] supportedContentTypes = new String[0];
            private String[] supportedFileExtensions = new String[0];

            public String[] getSupportedContentTypes() { return supportedContentTypes; }
            public void setSupportedContentTypes(String[] supportedContentTypes) {
                this.supportedContentTypes = supportedContentTypes;
            }

            public String[] getSupportedFileExtensions() { return supportedFileExtensions; }
            public void setSupportedFileExtensions(String[] supportedFileExtensions) {
                this.supportedFileExtensions = supportedFileExtensions;
            }
        }

        /**
         * Base64-encoded HMAC key used to sign short-lived download URLs for non-public
         * local file resources (see LocalFileResourceController#readLocalFile).
         * Must be set; the application fails to start when blank.
         */
        private String signingKey = "";

        /** Signed download URL validity in seconds. Default 300 (5 minutes). */
        private long signedUrlTtlSeconds = 300;

        public String getSigningKey() { return signingKey; }
        public void setSigningKey(String signingKey) { this.signingKey = signingKey; }

        public long getSignedUrlTtlSeconds() { return signedUrlTtlSeconds; }
        public void setSignedUrlTtlSeconds(long signedUrlTtlSeconds) {
            this.signedUrlTtlSeconds = signedUrlTtlSeconds;
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
            private long dataCenterId = 0;
            private long workerId = 0;
            private boolean autoAllocate = true;
            private String leaseKeyPrefix = "crystal:snowflake:lease:";
            private long leaseTtlMillis = 15000;
            private long leaseRenewIntervalMillis = 5000;
            private int actualGeneLength = 0;

            public long getStartPoint() { return startPoint; }
            public void setStartPoint(long startPoint) { this.startPoint = startPoint; }

            public int getTimestampLength() { return timestampLength; }
            public void setTimestampLength(int timestampLength) { this.timestampLength = timestampLength; }

            public int getDataCenterIdLength() { return dataCenterIdLength; }
            public void setDataCenterIdLength(int dataCenterIdLength) { this.dataCenterIdLength = dataCenterIdLength; }

            public int getWorkerIdLength() { return workerIdLength; }
            public void setWorkerIdLength(int workerIdLength) { this.workerIdLength = workerIdLength; }

            public long getDataCenterId() { return dataCenterId; }
            public void setDataCenterId(long dataCenterId) { this.dataCenterId = dataCenterId; }

            public long getWorkerId() { return workerId; }
            public void setWorkerId(long workerId) { this.workerId = workerId; }

            public boolean getAutoAllocate() { return autoAllocate; }
            public boolean isAutoAllocate() { return autoAllocate; }
            public void setAutoAllocate(boolean autoAllocate) { this.autoAllocate = autoAllocate; }

            public String getLeaseKeyPrefix() { return leaseKeyPrefix; }
            public void setLeaseKeyPrefix(String leaseKeyPrefix) { this.leaseKeyPrefix = leaseKeyPrefix; }

            public long getLeaseTtlMillis() { return leaseTtlMillis; }
            public void setLeaseTtlMillis(long leaseTtlMillis) { this.leaseTtlMillis = leaseTtlMillis; }

            public long getLeaseRenewIntervalMillis() { return leaseRenewIntervalMillis; }
            public void setLeaseRenewIntervalMillis(long leaseRenewIntervalMillis) {
                this.leaseRenewIntervalMillis = leaseRenewIntervalMillis;
            }
        }
    }

    // -------------------------------------------------------------------------
    // crystalframework.database
    // -------------------------------------------------------------------------
    public static class Database {
        private String defaultDataSource = "primary";
        private DataSource[] dataSources = new DataSource[0];
        private Routing routing = new Routing();

        public String getDefaultDataSource() { return defaultDataSource; }
        public void setDefaultDataSource(String defaultDataSource) { this.defaultDataSource = defaultDataSource; }

        public DataSource[] getDataSources() { return dataSources; }
        public void setDataSources(DataSource[] dataSources) { this.dataSources = dataSources; }

        public Routing getRouting() { return routing; }
        public void setRouting(Routing routing) { this.routing = routing; }

        public static class DataSource {
            private String name = "";
            private String url = "";
            private String username = "";
            private String password = "";
            private Pool pool = new Pool();

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getUrl() { return url; }
            public void setUrl(String url) { this.url = url; }

            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }

            public String getPassword() { return password; }
            public void setPassword(String password) { this.password = password; }

            public Pool getPool() { return pool; }
            public void setPool(Pool pool) { this.pool = pool; }

            public static class Pool {
                private int initialSize = 5;
                private int minIdle = 5;
                private int maxSize = 40;
                private Duration maxIdleTime = Duration.ofMinutes(30);
                private Duration maxAcquireTime = Duration.ofSeconds(5);
                private Duration maxCreateConnectionTime = Duration.ofSeconds(5);
                private Duration maxLifeTime = null;
                private Duration maxValidationTime = null;
                private String validationQuery = "SELECT 1";
                private int acquireRetry = 1;
                private String validationDepth = "REMOTE";

                public int getInitialSize() { return initialSize; }
                public void setInitialSize(int initialSize) { this.initialSize = initialSize; }

                public int getMinIdle() { return minIdle; }
                public void setMinIdle(int minIdle) { this.minIdle = minIdle; }

                public int getMaxSize() { return maxSize; }
                public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

                public Duration getMaxIdleTime() { return maxIdleTime; }
                public void setMaxIdleTime(Duration maxIdleTime) { this.maxIdleTime = maxIdleTime; }

                public Duration getMaxAcquireTime() { return maxAcquireTime; }
                public void setMaxAcquireTime(Duration maxAcquireTime) { this.maxAcquireTime = maxAcquireTime; }

                public Duration getMaxCreateConnectionTime() { return maxCreateConnectionTime; }
                public void setMaxCreateConnectionTime(Duration maxCreateConnectionTime) {
                    this.maxCreateConnectionTime = maxCreateConnectionTime;
                }

                public Duration getMaxLifeTime() { return maxLifeTime; }
                public void setMaxLifeTime(Duration maxLifeTime) { this.maxLifeTime = maxLifeTime; }

                public Duration getMaxValidationTime() { return maxValidationTime; }
                public void setMaxValidationTime(Duration maxValidationTime) {
                    this.maxValidationTime = maxValidationTime;
                }

                public String getValidationQuery() { return validationQuery; }
                public void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }

                public int getAcquireRetry() { return acquireRetry; }
                public void setAcquireRetry(int acquireRetry) { this.acquireRetry = acquireRetry; }

                public String getValidationDepth() { return validationDepth; }
                public void setValidationDepth(String validationDepth) { this.validationDepth = validationDepth; }
            }
        }

        public static class Routing {
            private boolean logDecisions = true;

            public boolean isLogDecisions() { return logDecisions; }
            public void setLogDecisions(boolean logDecisions) { this.logDecisions = logDecisions; }
        }
    }

    // -------------------------------------------------------------------------
    // crystalframework.test
    // -------------------------------------------------------------------------
    public static class Test {
        private SMTP smtp = new SMTP();
        private MessageChannel messageChannel = new MessageChannel();

        public SMTP getSmtp() { return smtp; }
        public void setSmtp(SMTP smtp) { this.smtp = smtp; }

        public MessageChannel getMessageChannel() { return messageChannel; }
        public void setMessageChannel(MessageChannel messageChannel) { this.messageChannel = messageChannel; }

        public static class SMTP {
            private String subject = "Crystal Framework SMTP Test";
            private String content = "This is a test email from Crystal Framework. If you received this message, your SMTP settings are working.";

            public String getSubject() { return subject; }
            public void setSubject(String subject) { this.subject = subject; }

            public String getContent() { return content; }
            public void setContent(String content) { this.content = content; }
        }

        public static class MessageChannel {
            private Lark lark = new Lark();

            public Lark getLark() { return lark; }
            public void setLark(Lark lark) { this.lark = lark; }

            public static class Lark {
                private String defaultMessage = "hello, world! <link href=\"https://github.com/LovelyCatEx/crystal-framework\" title=\"Crystal Framework\" />";

                public String getDefaultMessage() { return defaultMessage; }
                public void setDefaultMessage(String defaultMessage) { this.defaultMessage = defaultMessage; }
            }
        }
    }

    // -------------------------------------------------------------------------
    // crystalframework.message-channel
    // -------------------------------------------------------------------------
    public static class MessageChannel {
        /**
         * Base64-encoded AES key used to encrypt sensitive fields (e.g. password / appSecret)
         * inside the {@code config} blob of the {@code tenant_message_channels} table.
         * Must be set; the application fails to start when blank.
         */
        private String encryptionKey = "";

        public String getEncryptionKey() { return encryptionKey; }
        public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }
    }
}
