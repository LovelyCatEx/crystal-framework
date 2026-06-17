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
