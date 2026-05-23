# 二次开发快速开始

## 前提

- 已部署并运行 Crystal Framework（后端服务可用）
- 已安装 JDK 17+ 和 Maven 3.9+

## 最小的插件

### 1. 创建 Maven 项目

目录结构：

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

### 2. 添加依赖

在 `pom.xml` 中添加 `crystal-sdk`：

```xml
<dependencies>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-sdk</artifactId>
        <version>1.2.0</version>
    </dependency>
</dependencies>
```

### 3. 实现插件主类

```kotlin
package io.github.lovelycatex.myplugin

import com.lovelycatv.crystalframework.sdk.CrystalFrameworkModule

class MyPlugin : CrystalFrameworkModule {
    override fun onEnabled() {
        println("MyPlugin loaded!")
    }
}
```

### 4. 创建 metadata.yml

```yaml
name: MyPlugin
main: io.github.lovelycatex.myplugin.MyPlugin
author: YourName
version: 1.0.0
```

### 5. 打包部署

```shell
mvn clean package
cp target/my-plugin-1.0-SNAPSHOT.jar /path/to/crystal-framework/ext/
```

重启框架，插件即被加载。

## 下一步

查看完整的 [二次开发指引](./develop-guide) 了解更多细节，包括编写业务组件和 SDK 功能的使用。
