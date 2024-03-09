package com.kai.winter.io;

/**
 * 文件
 *
 * @param path file:/Users/kai/programs/spring-projects/winter-framework/step-by-step/resource-resolver/target/test-classes/com/gkd/demo/ClasspathScan.class
 *             jar:file:/Users/kai/cs61b/CS61B-sp21/library-sp21/javalib/jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar!
 * @param name com/gkd/demo/ClasspathScan.class
 *             jakarta/annotation/Nullable.class
 */
public record Resource(String path, String name) {
}
