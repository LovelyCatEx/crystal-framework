package com.lovelycatv.crystalframework.resource.config;

import com.lovelycatv.crystalframework.resource.types.ResourceFileType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("crystalframework.resource")
public class ResourceModuleConfiguration {
    private Config avatar;
    private Config tenantIcon;

    public Config getAvatar() {
        return avatar;
    }

    public Config getTenantIcon() {
        return tenantIcon;
    }

    public void setAvatar(Config avatar) {
        this.avatar = avatar;
    }

    public void setTenantIcon(Config tenantIcon) {
        this.tenantIcon = tenantIcon;
    }

    public Config get(ResourceFileType fileType) {
        return switch (fileType) {
            case USER_AVATAR -> this.avatar;
            case TENANT_ICON -> this.tenantIcon;
        };
    }

    public static class Config {
        private String[] supportedContentTypes = new String[0];

        public String[] getSupportedContentTypes() {
            return supportedContentTypes;
        }

        public void setSupportedContentTypes(String[] supportedContentTypes) {
            this.supportedContentTypes = supportedContentTypes;
        }
    }
}
