package cn.hlh.rpc.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 反射工具类
 */
public class ReflectUtil {
    //找到调用该反射工具类的主函数
    public static String getStackTrace(){
        StackTraceElement[] stack = new Throwable().getStackTrace();
        return stack[stack.length-1].getClassName();
    }

    //找到指定包下的所有类
    public static Set<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new LinkedHashSet<>();
        boolean recursive = true;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        //循环获取指定包下的所有文件
        while (dirs.hasMoreElements()){
            URL url = dirs.nextElement();
            // 得到协议的名称
            String protocol = url.getProtocol();
            // 如果是以文件的形式保存在服务器上
            if ("file".equals(protocol)) {
                // 获取包的物理路径
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                // 以文件的方式扫描整个包下的文件，并添加到集合中
                findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
            } else if ("jar".equals(protocol)) {
                // 以jar包的方式扫描整个包下的文件，并添加到集合中
                findAndAddClassesInPackageByJar(packageName,url,recursive,classes);
            }
        }
        return classes;
    }

    /**
     * 扫描file中的类文件，并将其加入集合classes
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     * @throws ClassNotFoundException
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) throws ClassNotFoundException {
        // 获取此包的目录，建立一个File
        File dir = new File(packagePath);
        // 如果不存在或不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在，就获取包下的所有文件（包括目录）
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则：可以循环(包含子目录)或以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录，则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件，则去掉.class只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                // 添加到集合中去
                //classes.add(Class.forName(packageName + '.' + className));
                //用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
            }
        }
    }

    /**
     * 扫描Jar包中的类文件，并将其加入集合classes
     * @param packageName
     * @param url
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByJar(String packageName,URL url,final boolean recursive,Set<Class<?>> classes) throws IOException, ClassNotFoundException {
        String packageDirName = packageName.replace('.', '/');
        // 定义一个JarFile
        JarFile jar;
        // 获取jar
        jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        // 对jar包中的实体进行循环迭代，实体可以是目录、类文件或其他文件（如META-INF等）
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
                // 如果是以/开头，则获取后面的字符串
                name = name.substring(1);
            }
            // 排除不同包的class文件
            if (name.startsWith(packageDirName)) {
                // 如果文件名中有"/"，则把"/"替换成"."
                int idx = name.lastIndexOf('/');
                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 再次判断是否是包中的文件并且可以迭代下去
                if ((idx != -1) || recursive) {
                    // 如果是一个.class文件
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        // 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        // 添加到classes集合中
                        classes.add(Class.forName(packageName + '.' + className));
                    }
                }
            }
        }
    }
}
