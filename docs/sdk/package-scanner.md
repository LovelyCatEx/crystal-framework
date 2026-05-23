# 包扫描器

要让 Spring 框架扫描到模块中的组件就必须使用包扫描器，为了避免直接侵入本框架的源代码，因此提供了配置式的包扫描器定义。

## 包扫描器配置

首先在模块的根目录任意创建一个类，这里命名为 `ExtPlaygroundModule`

然后在任意位置创建一个 config 类：

```kotlin
package io.github.lovelycatex.playground.config

@AutoConfiguration
@EnableR2dbcRepositories(basePackageClasses = [ExtPlaygroundModule::class])
class PlaygroundAutoConfiguration {
    @Bean
    fun playgroundPackageScanConfigurer(): CrystalFrameworkPackageScanConfigurer {
        return CrystalFrameworkPackageScanConfigurer { scan ->
            scan.scanBasePackage(ExtPlaygroundModule::class)
            scan.scanEntityPackage(ExtPlaygroundModule::class)
        }
    }
}
```

## 自动配置

接着来到模块中的 `resource` 文件夹，创建 `META-INF/spring` 文件夹。

然后创建一个空白文件并且命名为 `org.springframework.boot.autoconfigure.AutoConfiguration.imports`，请注意这个文件不要带有任何后缀名（可以认为最后的 `.imports` 就是后缀名）

最后将配置类的**全限定名（[包名].[类名]）**写入该文件中即可：`io.github.lovelycatex.playground.config.PlaygroundAutoConfiguration`（请根据实际情况填写）