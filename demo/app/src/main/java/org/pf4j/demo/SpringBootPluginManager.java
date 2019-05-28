package org.pf4j.demo;

import org.pf4j.ExtensionFactory;
import org.pf4j.spring.SingletonSpringExtensionFactory;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SpringBootPluginManager extends SpringPluginManager {
    private static final Logger log = LoggerFactory.getLogger(SpringBootPluginManager.class);
    public SpringBootPluginManager() {
    }

    public SpringBootPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }
    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new SingletonSpringExtensionFactory(this);
    }

    @PostConstruct
    public void init() {
        System.out.println(String.format("{%s}\t=========init Pluginmanager=========="+this.getApplicationContext(),getClass().getName()));

//        loadSystemPlugins();

        loadPlugins();
        startPlugins();

        AbstractAutowireCapableBeanFactory beanFactory = (AbstractAutowireCapableBeanFactory) this.getApplicationContext().getAutowireCapableBeanFactory();
        SpringBootExtensionsInjector extensionsInjector = new SpringBootExtensionsInjector(this, beanFactory);
        extensionsInjector.injectExtensions();
    }

    protected void loadSystemPlugins0(){
        ClassLoader cl = getClass().getClassLoader();

    }

    private Map<String, URL> loadSystemPlugins() {
        Map<String, URL> result = new LinkedHashMap<>();
        try {
            Enumeration<URL> urls = getClass().getClassLoader().getResources("META-INF/extensions.idx");
            if(!urls.hasMoreElements()){
                return result;
            }
            Path systemPluginsDirectory = (new File(".system_plugins")).toPath();
            Map<URL,Long> cachedUnJarFiles = new HashMap<>();

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String[] jarSegments = url.toString().split("!/");
                int jarLevels = 0;
                for(int i=0,j=jarSegments.length;i<j;i++){
                    if (jarSegments[i].endsWith(".jar")) {
                        jarLevels++;
                    }
                }
                if (jarLevels==2){
                    URL urlJar = new URL(jarSegments[0]+"!/");
                    JarFile  jarFile = ((JarURLConnection)urlJar.openConnection()).getJarFile();
                    JarEntry jarEntry= jarFile.getJarEntry(jarSegments[1]);
                    String filename = jarEntry.getName();
                    int separatorIndex = filename.lastIndexOf("/");
                    if (separatorIndex>-1){
                        filename = filename.substring(separatorIndex+1);
                    }

                    File file = new File(systemPluginsDirectory.toFile(), filename);
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
                    System.out.println(String.format("{%s}'loadSystemPlugins\t++++++++++++++ {%s} ok!",getClass().getName(),url));
                    super.loadPlugin(file.toPath());
//                    this.
                }
//                System.out.println(String.format("{%s}\t+++++++springbootpluginmanager222222+++++++ resource=["+url + "] ",getClass().getName()));
            }
//            result.put(null, bucket);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            log.error("error",e);
        }

        return result;
    }
}
