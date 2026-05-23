---
name: vitepress-markdown
description: VitePress Markdown 扩展语法参考，包括 {{}} 转义、代码块、自定义容器等常用功能。
---

# VitePress Markdown 参考

## Mustache `{{}}` 转义

VitePress 使用 Vue 模板，`{{ }}` 会被 Vue 解析为插值表达式。**在文档中展示 `{{变量名}}` 这类占位符时必须转义。**

### 行内转义：`<code v-pre>`

```markdown
使用 <code v-pre>{{变量名}}</code> 格式声明占位符。
```

### 块级转义：`::: v-pre`

```markdown
::: v-pre
{{ 这里的内容会原样显示 }}
:::
```

### 代码块（自动转义）

围栏代码块默认自动包裹 `v-pre`，无需额外处理：

````markdown
```kotlin
title = "Order {{order_no}} Paid Successfully"
```
````

### 反例：行内反引号会消失

```markdown
使用 `{{变量名}}` 格式声明占位符。  <!-- ❌ {{}} 会被 Vue 解析导致消失 -->
```

## 代码块功能

### 语法高亮

使用 Shiki，在开头的 fence 后指定语言别名：

````
```kotlin
```
````

### 行高亮

````
```kotlin{4}
```             // 高亮第 4 行
````

````
```kotlin{5-8}
```             // 高亮第 5~8 行
````

````
```kotlin{4,7,9}
```             // 高亮第 4、7、9 行
````

也支持行内注释标记：

```kotlin
// [!code highlight]
```

### Diff 标记

```kotlin
// [!code --]  // 标记删除（红色）
// [!code ++]  // 标记新增（绿色）
```

### 错误和警告标记

```kotlin
// [!code warning]
// [!code error]
```

### Focus 标记

```kotlin
// [!code focus]        // 模糊其他行
// [!code focus:3]      // 聚焦接下来的 3 行
```

### 行号

全局开启：`markdown.lineNumbers: true`

按块覆盖：

````
```kotlin{4}:line-numbers
```             // 显示行号
````

````
```kotlin:no-line-numbers
```             // 不显示行号
````

````
```kotlin:line-numbers=2
```             // 从 2 开始编号
````

### 导入代码片段

```markdown
<<< @/src/main/kotlin/com/example/Example.kt
```

支持行高亮：

```markdown
<<< @/src/main/kotlin/com/example/Example.kt{2}
```

支持 VS Code region：

```markdown
<<< @/src/main/kotlin/com/example/Example.kt#regionName
```

支持指定语言：

```markdown
<<< @/snippets/snippet.cs{c#}
```

## 代码组

````markdown
::: code-group

```kotlin [build.gradle.kts]
dependencies {
    implementation("com.example:lib:1.0")
}
```

```xml [pom.xml]
<dependency>
    <groupId>com.example</groupId>
    <artifactId>lib</artifactId>
    <version>1.0</version>
</dependency>
```

:::
````

## 导入 Markdown 文件

```markdown
<!--@include: ./path/to/file.md-->
```

支持行范围：

```markdown
<!--@include: ./path/to/file.md{3,}-->   <!-- 第 3 行到末尾 -->
<!--@include: ./path/to/file.md{,10}-->   <!-- 第 1 到 10 行 -->
<!--@include: ./path/to/file.md{1,10}-->  <!-- 第 1 到 10 行 -->
```

## 自定义容器

```markdown
::: info
这是一个 info 容器。
:::

::: tip
这是一个 tip 容器。
:::

::: warning
这是一个 warning 容器。
:::

::: danger
这是一个 danger 容器。
:::

::: details
这是一个可折叠的 details 容器。
:::
```

自定义标题：

```markdown
::: danger STOP
危险区域，请勿操作。
:::
```

### `raw` 容器

用于防止 VitePress 的样式和路由冲突：

```markdown
::: raw
<div>不会受 VitePress 样式影响</div>
:::
```

也可直接在元素上使用 `vp-raw` class。

## GitHub-Flavored Alerts

```markdown
> [!NOTE] 注意
> 这是一个 NOTE 提示。

> [!TIP] 提示
> 这是一个 TIP 提示。

> [!IMPORTANT] 重要
> 这是一个 IMPORTANT 提示。

> [!WARNING] 警告
> 这是一个 WARNING 提示。

> [!CAUTION] 小心
> 这是一个 CAUTION 提示。
```

## 其他

### 标题锚点

标题自动生成锚点链接，可通过 `{#custom-anchor}` 自定义：

```markdown
## 自定义锚点 {#my-custom-anchor}
```

### Emoji

```markdown
:tada: :100:
```

### 数学公式

需要安装 `markdown-it-mathjax3` 并在配置中启用 `markdown.math: true`。

### 图片懒加载

配置 `markdown.image.lazyLoading: true` 开启。

## 参考链接

- [VitePress Markdown 扩展](https://vitepress.dev/zh/guide/markdown)
- [在 Markdown 中使用 Vue](https://vitepress.dev/zh/guide/using-vue)
