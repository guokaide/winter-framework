package com.kai.winter.io;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.sub.AnnoScan;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceResolverTest {

    @Test
    public void scanClass() {
        var pkg = "com.kai.scan";
        ResourceResolver resolver = new ResourceResolver(pkg);
        List<String> classes = resolver.scan(resource -> {
            String name = resource.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        Collections.sort(classes);
        System.out.println(classes);
        assertTrue(classes.contains("com.kai.scan.convert.ValueConverterBean"));
    }

    @Test
    public void scanJar() {
        var pkg = PostConstruct.class.getPackageName();
        var resolver = new ResourceResolver(pkg);
        List<String> classes = resolver.scan(resource -> {
            String name = resource.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        assertTrue(classes.contains(PostConstruct.class.getName()));
        assertTrue(classes.contains(AnnoScan.class.getName()));
    }

    @Test
    public void scanTxt() {
        var pkg = "com.kai.scan";
        var resolver = new ResourceResolver(pkg);
        List<String> files = resolver.scan(resource -> {
            String name = resource.name();
            if (name.endsWith(".txt")) {
                return name.replace("\\", "/");
            }
            return null;
        });
        Collections.sort(files);
        System.out.println(files);
    }
}