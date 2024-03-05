package com.kai.demo;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

// https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection#58773038
public class ClasspathScan {

    private static List<Class<?>> getClasses(final String pkgName) throws URISyntaxException, IOException {
        // 1. 获取包名相关的资源 URI (定位一个资源)
        // com/gkd/demo
        // jakarta/annotation
        final String pkgPath = pkgName.replace('.', '/');
        // file:/Users/kai/programs/spring-projects/winter-framework/step-by-step/resource-resolver/target/test-classes/com/gkd/demo
        // jar:file:/Users/kai/cs61b/CS61B-sp21/library-sp21/javalib/jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar!/jakarta/annotation
        final URI pkg = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(pkgPath)).toURI();
        final List<Class<?>> allClasses = new ArrayList<>();

        // 2. URI 转换为 Path (文件系统中定位一个文件)
        // /Users/kai/programs/spring-projects/winter-framework/step-by-step/resource-resolver/target/test-classes/com/gkd/demo
        // jakarta/annotation
        Path root;
        if (pkg.toString().startsWith("jar:")) {
            try {
                root = FileSystems.getFileSystem(pkg).getPath(pkgPath);
            } catch (Exception e) {
                root = FileSystems.newFileSystem(pkg, Collections.emptyMap()).getPath(pkgPath);
            }
        } else {
            root = Paths.get(pkg);
        }

        // 3. 深度优先遍历 root 下的所有目录和文件
        final String extension = ".class";
        try (final Stream<Path> allPaths = Files.walk(root)) {
            allPaths.filter(Files::isRegularFile).forEach(file -> {
                // /Users/kai/programs/spring-projects/winter-framework/step-by-step/resource-resolver/target/test-classes/com/gkd/demo/ClasspathScan.class
                final String path = file.toString().replace("/", ".");
                // com.gkd.demo.ClasspathScan
                final String name = path.substring(path.indexOf(pkgName), path.length() - extension.length());
                try {
                    allClasses.add(Class.forName(name));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return allClasses;
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("===scan class===");
        for (final Class<?> cls : getClasses(ClasspathScan.class.getPackageName())) {
            System.out.println(cls);
        }

        System.out.println("===scan jar===");
        for (final Class<?> cls : getClasses(PostConstruct.class.getPackageName())) {
            System.out.println(cls);
        }
    }
}
