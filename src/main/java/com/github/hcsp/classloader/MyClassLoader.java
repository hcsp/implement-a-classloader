package com.github.hcsp.classloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyClassLoader extends ClassLoader {
    // 存放字节码文件的目录
    private final File bytecodeFileDirectory;

    public MyClassLoader(File bytecodeFileDirectory) {
        this.bytecodeFileDirectory = bytecodeFileDirectory;
    }

    public List<File> getBytecodeFiles(File file) {
        List<File> fileList = new ArrayList<>();
        for (File isolateFile : Objects.requireNonNull(file.listFiles())) {
            if (isolateFile.isFile() && isolateFile.getName().endsWith(".class")) {
                fileList.add(isolateFile);
            }

            if (isolateFile.isDirectory()) {
                fileList.addAll(getBytecodeFiles(isolateFile));
            }
        }
        return fileList;
    }

    // 还记得类加载器是做什么的么？
    // "从外部系统中，加载一个类的定义（即Class对象）"
    // 请实现一个自定义的类加载器，将当前目录中的字节码文件加载成为Class对象
    // 提示，一般来说，要实现自定义的类加载器，你需要覆盖以下方法，完成：
    //
    // 1.如果类名对应的字节码文件存在，则将它读取成为字节数组
    //   1.1
    // 2.如果类名对应的字节码文件不存在，则抛出ClassNotFoundException
    //
    // 一个用于测试的字节码文件可以在本项目的根目录找到
    //
    // 请思考：双亲委派加载模型在哪儿？为什么我们没有处理？
    // 双亲委派加载模型主要用于 Java 内置的类的 loader，方法 loadClass 中包括了双亲委派模型
    // 扩展阅读：ClassLoader类的Javadoc文档
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            List<File> bytecodeFileList = getBytecodeFiles(bytecodeFileDirectory)
                    .stream()
                    .filter(file -> file.getName().contains(name))
                    .collect(Collectors.toList());
            if (bytecodeFileList.size() > 0) {
                File bytecodeFile = bytecodeFileList.get(0);
                byte[] bytes = Files.readAllBytes(bytecodeFile.toPath());
                return defineClass(name, bytes, 0, bytes.length);

            } else {
                throw new ClassNotFoundException(name);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        File projectRoot = new File(System.getProperty("basedir", System.getProperty("user.dir")));
        MyClassLoader myClassLoader = new MyClassLoader(projectRoot);

        Class testClass = myClassLoader.loadClass("com.github.hcsp.MyTestClass");
        Object testClassInstance = testClass.getConstructor().newInstance();
        String message = (String) testClass.getMethod("sayHello").invoke(testClassInstance);
        System.out.println(message);
    }
}
