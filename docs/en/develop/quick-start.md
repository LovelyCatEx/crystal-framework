# Plugin Development Quick Start

## Prerequisites

- Crystal Framework backend is deployed and running
- JDK 17+ and Maven 3.9+ installed

## Minimal Plugin

### 1. Create a Maven Project

Directory structure:

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

### 2. Add Dependency

Add `crystal-sdk` to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.lovelycatv.crystalframework</groupId>
        <artifactId>crystal-sdk</artifactId>
        <version>1.2.0</version>
    </dependency>
</dependencies>
```

### 3. Implement the Plugin Main Class

```kotlin
package io.github.lovelycatex.myplugin

import com.lovelycatv.crystalframework.sdk.CrystalFrameworkModule

class MyPlugin : CrystalFrameworkModule {
    override fun onEnabled() {
        println("MyPlugin loaded!")
    }
}
```

### 4. Create metadata.yml

```yaml
name: MyPlugin
main: io.github.lovelycatex.myplugin.MyPlugin
author: YourName
version: 1.0.0
```

### 5. Package and Deploy

```shell
mvn clean package
cp target/my-plugin-1.0-SNAPSHOT.jar /path/to/crystal-framework/ext/
```

Restart the framework and your plugin will be loaded.

## Next Steps

See the full [Plugin Development Guide](./develop-guide) for more details, including writing business components and using SDK features.
