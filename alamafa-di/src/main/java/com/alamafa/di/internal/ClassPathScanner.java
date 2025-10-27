package com.alamafa.di.internal;

import com.alamafa.di.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.function.Predicate;

/**
 * 基于 classpath 的通用扫描器，可遍历目录与 JAR，配合谓词过滤结果。
 */
public final class ClassPathScanner {
    private ClassPathScanner() {
    }

    /**
     * 查找带有 {@link com.alamafa.di.annotation.Configuration} 的类。
     */
    public static Set<Class<?>> findConfigurationClasses(ClassLoader classLoader, String basePackage) {
        return findClasses(classLoader, basePackage, clazz -> clazz.isAnnotationPresent(Configuration.class));
    }

    /**
     * 按包名扫描所有类，并基于谓词过滤。
     */
    public static Set<Class<?>> findClasses(ClassLoader classLoader, String basePackage, Predicate<Class<?>> predicate) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null");
        }
        if (basePackage == null) {
            throw new IllegalArgumentException("basePackage must not be null");
        }
        if (predicate == null) {
            throw new IllegalArgumentException("predicate must not be null");
        }
        Set<Class<?>> classes = new LinkedHashSet<>();
        String path = basePackage.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    scanDirectory(new File(filePath), basePackage, classes, classLoader, predicate);
                } else if ("jar".equals(protocol)) {
                    scanJar(url, basePackage, classes, classLoader, predicate);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan classpath for package " + basePackage, e);
        }
        return classes;
    }

    /** 遍历文件系统目录并加载 class。 */
    private static void scanDirectory(File directory,
                                      String packageName,
                                      Set<Class<?>> classes,
                                      ClassLoader loader,
                                      Predicate<Class<?>> predicate) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes, loader, predicate);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                handleClass(className, loader, predicate, classes);
            }
        }
    }

    /** 扫描 JAR 包中的类条目。 */
    private static void scanJar(URL url,
                                String packageName,
                                Set<Class<?>> classes,
                                ClassLoader loader,
                                Predicate<Class<?>> predicate) {
        try {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                String packagePath = packageName.replace('.', '/');
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String name = entry.getName();
                    if (!name.startsWith(packagePath) || !name.endsWith(".class")) {
                        continue;
                    }
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    handleClass(className, loader, predicate, classes);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan JAR for package " + packageName, e);
        }
    }

    /** 尝试加载类并根据谓词筛选。 */
    private static void handleClass(String className,
                                    ClassLoader loader,
                                    Predicate<Class<?>> predicate,
                                    Set<Class<?>> classes) {
        try {
            Class<?> clazz = Class.forName(className, false, loader);
            if (predicate.test(clazz)) {
                classes.add(clazz);
            }
        } catch (ClassNotFoundException ignored) {
        } catch (LinkageError linkageError) {
            throw new IllegalStateException("Failed to load class while scanning: " + className, linkageError);
        }
    }
}
