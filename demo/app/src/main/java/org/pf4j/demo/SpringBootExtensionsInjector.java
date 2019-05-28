package org.pf4j.demo;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.ExtensionsInjector;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class SpringBootExtensionsInjector extends ExtensionsInjector {
    private static final Logger log = LoggerFactory.getLogger(SpringBootExtensionsInjector.class);
    public SpringBootExtensionsInjector(PluginManager pluginManager, AbstractAutowireCapableBeanFactory beanFactory) {
        super(pluginManager,beanFactory);
    }
    @Override
    public void injectExtensions() {
        // add extensions from classpath (non plugin)
//        System.out.println(">>>>>>>>>>>="+getClass().getClassLoader() +"==="+ ((SpringPluginManager)pluginManager).getApplicationContext().getClassLoader());
        Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
        for (String extensionClassName : extensionClassNames) {
                try {
                    log.debug("Register extension '{}' as bean", extensionClassName);
                    System.out.println(String.format("{%s}\tRegister extension '{%s}'as bean",getClass().getName(), extensionClassName));
                    Class<?> extensionClass = ((SpringPluginManager)pluginManager).getApplicationContext().getClassLoader().loadClass(extensionClassName);
                    System.out.println(String.format("{%s}\t'{%s}'isMembeClass{%s}",getClass().getName(), extensionClassName,extensionClass.isMemberClass()));
                    if(!extensionClass.isMemberClass()) {
                        registerExtension(extensionClass);
                    }
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
        }

        // add extensions for each started plugin
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        for (PluginWrapper plugin : startedPlugins) {
            log.debug("Registering extensions of the plugin '{}' as beans", plugin.getPluginId());
            System.out.println(String.format("{%s}\tRegistering extensions of the plugin '{%s}' as beans",getClass().getName(), plugin.getPluginId()));
            extensionClassNames = pluginManager.getExtensionClassNames(plugin.getPluginId());
            for (String extensionClassName : extensionClassNames) {
                try {
                    log.debug("Register extension '{}' as bean", extensionClassName);
                    Class<?> extensionClass = plugin.getPluginClassLoader().loadClass(extensionClassName);
                    registerExtension(extensionClass);
                    System.out.println(String.format("{%s}\tRegistering extensions of the plugin '{%s}' as beans {%s}",getClass().getName(), plugin.getPluginId(),extensionClass));

                    if (plugin.getPlugin() instanceof SpringPlugin) {
                        //register all web-related beans
                        registerWebBeans(plugin);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
    }
    private void registerWebBeans(PluginWrapper plugin) {
        try {
            ApplicationContext _applicationContext = ((SpringBootPluginManager)this.pluginManager).getApplicationContext();
            AnnotationConfigServletWebServerApplicationContext applicationContext = (AnnotationConfigServletWebServerApplicationContext)_applicationContext;
            AnnotationConfigApplicationContext _appCtxPlugin = (AnnotationConfigApplicationContext)((SpringPlugin)plugin.getPlugin()).getApplicationContext();
            ClassLoader pluginClassLoader = plugin.getPluginClassLoader();
            //新建classloader 核心
            //获取导入的jar的controller  service  dao 等类，并且创建BeanDefinition
//                AnnotationConfigApplicationContext applicationContext = (AnnotationConfigApplicationContext) this.getApplicationContext();
            Set<BeanDefinition> beanDefinitions = getBeanDefinitions(applicationContext,plugin);

//            applicationContext.refresh();
//            String[] beanNames = applicationContext.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println("**************=>"+beanName);
//            }

            //修改BeanFactory的ClassLoader
//            if(plugin.getPlugin() instanceof SpringPlugin){
//                ((SpringPlugin) plugin.getPlugin()).getApplicationContext()
//            }
//            ClassLoader old = applicationContext.getDefaultListableBeanFactory().getBeanClassLoader();
//            applicationContext.getDefaultListableBeanFactory().setBeanClassLoader(pluginClassLoader);

            beanDefinitions.forEach(item -> {
                //根据beanDefinition通过BeanFactory注册bean，如果已经存在该bean，则先行删除后注册
//                System.out.println(String.format("{%s}\t>>>>>>>>>>>>>item bean "+item.getBeanClassName()+ " isInterface="+item.getClass().isInterface(),getClass().getName()));
                boolean isInterface =false;
                try {
                    Class cn = Class.forName(item.getBeanClassName(), false, pluginClassLoader);
                    isInterface = cn.isInterface();
                    System.out.println(String.format("{%s}'registerWebBeans\t>>>>>>>>>>>>>item bean {%s} isInterface="+cn.isInterface(),getClass().getName(),item.getBeanClassName()));

                }catch (Exception e){}

                if(!isInterface) {
                    if (_appCtxPlugin.getDefaultListableBeanFactory().containsBeanDefinition(item.getBeanClassName())) {
                        System.out.println(String.format("{%s}'registerWebBeans\t>>>>>>>>>>>>>remove bean {%s}",getClass().getName(),item.getBeanClassName()));
                        _appCtxPlugin.getDefaultListableBeanFactory().removeBeanDefinition(item.getBeanClassName());
                    }
                    System.out.println(String.format("{%s}'registerWebBeans\t>>>>>>>>>>>>>registerBeanDefinition {%s}",getClass().getName(),item.getBeanClassName()));
                    _appCtxPlugin.getDefaultListableBeanFactory().registerBeanDefinition(item.getBeanClassName(), item);
                }
            });
            //手动刷新context不需要，会导致重复刷新
            //annotationConfigServletWebServerApplicationContext.refresh();

            //获取requestMappingHandlerMapping，用来注册HandlerMapping
            RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            beanDefinitions.forEach(item -> {

                String classname = item.getBeanClassName();
                try {
                    Class c = Class.forName(classname, false, pluginClassLoader);
                    Controller annotation = (Controller) c.getAnnotation(Controller.class);
                    RestController annotation2 = (RestController) c.getAnnotation(RestController.class);
                    //如果此bean是Controller，则注册到RequestMappingHandlerMapping里面
                    if (annotation != null || annotation2 != null) {
                        //获取该bean 真正的创建
                        Object proxy = _appCtxPlugin.getBean(item.getBeanClassName());

                        Method getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
                        getMappingForMethod.setAccessible(true);
                        try {
                            Method[] method_arr = c.getMethods();
                            for (Method m_d : method_arr) {
                                if (m_d.getAnnotation(RequestMapping.class) != null) {
                                    //创建RequestMappingInfo
                                    RequestMappingInfo mapping_info = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping, m_d, c);
                                    //注册，先删后注册
                                    requestMappingHandlerMapping.unregisterMapping(mapping_info);
                                    requestMappingHandlerMapping.registerMapping(mapping_info, proxy, m_d);
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
//            applicationContext.getDefaultListableBeanFactory().setBeanClassLoader(old);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private Set<BeanDefinition> getBeanDefinitions(ApplicationContext applicationContext, PluginWrapper plugin) throws Exception {
        Set<BeanDefinition> candidates = new LinkedHashSet<>();

//        ClassLoader old = classLoader;
//        ClassLoader classLoader = applicationContext.getClassLoader();
//        URLClassLoader cl = new URLClassLoader(new URL[]{(new File(plugin.getPluginPath()+"/classes")).toURI().toURL()},classLoader);
        ClassLoader cl = plugin.getPluginClassLoader();
        String pluginBasePath = plugin.getPluginPath().toUri().toURL().toExternalForm();
        System.out.println(String.format("{%s}'getBeanDefinitions\tplugin["+plugin+"] basepath=["+pluginBasePath+"]",getClass().getName()));
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(cl){
            @Override
            public Resource[] getResources(String locationPattern) throws IOException {
                Resource[] resources = super.getResources(locationPattern);
                return Arrays.stream(resources).filter(e->{
                    boolean flag =false;
                    try {
                        flag = e.getURL().toExternalForm().indexOf(pluginBasePath) > -1;
//                        System.out.println(String.format("{%s}\t++++++++++ resource=[" + e + "] basepath =[" + pluginBasePath + "] isFromPlugin[" + flag + "]", getClass().getName()));
                    }catch (Exception e1) {
                        e1.printStackTrace();
                    }
                return flag;
                }).toArray(Resource[]::new);
            }
//            @Override
//            protected Set<Resource> doFindAllClassPathResources(String path) throws IOException {
//                Set<Resource> result = new LinkedHashSet<>(16);
//                ClassLoader cl = getClassLoader();
//                Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(path) : ClassLoader.getSystemResources(path));
//                while (resourceUrls.hasMoreElements()) {
//                    URL url = resourceUrls.nextElement();
//                    System.out.println("++++++++++ url="+url + "] isFromPlugin="+(url.toString().indexOf(pluginBasePath)>-1));
//                    result.add(convertClassLoaderURL(url));
//                }
//                return result;
//            }
//            @Override
//            protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
//                String rootDirPath = determineRootDir(locationPattern);
//                String subPattern = locationPattern.substring(rootDirPath.length());
//                Resource[] rootDirResources = getResources(rootDirPath);
//                Set<Resource> result = new LinkedHashSet<>(16);
//                for (Resource rootDirResource : rootDirResources) {
//                    rootDirResource = resolveRootDirResource(rootDirResource);
//                    URL rootDirUrl = rootDirResource.getURL();
//System.out.println("++++++++++ rootDirResource="+rootDirResource + "] isFromPlugin="+(rootDirResource.toString().indexOf(pluginBasePath)>-1));
//                    if (ResourceUtils.isJarURL(rootDirUrl) || isJarResource(rootDirResource)) {
////                        result.addAll(doFindPathMatchingJarResources(rootDirResource, rootDirUrl, subPattern));
//                    }
//                    else {
//                        result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
//                    }
//                }
//                return result.toArray(new Resource[0]);
//            }
        };
        Resource[] resources = resourcePatternResolver.getResources("classpath*:/**/*.class");

        MetadataReaderFactory metadata=new SimpleMetadataReaderFactory();
        System.out.println(String.format("{%s}'getBeanDefinitions\t=================resources found["+resources.length+"]",getClass().getName()));
        for(Resource resource:resources) {
            System.out.println(String.format("{%s}'getBeanDefinitions\t %s",getClass().getName(),resource.toString()));
//            System.out.println(String.format("{%s}\t=================resource["+resource.getFilename()+"]",getClass().getName()));
            MetadataReader metadataReader=metadata.getMetadataReader(resource);
            ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
            sbd.setResource(resource);
            sbd.setSource(resource);
            candidates.add(sbd);
        }
        for(BeanDefinition beanDefinition : candidates) {
            String classname=beanDefinition.getBeanClassName();
            Controller c=Class.forName(classname,false,cl).getAnnotation(Controller.class);

            RestController c2= Class.forName(classname,false,cl).getAnnotation(RestController.class);

//            System.out.println(String.format("{%s}\t===============c2="+c2,getClass().getName()));
            Service s=Class.forName(classname,false,cl).getAnnotation(Service.class);
            Component component=Class.forName(classname,false,cl).getAnnotation(Component.class);
//            if(c!=null ||s!=null ||component!=null)
//                System.out.println(String.format("{%s}\t %s",getClass().getName(),classname));
        }
//        classLoader = old;

        return candidates;
    }

//    @Override
    protected void registerExtension0(Class<?> extensionClass) {
//        System.out.println(String.format("{%s}'registerExtension\t %s",getClass().getName(),extensionClass.getName()));
//        if(beanFactory.getSingleton(extensionClass.getName())==null){
//            System.out.println(String.format("{%s}'registerExtension\t getSingleton{%s} is null",getClass().getName(),extensionClass.getName()));
        try {
            super.registerExtension(extensionClass);
        }catch (Exception e){
            e.printStackTrace();
        }
//        }
    }
}
