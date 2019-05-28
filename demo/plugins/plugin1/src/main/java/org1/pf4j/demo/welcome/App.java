package org1.pf4j.demo.welcome;

import org.pf4j.util.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class App {
    public static void main(String[] args) throws Exception {
        String url = "jar:file:/D:/mypoc/production/pf4j-spring/demo-dist/pf4j-spring-demo.jar!/BOOT-INF/lib/pf4j-spring-demo-plugin3-0.6.0-SNAPSHOT.jar!/";
        String[] jarSegments = url.split("!/");

        Path systemPluginsDirectory = (new File("d:/test")).toPath();
//        if (Files.exists(systemPluginsDirectory)) {
//            delete(systemPluginsDirectory);
//        }
//        Files.createDirectories(systemPluginsDirectory);

//        Path tempDirectory = Files.createTempDirectory(null);
        Set<Path> unzipJars = new HashSet<Path>();

        int jarLevels = 0;
        for (int i = 0, j = jarSegments.length; i < j; i++) {
            if (jarSegments[i].endsWith(".jar")) {
                jarLevels++;
            }
            System.out.println(jarSegments[i]);
        }
        JarFile jarFile = null;
        if (jarLevels == 2) {
            URL urlJar = new URL(jarSegments[0]+"!/");
//            urlJar.openConnection();
            jarFile = ((JarURLConnection)urlJar.openConnection()).getJarFile();

//            jarFile = new JarFile(jarSegments[0]);
//            File target = new File(systemPluginsDirectory.toFile(),jarSegments[1]);
//            target.mkdirs();


            JarEntry jarEntry= jarFile.getJarEntry(jarSegments[1]);

            File file = new File(systemPluginsDirectory.toFile(), jarEntry.getName());
            if (file.exists()){
                file.delete();
            }

            // create intermediary directories - sometimes zip don't add them
            File dir = new File(file.getParent());
            dir.mkdirs();

//            try(JarInputStream zipInputStream =  new JarInputStream(jarFile.getInputStream(jarEntry))) {

                byte[] buffer = new byte[1024];
                int length;
//                try (FileOutputStream fos = new FileOutputStream(file)) {
//                    System.out.println("length="+jarEntry.getSize());
//                    System.out.println(" "+jarEntry..getValue("Plugin-Id"));
//                    System.out.println("="+jarEntry.getLastModifiedTime());
//                    while ((length = zipInputStream.read(buffer,0,buffer.length)) >= 0) {
//                        fos.write(buffer, 0, length);
//                        System.out.println("length="+length);
//                    }
//                    System.out.println("length="+length);
//                }

                byte[] bytes = new byte[1024];
                long lastModifiedTime = jarEntry.getLastModifiedTime().toMillis();
                file.setLastModified(lastModifiedTime);
                try(BufferedInputStream in = new BufferedInputStream(jarFile.getInputStream(jarEntry));
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));) {

                    int len = in.read(bytes, 0, bytes.length);
                    while (len != -1) {
                        out.write(bytes, 0, len);
                        len = in.read(bytes, 0, bytes.length);
                    }

                    in.close();
                    out.flush();
//                    file.setLastModified(jarEntry.getLastModifiedTime().toMillis());
//                    out.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

            file.setLastModified(lastModifiedTime);
                System.out.println("unzip ok!");
//            }catch (Exception e){
//                e.printStackTrace();
//            }



//            URL urlJar = new URL(jarSegments[0]);
//            urlJar.openConnection()
//            Map<String,String> zip_properties = new HashMap<>();
//            zip_properties.put("create","false");
//            FileSystem jarfs = FileSystems.newFileSystem((new URL(url)).toURI(),zip_properties);
//            System.out.println("==="+jarfs);
//            Path njar = jarfs.getPath("/"+jarSegments[1]);
//
//            System.out.println("==="+njar);
//            Path target = systemPluginsDirectory.resolve(njar.getFileName());
//            Files.copy(njar,target);
//            System.out.println(String.format(" unjar {%s} to {%s}",url,target));
//            delete();
//                    extractJar(((JarURLConnection)urlJar.openConnection()).getJarFile(),tempDirectory.toFile());
//
//                    String pluginId = ((JarURLConnection)urlConnection).getManifest().getMainAttributes().getValue("Plugin-Id");
//                    if (StringUtils.isNotNullOrEmpty(pluginId)){
//                        result.put(pluginId,urlConnection.getURL());
//                    }

        }
    }
    public static void delete(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isSymbolicLink()) {
                    Files.delete(path);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);

                return FileVisitResult.CONTINUE;
            }

        });
    }
    public static void extractJar(File source, File destination ) throws IOException {
//        log.debug("Extract content of '{}' to '{}'", source, destination);

        // delete destination file if exists
        if (destination.exists() && destination.isDirectory()) {
            FileUtils.delete(destination.toPath());
        }

        try (JarInputStream zipInputStream =  new JarInputStream(new FileInputStream(source))) {
            JarEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextJarEntry()) != null) {
                try {
                    File file = new File(destination, zipEntry.getName());

                    // create intermediary directories - sometimes zip don't add them
                    File dir = new File(file.getParent());
                    dir.mkdirs();

                    if (zipEntry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        byte[] buffer = new byte[1024];
                        int length;
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            while ((length = zipInputStream.read(buffer)) >= 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
//                    log.error("File '{}' not found", zipEntry.getName());
                }
            }
        }
    }
}

