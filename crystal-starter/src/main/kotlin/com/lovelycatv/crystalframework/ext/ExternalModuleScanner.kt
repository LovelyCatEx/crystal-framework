package com.lovelycatv.crystalframework.ext

import com.lovelycatv.crystalframework.sdk.CrystalFrameworkModule
import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.AnnotationConfigUtils
import org.springframework.context.annotation.ScannedGenericBeanDefinition
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.UrlResource
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.lang.reflect.Modifier
import java.net.URI
import java.net.URLClassLoader
import java.util.jar.JarFile

class ExternalModuleScanner(
    private val extDirPath: String,
) : BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    private val logger = logger()
    private var moduleClassLoader: ClassLoader? = null

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 200

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val extDir = File(extDirPath)
        if (!extDir.exists() || !extDir.isDirectory) {
            extDir.mkdirs()
            logger.debug("External module directory [{}] not found, skipping", extDirPath)
            return
        }

        val jarFiles = extDir.listFiles { f -> f.name.endsWith(".jar") } ?: return
        if (jarFiles.isEmpty()) {
            logger.debug("No JAR files found in [{}]", extDirPath)
            return
        }

        val jarUrls = jarFiles.map { it.toURI().toURL() }.toTypedArray()
        val classLoader = URLClassLoader(jarUrls, CrystalFrameworkModule::class.java.classLoader)
        this.moduleClassLoader = classLoader

        // Must set bean factory classloader before registering bean definitions,
        // otherwise other BeanDefinitionRegistryPostProcessors that resolve bean
        // types will fail to find external classes.
        if (registry is ConfigurableListableBeanFactory) {
            registry.beanClassLoader = classLoader
        }

        val originalTCCL = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = classLoader

            jarFiles.forEach { jarFile ->
                try {
                    loadPlugin(jarFile, classLoader, registry)
                } catch (e: Exception) {
                    logger.error("Failed to load plugin from [{}]", jarFile.name, e)
                }
            }
        } finally {
            Thread.currentThread().contextClassLoader = originalTCCL
        }
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        moduleClassLoader?.let { beanFactory.beanClassLoader = it }
    }

    private fun loadPlugin(
        jarFile: File,
        classLoader: URLClassLoader,
        registry: BeanDefinitionRegistry,
    ) {
        val metadata = readMetadata(jarFile) ?: return
        logger.info("Loading plugin [${jarFile.name}], " +
                "name: ${metadata.name}, " +
                "version: ${metadata.version}, " +
                "author: ${metadata.author}, " +
                "mainClass: ${metadata.main}"
        )

        val mainClass = try {
            Class.forName(metadata.main, false, classLoader)
        } catch (e: Exception) {
            logger.error("Plugin [{}]: main class [{}] not found in [{}]", metadata.name, metadata.main, jarFile.name)
            return
        }

        if (!CrystalFrameworkModule::class.java.isAssignableFrom(mainClass) ||
            mainClass.isInterface || Modifier.isAbstract(mainClass.modifiers)
        ) {
            logger.error("Plugin [{}]: main class [{}] must implement CrystalFrameworkModule", metadata.name, metadata.main)
            return
        }

        val basePackage = mainClass.getPackage()?.name ?: metadata.main.substringBeforeLast('.')

        try {
            val instance = mainClass.getDeclaredConstructor().newInstance() as CrystalFrameworkModule
            instance.onEnabled()
        } catch (e: Exception) {
            logger.error("Plugin [{}]: failed to enable", metadata.name, e)
            return
        }

        logger.info("Plugin [{} v{} by {}] loaded from [{}]",
            metadata.name, metadata.version, metadata.author, jarFile.name)

        registerBean(mainClass, jarFile, classLoader, registry)
        scanComponents(basePackage, classLoader, registry, metadata.main)
    }

    private fun readMetadata(jarFile: File): PluginMetadata? {
        JarFile(jarFile).use { jar ->
            val entry = jar.getJarEntry("metadata.yml") ?: run {
                logger.warn("No metadata.yml found in [{}], skipping", jarFile.name)
                return null
            }
            val yamlContent = jar.getInputStream(entry).use { it.readAllBytes().decodeToString() }
            return parseMetadata(yamlContent, jarFile.name)
        }
    }

    private fun parseMetadata(yamlContent: String, jarName: String): PluginMetadata? {
        val fields: Map<String, Any> = try {
            Yaml().load(yamlContent) as? Map<String, Any> ?: return null
        } catch (e: Exception) {
            logger.warn("Plugin in [{}]: failed to parse metadata.yml", jarName, e)
            return null
        }

        val name = fields["name"]?.toString()
        val main = fields["main"]?.toString()
        val author = fields["author"]?.toString()
        val version = fields["version"]?.toString()

        val missing = mutableListOf<String>().apply {
            if (name == null) add("name")
            if (main == null) add("main")
            if (author == null) add("author")
            if (version == null) add("version")
        }
        if (missing.isNotEmpty()) {
            logger.warn("Plugin in [{}]: metadata.yml missing required fields: {}", jarName, missing)
            return null
        }

        return PluginMetadata(name!!, main!!, author!!, version!!)
    }

    private fun registerBean(
        clazz: Class<*>,
        jarFile: File,
        classLoader: ClassLoader,
        registry: BeanDefinitionRegistry,
    ) {
        val entryName = clazz.name.replace('.', '/') + ".class"
        val resource = UrlResource(URI("jar:${jarFile.toURI()}!/$entryName"))
        val metadataReader = SimpleMetadataReaderFactory(classLoader).getMetadataReader(resource)
        val beanDef = ScannedGenericBeanDefinition(metadataReader)
        beanDef.resource = resource
        beanDef.source = clazz.name
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDef)
        val beanName = BeanDefinitionReaderUtils.generateBeanName(beanDef, registry)
        if (!registry.containsBeanDefinition(beanName)) {
            registry.registerBeanDefinition(beanName, beanDef)
        }
    }

    private fun scanComponents(
        basePackage: String,
        classLoader: ClassLoader,
        registry: BeanDefinitionRegistry,
        mainClassName: String,
    ) {
        val provider = CrystalFrameworkComponentProvider()
        provider.setResourceLoader(DefaultResourceLoader(classLoader))
        provider.findCandidateComponents(basePackage).forEach { candidate ->
            if (candidate.beanClassName != null && candidate.beanClassName != mainClassName) {
                try {
                    AnnotationConfigUtils.processCommonDefinitionAnnotations(candidate as AnnotatedBeanDefinition)
                    val name = BeanDefinitionReaderUtils.generateBeanName(candidate, registry)
                    if (!registry.containsBeanDefinition(name)) {
                        registry.registerBeanDefinition(name, candidate)
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to register [{}]: {}", candidate.beanClassName, e.message)
                }
            }
        }
    }

    private data class PluginMetadata(
        val name: String,
        val main: String,
        val author: String,
        val version: String,
    )
}
