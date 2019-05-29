package org.pf4j.demo;

import org.pf4j.ExtensionFactory;
import org.pf4j.spring.SingletonSpringExtensionFactory;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SystemPluginManager extends SpringPluginManager {
    private static final Logger log = LoggerFactory.getLogger(SystemPluginManager.class);

    public SystemPluginManager() {
    }
    public SystemPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new SingletonSpringExtensionFactory(this);
    }

    /**
     * 指定系统插件解压目录为插件根目录
     */
    @Override
    protected Path createPluginsRoot() {
        String pluginsDir = System.getProperty("pf4j.systemPluginsDir");
        if (pluginsDir == null) {
            if (isDevelopment()) {
                pluginsDir = "../.system_plugins";
            } else {
                pluginsDir = ".system_plugins";
            }
        }
        Path path=Paths.get(pluginsDir);
        if(Files.notExists(path)){
            try {
                path = Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    @Override
    public void loadPlugins() {
        this.extractSystemPlugins();
        super.loadPlugins();
    }
    /**
     * 加载系统插件。系统插件是通过MAVEN DEPENDENCY方式在编译打包时加入到系统执行文件中，在系统classpath下，即SpringBoot的可执行
     * JAR包中的BOOT-INF/lib目录下。
     * 查找系统插件JAR包时，由于extension在编译时都会自动生成META-INF/extensions.idx文件，因此首先搜寻所有的extension.idx资源；
     * 按照我们的开发规范，插件必须以JAR包方式存在，因此只要形如jar:file/.../xxx.jar!/..../yyy.jar!/.../extension.idx的资源才是
     * 来自插件包。
     * 接下来在处理系统插件时，将该插件JAR包解压到.system_plugins目录下后进行加载。
     *
     * */

    private void extractSystemPlugins() {
        try {
            Enumeration<URL> urls = getClass().getClassLoader().getResources("META-INF/extensions.idx");
            System.out.println(String.format("{%s}'extractSystemPlugins\t++++++++++++++ urls.hasMoreElements(){%s} ok!",getClass().getName(),urls.hasMoreElements()));

            if(!urls.hasMoreElements()){
                return;
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                System.out.println(String.format("{%s}'extractSystemPlugins\t++++++++++++++ url{%s} ok!",getClass().getName(),url));

                String[] jarSegments = url.toString().split("!/");
                int jarLevels = 0;
                for(int i=0,j=jarSegments.length;i<j;i++){
                    if (jarSegments[i].endsWith(".jar")) {
                        jarLevels++;
                    }
                }
                if (jarLevels==2){
                    URL urlJar = new URL(jarSegments[0]+"!/");
                    JarFile jarFile = ((JarURLConnection)urlJar.openConnection()).getJarFile();
                    JarEntry jarEntry= jarFile.getJarEntry(jarSegments[1]);
                    String filename = jarEntry.getName();
                    int separatorIndex = filename.lastIndexOf("/");
                    if (separatorIndex>-1){
                        filename = filename.substring(separatorIndex+1);
                    }

                    File file = new File(getPluginsRoot().toFile(), filename);
                    if (file.exists()){
                        file.delete();
                    }

                    // create intermediary directories - sometimes zip don't add them
                    File dir = new File(file.getParent());
                    dir.mkdirs();
                    byte[] bytes = new byte[1024];
                    long lastModifiedTime = jarEntry.getLastModifiedTime().toMillis();
//                    file.setLastModified(lastModifiedTime);
                    try(BufferedInputStream in = new BufferedInputStream(jarFile.getInputStream(jarEntry));
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));) {
                        int len = in.read(bytes, 0, bytes.length);
                        while (len != -1) {
                            out.write(bytes, 0, len);
                            len = in.read(bytes, 0, bytes.length);
                        }
                        in.close();
                        out.flush();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    file.setLastModified(lastModifiedTime);
                    System.out.println(String.format("{%s}'extractSystemPlugins\t++++++++++++++ jarfile{%s} ok!",getClass().getName(),jarSegments[1]));
//                    super.loadPlugin(file.toPath());
//                    this.
                }
//                System.out.println(String.format("{%s}\t+++++++springbootpluginmanager222222+++++++ resource=["+url + "] ",getClass().getName()));
            }
//            result.put(null, bucket);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            log.error("error",e);
        };
    }
    public void work(){
        this.loadPlugins();
        this.startPlugins();
    }
}
