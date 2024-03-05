package com.kai.winter.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * a classpath scan to scan classes or files in dir and jar.
 */
public class ResourceResolver {

    Logger logger = LoggerFactory.getLogger(getClass());

    String basePackage;

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    public <R> List<R> scan(Function<Resource, R> mapper) {
        String basePackagePath = this.basePackage.replace(".", "/");
        List<R> collector = new ArrayList<>();
        try {
            scan0(basePackagePath, collector, mapper);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return collector;
    }

    /**
     * 加载 basePackagePath 路径下的所有文件，通过 mapper 将其转换，最后输出到 collector 中
     * 1. 使用 ClassLoader 获取所有的 basePackage 资源
     * 2. 将获取到的资源全部都转换为 Resource
     * 3. 通过 mapper 将所有的 Resource 转换为 List<R>
     */
    <R> void scan0(String basePackagePath, List<R> collector, Function<Resource, R> mapper) throws IOException, URISyntaxException {
        logger.atDebug().log("scan path: {}", basePackagePath);
        Enumeration<URL> resources = getClassLoader().getResources(basePackagePath);
        while (resources.hasMoreElements()) {
            URI uri = resources.nextElement().toURI();
            String uriStr = removeTrailingSlash(uriToString(uri));
            String uriBase = uriStr.substring(0, uriStr.length() - basePackagePath.length());
            if (uriBase.startsWith("file:")) {
                uriBase = uriBase.substring(5);
            }
            if (uriBase.startsWith("jar:")) {
                scanFile(true, jarUriToPath(basePackagePath, uri), uriBase, collector, mapper);
            } else {
                scanFile(false, Path.of(uri), uriBase, collector, mapper);
            }
        }
    }

    <R> void scanFile(boolean isJar, Path root, String base, List<R> collector, Function<Resource, R> mapper) throws IOException {
        String baseDir = removeTrailingSlash(base);
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                Resource resource;
                if (isJar) {
                    // jar:file:/Users/kai/cs61b/CS61B-sp21/library-sp21/javalib/jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar!
                    // jakarta/annotation/Nullable.class
                    resource = new Resource(baseDir, removeLeadingSlash(file.toString()));
                } else {
                    // file:/Users/kai/programs/spring-projects/winter-framework/step-by-step/resource-resolver/target/test-classes/com/gkd/demo/ClasspathScan.class
                    // com/gkd/demo/ClasspathScan.class
                    resource = new Resource("file:" + file.toString(),
                            removeLeadingSlash(file.toString().substring(baseDir.length())));
                }
                logger.atDebug().log("found resource: {}", resource);
                R r = mapper.apply(resource);
                if (r != null) {
                    collector.add(r);
                }
            });
        }
    }

    /**
     * 获取 ClassLoader，用于加载类或者资源
     */
    ClassLoader getClassLoader() {
        // 1. 获取 JVM 提供的 ClassLoader，在 ClassPath 中搜索
        // 或者获取 Servlet 容器专属的 ClassLoader，在 /WEB-INF/classes目录和 /WEB-INF/lib 的所有jar包搜索
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 2. 如果没有获取到当前线程的上下文 ClassLoader, 则在当前 Class 中获取
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader;
    }

    /**
     * URI 转换为 String
     */
    String uriToString(URI uri) {
        return URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
    }

    Path jarUriToPath(String basePackagePath, URI jarUri) throws IOException {
        return FileSystems.newFileSystem(jarUri, Map.of()).getPath(basePackagePath);
    }

    String removeLeadingSlash(String s) {
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }

    String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
