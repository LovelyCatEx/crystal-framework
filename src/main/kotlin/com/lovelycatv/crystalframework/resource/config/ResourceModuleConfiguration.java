package com.lovelycatv.crystalframework.resource.config;

import com.lovelycatv.crystalframework.resource.types.ResourceFileType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("crystalframework.resource")
public class ResourceModuleConfiguration {
    private Config avatar;

    public Config getAvatar() {
        return avatar;
    }

    public void setAvatar(Config avatar) {
        this.avatar = avatar;
    }

    public Config get(ResourceFileType fileType) {
        return switch (fileType) {
            case USER_AVATAR -> this.avatar;
        };
    }

    public static class Config {
        private String[] supportedContentTypes;

        public String[] getSupportedContentTypes() {
            return supportedContentTypes;
        }

        public void setSupportedContentTypes(String[] supportedContentTypes) {
            this.supportedContentTypes = supportedContentTypes;
        }
    }
}
