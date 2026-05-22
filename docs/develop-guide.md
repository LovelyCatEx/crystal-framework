# 开发指引

## 0x00 工具准备

### Git

安装Git: [Windows](https://git-scm.com/install/windows) | [Mac](https://git-scm.com/install/mac) | [Linux](https://git-scm.com/install/linux)

### Docker

推荐使用 Docker Desktop: [Docker Desktop](https://www.docker.com/products/docker-desktop/)

## 0x01 克隆仓库

使用本项目进行开发一般有如下两种情况：

1. 修改本项目源代码
2. 基于本项目进行二次开发

针对不同的开发场景，请阅读下面的操作步骤。

### 修改本项目源码

首先使用命令行进入你想要将项目保存到的目录，然后执行以下命令：

```shell
git clone -b develop https://github.com/LovelyCatEx/crystal-framework.git
cd crystal-framework
git checkout -b [分支名]
```

### 二次开发

如果你只想使用本框架进行二次开发，请先 Fork 本仓库，然后使用下面的命令克隆你的仓库：

```shell
git clone https://github.com/[Owner 名称]/[仓库名称].git
cd [仓库名称]
```

后续步骤请看 **0x03 开发说明** 部分。

## 0x02 环境准备

本项目提供Docker / DevContainer 两种开发方式，任选其中之一即可。

两种准备环境的方式大体相同，区别在于 dev-server 容器是可选项。
由于 DevContainer 需要占用大量内存，性能配置较低的设备建议选择 Docker 进行开发。

### DevContainer

DevContainer 是一种基于 Docker 容器的开发环境方案，由微软主导推动，旨在为开发者提供‌一致、隔离、可复现‌的开发环境。它通过将项目所需的所有依赖（如语言运行时、工具链、库、配置等）封装在容器中，实现开发环境无关的目标。

#### 操作步骤

1. DevContainer 的配置在项目根目录的 `.devcontainer` 文件夹中，进入此文件夹。
2. 将里面的 `.env.example` 复制一份并改名为 `.env`，若非必要请不要随意修改里面的内容。
3. 完成上述步骤后回到项目根目录。
4. 再将项目根目录中的 `.env.example` 复制一份并改名为 `.env`。
5. 将上一步的 `.env` 文件中 `POSTGRES_HOST` 的值改为 `postgres`、`REDIS_HOST` 的值改成 `redis`、`SNAILJOB_SERVER_HOST` 的值改成 `snailjob`，保存。
6. 此时你可以启动后端项目，数据库将会自动初始化。

#### 挂载 Maven 仓库

DevContainer 容器默认使用的 Maven 仓库路径是 `.devcontainer/.m2/repository`，你可以将其改成本地已有的仓库，避免重复下载依赖。

一般情况下，Maven 默认的仓库路径是 `~/.m2/repository`，将 `./.m2/repository:/root/.m2/repository` 替换为 `~/.m2/repository:/root/.m2/repository` 即可。

### Docker

首先打开项目根目录下的 `.devcontainer` 文件夹。

将里面的 `.env.example` 复制一份并改名为 `.env`，若非必要请不要随意修改里面的内容。

然后使用下面的命令启动项目开发所需的环境。

```bash
docker compose up --scale dev-server=0 -d
```

然后将项目根目录下的 `.env.example` 复制一份，改名为 `.env`。

到这里你可以直接启动后端项目，数据库将会自动初始化。

## 0x03 开发说明

使用本项目进行开发一般有如下两种情况：

1. 修改本项目源代码
2. 基于本项目进行二次开发

针对不同的开发场景，请阅读下面的注意事项。

### 修改本项目

假设你已经完成了 **0x01 克隆仓库** 步骤。

接下来你可以在你自己创建的分支进行开发，建议一个功能/修改单独开一个分支进行。

当所有开发工作完成后，请向本项目发起一个 Pull Request (简称 PR)，从你的分支合并到 develop 分支。

::: warning
本项目只允许从你的分支合并到 develop 分支，PR 的标题请**尽量**遵守如下命名规范: [Commit 信息编写规范](https://www.conventionalcommits.org/zh-hans/v1.0.0/)
:::

### 二次开发

1. 打开项目根目录下的 `pom.xml`，按以下步骤配置：

```xml{12}
<modules>
    <module>crystal-shared</module>
    <module>crystal-audit</module>
    <module>crystal-schedule</module>
    <module>crystal-resource</module>
    <module>crystal-starter</module>
    <module>crystal-encrypt</module>
    <module>crystal-shared-types</module>
    <module>crystal-mail</module>
    <module>crystal-sdk</module>
    <module>ext-playground</module>
    <module>my-project</module>
</modules>
```

2. 在 `<properties>` 标签中添加版本属性：

```xml{7}
<properties>
    <java.version>17</java.version>
    <kotlin.version>2.3.21</kotlin.version>
    <snailjob.version>1.10.0</snailjob.version>
    <revision>1.0.0</revision>
    <ext.playground.version>0.0.1</ext.playground.version>
    <my.project.version>0.0.1</my.project.version> <!-- 添加新模块版本属性 -->
</properties>
```

3. 在 `<dependencyManagement>` 标签中添加依赖管理：

```xml{5-9}
package io.github.lovelycatex.myproject

import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

@Configuration
class MyProjectModule : InitializingBean {
    override fun afterPropertiesSet() {
        logger().info("Demo module loaded!")
    }
}
```

4. 在项目根目录下创建新模块文件夹，结构如下：

```
my-project/
├── pom.xml
└── src/
    └── main/
        ├── kotlin/
        │   └── io/
        │       └── github/
        │           └── lovelycatex/
        │               └── myproject/
        │                   ├── ExtDemoModule.kt
        │                   └── config/
        │                       └── DemoAutoConfiguration.kt
        └── resources/
            └── META-INF/
                └── spring/
                    └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

5. 创建模块的 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>core</artifactId>
        <version>${revision}</version>
    </parent>

    <artifactId>my-project</artifactId> <!-- 此处一定要和上面的 module 名称保持一致 -->
    <version>${my.project.version}</version> <!-- 此处一定要和上面的 properties 中填写的标签保持一致 -->

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>

        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <sourceDir>/src/main/kotlin</sourceDir>
                                <sourceDir>target/generated-sources/annotations</sourceDir>
                            </sourceDirs>
                        </configuration>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <sourceDir>/src/test/kotlin</sourceDir>
                                <sourceDir>target/generated-test-sources/test-annotations</sourceDir>
                            </sourceDirs>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>com.lovelycatv.crystalframework</groupId>
            <artifactId>crystal-sdk</artifactId>
        </dependency>
    </dependencies>
</project>
```

6. 修改 `crystal-starter/pom.xml`

```xml{35-38}
<dependencies>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-shared</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-shared-types</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-audit</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-schedule</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-resource</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-encrypt</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-mail</artifactId>
    </dependency>
    <!-- External Modules -->
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>ext-playground</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>my-project</artifactId> <!-- 此处一定要和上面的 module 名称保持一致 -->
    </dependency>
</dependencies>
```

7. 创建模块主类 ExtDemoModule.kt

```kotlin
package io.github.lovelycatex.myproject

import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

@Configuration
class MyProjectModule : InitializingBean {
    override fun afterPropertiesSet() {
        logger().info("Demo module loaded!")
    }
}
```

8. 创建自动配置类 DemoAutoConfiguration.kt

```kotlin
package io.github.lovelycatex.myproject

import com.lovelycatv.crystalframework.sdk.config.CrystalFrameworkPackageScanConfigurer
import io.github.lovelycatex.myproject.MyProjectModule
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@AutoConfiguration
@EnableR2dbcRepositories(basePackageClasses = [MyProjectModule::class])
class MyProjectAutoConfiguration {
    @Bean
    fun MyProjectPackageScanConfigurer(): CrystalFrameworkPackageScanConfigurer {
        return CrystalFrameworkPackageScanConfigurer { scan ->
            scan.scanBasePackage(MyProjectModule::class)
            scan.scanEntityPackage(MyProjectModule::class)
        }
    }
}
```

9. 创建 Spring 自动配置文件

在 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中添加：

```
io.github.lovelycatex.myproject.MyProjectAutoConfiguration
```

10. 验证模块

完成上述步骤后，执行以下命令验证模块是否能正常构建：

```bash
mvn install -DskipTests
```

如果构建成功，新模块就创建完成了。接下来你可以基于此模块进行业务开发。

初次之外，如果你已经准备好了开发所需的容器，也可以选择同步执行 Test 命令验证是否可用：

```bash
mvn clean install
```

更多关于 CrystalFrameworkSDK 的使用，请阅读其他文档。
