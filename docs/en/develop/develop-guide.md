# Development Guide

Crystal Framework supports two development modes: **Integrated Mode** and **Standalone Mode**.

- **Integrated Mode**: The plugin module is a Maven submodule of this project, compiled together with the framework. Suitable for scenarios that require deep modification of the framework source code.
- **Standalone Mode (Recommended)**: The plugin is a completely independent Maven project that only depends on `crystal-sdk`. After packaging, simply place it in the framework's `/ext` directory for hot-loading. No need to modify any framework configuration files.

The following are the detailed steps for the Standalone mode.

---

## Step 1. Create an Independent Maven Project

Create a new Maven project **outside** the framework source directory with the following structure:

```
my-plugin/
├── pom.xml
└── src/
    └── main/
        ├── kotlin/
        │   └── io/
        │       └── github/
        │           └── lovelycatex/
        │               └── myplugin/
        │                   └── MyPlugin.kt
        └── resources/
            └── metadata.yml
```

## Step 2. Configure pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.lovelycatex</groupId>
    <artifactId>my-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <kotlin.code.style>official</kotlin.code.style>
        <kotlin.compiler.jvmTarget>1.8</kotlin.compiler.jvmTarget>
        <kotlin.version>2.3.20</kotlin.version>
    </properties>

    <repositories>
        <repository>
            <id>mavenCentral</id>
            <url>https://repo1.maven.org/maven2/</url>
        </repository>
    </repositories>

    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>com.lovelycatv.crystalframework</groupId>
            <artifactId>crystal-sdk</artifactId>
            <version>1.2.0</version>
        </dependency>
    </dependencies>

</project>
```

## Step 3. Create the Plugin Main Class

The plugin's main class must implement the `CrystalFrameworkModule` interface and provide a no-arg constructor.

```kotlin
package io.github.lovelycatex.myplugin

import com.lovelycatv.crystalframework.sdk.CrystalFrameworkModule
import com.lovelycatv.vertex.log.logger

class MyPlugin : CrystalFrameworkModule {
    override fun onEnabled() {
        logger().info("MyPlugin loaded!")
    }
}
```

The `onEnabled()` method is called when the plugin is loaded and can be used to initialize resources, register configurations, etc.

## Step 4. Create metadata.yml

Create `metadata.yml` in the `src/main/resources/` directory. It must contain the following four fields:

```yaml
name: MyPlugin
main: io.github.lovelycatex.myplugin.MyPlugin
author: YourName
version: 1.0.0
```

| Field | Description |
|-------|-------------|
| `name` | Plugin name |
| `main` | Fully qualified name of the main class (must implement `CrystalFrameworkModule`) |
| `author` | Author |
| `version` | Plugin version |

## Step 5. Write Business Components

You can use Spring annotations to write business components as usual. When the framework starts, it will automatically scan all `@Component`-stereotype classes (including `@Service`, `@Repository`, `@Controller`, `@Configuration`, etc.) in the plugin main class's package and register them as Spring Beans.

```kotlin
package io.github.lovelycatex.myplugin

import org.springframework.stereotype.Service

@Service
class MyPluginService {
    fun hello() = "Hello from MyPlugin!"
}
```

```kotlin
package io.github.lovelycatex.myplugin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(basePackageClasses = [MyPlugin::class])
class MyPluginConfiguration {
    @Bean
    fun myPluginBean() = "MyPluginBean"
}
```

## Step 6. Package and Deploy

Execute in the plugin project root directory:

```bash
mvn clean package
```

After packaging, the JAR file `my-plugin-1.0-SNAPSHOT.jar` will be generated in the `target/` directory. Copy it to the `ext/` folder in the framework's runtime directory:

```bash
cp target/my-plugin-1.0-SNAPSHOT.jar /path/to/crystal-framework/ext/
```

When the framework starts, it will automatically scan all JAR files in the `ext/` directory and load the plugins found inside.

## SDK Features

The framework provides a set of SDK modules that make it easy for plugins to leverage existing framework capabilities. Click the links below for detailed documentation:

- [System Settings](./sdk/system-settings)
- [System Permission](./sdk/system-permission)
- [Tenant Permission](./sdk/tenant-permission)
- [Mail Template](./sdk/mail-template)

## Integrated Mode

If your plugin needs to be compiled together with the framework source code (e.g., to modify the framework itself), you can add the plugin module as a submodule in the root `pom.xml`. The approach is the same as the standalone mode: create a Maven project, implement `CrystalFrameworkModule`, provide `metadata.yml`. The only difference is the build method.
