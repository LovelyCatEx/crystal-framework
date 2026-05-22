# Development Guide

## 0x00 Tool Preparation

### Git

Install Git: [Windows](https://git-scm.com/install/windows) | [Mac](https://git-scm.com/install/mac) | [Linux](https://git-scm.com/install/linux)

### Docker

Recommended to use Docker Desktop: [Docker Desktop](https://www.docker.com/products/docker-desktop/)

## 0x01 Clone Repository

There are generally two scenarios for using this project:

1. Modify this project's source code
2. Secondary development based on this project

For different development scenarios, please read the following steps.

### Modify This Project's Source Code

First, use the command line to enter the directory where you want to save the project, then execute the following commands:

```shell
git clone -b develop https://github.com/LovelyCatEx/crystal-framework.git
cd crystal-framework
git checkout -b [branch-name]
```

### Secondary Development

If you only want to use this framework for secondary development, please first Fork this repository, then clone your repository using the following commands:

```shell
git clone https://github.com/[Owner-Name]/[Repository-Name].git
cd [Repository-Name]
```

Please proceed to **0x03 Development Notes** section for subsequent steps.

## 0x02 Environment Preparation

This project provides two development methods: Docker / DevContainer, you can choose either one.

The two environment preparation methods are mostly the same, the difference is that the dev-server container is optional.
Since DevContainer requires a lot of memory, devices with lower performance configurations are recommended to choose Docker for development.

### DevContainer

DevContainer is a Docker container-based development environment solution, led by Microsoft, aiming to provide developers with a **consistent, isolated, and reproducible** development environment. It achieves the goal of development environment independence by encapsulating all dependencies required by the project (such as language runtime, toolchain, libraries, configuration, etc.) in the container.

#### Operation Steps

1. The DevContainer configuration is in the `.devcontainer` folder of the project root directory, enter this folder.
2. Copy the `.env.example` inside and rename it to `.env`, unless necessary, please don't modify the contents arbitrarily.
3. After completing the above steps, return to the project root directory.
4. Then copy the `.env.example` in the project root directory and rename it to `.env`.
5. Change the value of `POSTGRES_HOST` in the `.env` file from the previous step to `postgres`, change the value of `REDIS_HOST` to `redis`, change the value of `SNAILJOB_SERVER_HOST` to `snailjob`, and save.
6. At this point, you can start the backend project, and the database will be automatically initialized.

#### Mount Maven Repository

The default Maven repository path used by the DevContainer container is `.devcontainer/.m2/repository`, you can change it to your existing local repository to avoid downloading dependencies repeatedly.

Generally, the default Maven repository path is `~/.m2/repository`, just replace `./.m2/repository:/root/.m2/repository` with `~/.m2/repository:/root/.m2/repository`.

### Docker

First, open the `.devcontainer` folder in the project root directory.

Copy the `.env.example` inside and rename it to `.env`, unless necessary, please don't modify the contents arbitrarily.

Then use the following command to start the environment required for project development.

```bash
docker compose up --scale dev-server=0 -d
```

Then copy the `.env.example` in the project root directory and rename it to `.env`.

At this point, you can directly start the backend project, and the database will be automatically initialized.

## 0x03 Development Notes

There are generally two scenarios for using this project:

1. Modify this project's source code
2. Secondary development based on this project

For different development scenarios, please read the following notes.

### Modify This Project

Assume you have completed the **0x01 Clone Repository** step.

Next, you can develop on your own created branch, it is recommended to open a separate branch for one feature/modification.

When all development work is completed, please initiate a Pull Request (PR for short) to this project, merging from your branch to the develop branch.

::: warning
This project only allows merging from your branch to the develop branch, please **try** to follow the following naming convention for PR titles: [Commit Message Writing Convention](https://www.conventionalcommits.org/en/v1.0.0/)
:::

### Secondary Development

**Step1. Open the `pom.xml` in the project root directory and configure according to the following steps**

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

**Step2. Add version property in `<properties>` tag**

```xml{7}
<properties>
    <java.version>17</java.version>
    <kotlin.version>2.3.21</kotlin.version>
    <snailjob.version>1.10.0</snailjob.version>
    <revision>1.0.0</revision>
    <ext.playground.version>0.0.1</ext.playground.version>
    <my.project.version>0.0.1</my.project.version> <!-- Add new module version property -->
</properties>
```

**Step3. Add dependency management in `<dependencyManagement>` tag**

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

**Step4. Create a new module folder in the project root directory with the following structure**

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

**Step5. Create module's pom.xml**

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

    <artifactId>my-project</artifactId> <!-- Must be consistent with the module name above -->
    <version>${my.project.version}</version> <!-- Must be consistent with the property tag filled in properties above -->

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

**Step6. Modify `crystal-starter/pom.xml`**

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
        <artifactId>my-project</artifactId> <!-- Must be consistent with the module name above -->
    </dependency>
</dependencies>
```

**Step7. Create module main class MyProjectDemoModule.kt**

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

**Step8. Create auto-configuration class MyProjectAutoConfiguration.kt**

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

**Step9. Create Spring auto-configuration file**

Add to `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
io.github.lovelycatex.myproject.MyProjectAutoConfiguration
```

**Step10. Verify the module**

After completing the above steps, execute the following command to verify if the module can be built normally:

```bash
mvn install -DskipTests
```

If the build is successful, the new module is created. Next, you can develop business based on this module.

In addition, if you have prepared the containers required for development, you can also choose to execute Test commands simultaneously to verify usability:

```bash
mvn clean install
```

For more usage of CrystalFrameworkSDK, please read other documents.
