package com.github.hcsp.classloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MyClassLoader extends ClassLoader {
    // 存放字节码文件的目录
    private final File bytecodeFileDirectory;
    private final String fileExtension = ".class";

    public MyClassLoader(File bytecodeFileDirectory) {
        this.bytecodeFileDirectory = bytecodeFileDirectory;
    }

    // 还记得类加载器是做什么的么？
    // "从外部系统中，加载一个类的定义（即Class对象）"
    // 请实现一个自定义的类加载器，将当前目录中的字节码文件加载成为Class对象
    // 提示，一般来说，要实现自定义的类加载器，你需要覆盖以下方法，完成：
    //
    // 1.如果类名对应的字节码文件存在，则将它读取成为字节数组
    //   1.1 调用ClassLoader.defineClass()方法将字节数组转化为Class对象
    // 2.如果类名对应的字节码文件不存在，则抛出ClassNotFoundException
    //
    // 一个用于测试的字节码文件可以在本项目的根目录找到
    //
    // 请思考：双亲委派加载模型在哪儿？为什么我们没有处理？
    // 扩展阅读：ClassLoader类的Javadoc文档
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = null;
        try {
            data = loadClassData(name);
        } catch (Exception e) {
        // catch (ClassNotFoundException e)
        // catch (ReflectiveOperationException e)
        // above exceptions are not working, why??
            throw new ClassNotFoundException(e.getMessage());
        }

        return this.defineClass(name, data, 0, data.length);
    }

    private byte[] loadClassData(String name) throws IOException {
        return Files.readAllBytes(new File(name + fileExtension).toPath());
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
