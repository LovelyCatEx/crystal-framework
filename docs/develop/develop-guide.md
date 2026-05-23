# 二次开发指引

Crystal Framework 支持两种开发模式：**集成模式** 与 **非集成模式**。

- **集成模式**：插件模块作为本项目的一个 Maven 子模块，与框架一同编译。适合需要深度修改框架源码的场景。
- **非集成模式（推荐）**：插件是一个完全独立的 Maven 项目，仅依赖 `crystal-sdk`，打包后放入框架的 `/ext` 目录即可热加载。无需修改框架的任何配置文件。

以下为非集成模式的详细步骤。

---

## 步骤1. 创建独立的 Maven 项目

在框架源码目录**之外**创建一个新的 Maven 项目，目录结构如下：

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

## 步骤2. 配置 pom.xml

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

## 步骤3. 创建插件主类

插件的主类必须实现 `CrystalFrameworkModule` 接口，并提供一个无参构造器。

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

`onEnabled()` 方法在插件加载时被调用，可用于初始化资源、注册配置等准备工作。

## 步骤4. 创建 metadata.yml

在 `src/main/resources/` 目录下创建 `metadata.yml`，必须包含以下四个字段：

```yaml
name: MyPlugin
main: io.github.lovelycatex.myplugin.MyPlugin
author: YourName
version: 1.0.0
```

| 字段 | 说明 |
|------|------|
| `name` | 插件名称 |
| `main` | 插件主类全限定名（必须实现 `CrystalFrameworkModule`） |
| `author` | 作者 |
| `version` | 插件版本 |

## 步骤5. 编写业务组件

你可以在插件中正常使用 Spring 的注解编写业务组件，框架启动时会自动扫描插件主类所在包下的所有 `@Component` 族类（包括 `@Service`、`@Repository`、`@Controller`、`@Configuration` 等）并注册为 Spring Bean。

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

## 步骤6. 打包并部署

在插件项目根目录执行：

```bash
mvn clean package
```

打包后会在 `target/` 目录下生成 `my-plugin-1.0-SNAPSHOT.jar`。将此 JAR 文件复制到框架运行目录的 `ext/` 文件夹中：

```bash
cp target/my-plugin-1.0-SNAPSHOT.jar /path/to/crystal-framework/ext/
```

框架启动时会自动扫描 `ext/` 目录下的所有 JAR 文件，加载其中的插件。

## SDK 功能模块

框架提供了一系列 SDK 功能模块，方便插件使用框架的既有能力。点击以下链接查看详细文档：

- [系统设置项](./sdk/system-settings)
- [系统权限](./sdk/system-permission)
- [租户权限](./sdk/tenant-permission)
- [邮件模板](./sdk/mail-template)

## 集成模式说明

如果你的插件需要与框架源码一起编译（例如需要修改框架本身），可以将插件模块添加到框架的根 `pom.xml` 中作为子模块。具体做法与非集成模式相同：创建 Maven 项目、实现 `CrystalFrameworkModule`、提供 `metadata.yml`。区别仅在于构建方式。
