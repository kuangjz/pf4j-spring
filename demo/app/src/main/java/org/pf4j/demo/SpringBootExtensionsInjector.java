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
//                    System.out.println(String.format("{%s}\tRegister extension '{%s}'as bean start....",toString2(), extensionClassName));
                    Class<?> extensionClass = ((SpringPluginManager)pluginManager).getApplicationContext().getClassLoader().loadClass(extensionClassName);
                    System.out.println(String.format("{%s}\t'{%s}'isMembeClass{%s}",toString2(), extensionClassName,extensionClass.isMemberClass()));
                    /**
                     * extension是classloader根据classpath中的extension.idx文件内容来进行加载，classpath中包含class文件和jar文件。
                     * 根据我们的要求：纯粹的extension（由classpath中获得的）不能是内部类，而plugin中的extension必须是内部类，
                     * 因此extensionClass如果不是内部类，则进行加载，否则该extension应该是在plugin中定义的，由plugin进行加载注入到spring中
                     * */
                    if(!extensionClass.isMemberClass()) {
                        registerExtension0(extensionClass);
                        System.out.println(String.format("{%s}\tRegister extension '{%s}'as bean success....",toString2(), extensionClassName));
                    }
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
        }

        // add extensions for each started plugin
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        for (PluginWrapper plugin : startedPlugins) {
            log.debug("Registering extensions of the plugin '{}' as beans", plugin.getPluginId());
            System.out.println(String.format("{%s}\tRegistering extensions of the plugin '{%s}' as beans",toString2(), plugin.getPluginId()));
            extensionClassNames = pluginManager.getExtensionClassNames(plugin.getPluginId());
            for (String extensionClassName : extensionClassNames) {
                try {
                    log.debug("Register extension '{}' as bean", extensionClassName);
                    Class<?> extensionClass = plugin.getPluginClassLoader().loadClass(extensionClassName);
                    registerExtension(extensionClass);
                    System.out.println(String.format("{%s}\tRegistering extensions of the plugin '{%s}' as beans {%s}",toString2(), plugin.getPluginId(),extensionClass));
                    /**
                     * 如果插件类型是SpringPlugin，则必须加载该插件中的各种资源：bean/component/controller/restcontroller/service等
                     * */
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
            ApplicationContext _applicationContext = ((SpringPluginManager)this.pluginManager).getApplicationContext();
            AnnotationConfigServletWebServerApplicationContext applicationContext = (AnnotationConfigServletWebServerApplicationContext)_applicationContext;
            /**
             * 使用plugin的classloader加载插件中的类并注册到plugin的context中
             * */
            AnnotationConfigApplicationContext _appCtxPlugin = (AnnotationConfigApplicationContext)((SpringPlugin)plugin.getPlugin()).getApplicationContext();
            ClassLoader pluginClassLoader = plugin.getPluginClassLoader();
            //获取导入的jar的controller  service  dao 等类，并且创建BeanDefinition
            Set<BeanDefinition> beanDefinitions = getBeanDefinitions(plugin);

//            applicationContext.refresh();
//            String[] beanNames = applicationContext.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println("**************=>"+beanName);
//            }
            beanDefinitions.forEach(item -> {
                //根据beanDefinition通过BeanFactory注册bean，如果已经存在该bean，则先行删除后注册
//                System.out.println(String.format("{%s}\t>>>>>>>>>>>>>item bean "+item.getBeanClassName()+ " isInterface="+item.getClass().isInterface(),toString2()));
                boolean isInterface =false;
                try {
                    Class cn = Class.forName(item.getBeanClassName(), false, pluginClassLoader);
                    isInterface = cn.isInterface();
                    System.out.println(String.format("{%s}'registerWebBeans\t>>>>>>>>>>>>>item bean {%s} isInterface="+cn.isInterface(),toString2(),item.getBeanClassName()));

                }catch (Exception e){}


                /**
                 * 如果class是接口，则不注册到context中。
                 * 如果bean的定义已经在context中注册了，则先删后注册
                 * */
                if(!isInterface) {
                    if (_appCtxPlugin.getDefaultListableBeanFactory().containsBeanDefinition(item.getBeanClassName())) {
                        System.out.println(String.format("{%s}'registerWebBeans\t>>>>>>>>>>>>>remove bean {%s}",toString2(),item.getBeanClassName()));
                        _appCtxPlugin.getDefaultListableBeanFactory().removeBeanDefinition(item.getBeanClassName());
                    }
                    System.out.println(String.format("{%s}'registerWebBeans\t>>>>>>>>>>>>>registerBeanDefinition {%s}",toString2(),item.getBeanClassName()));
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Set<BeanDefinition> getBeanDefinitions(PluginWrapper plugin) throws Exception {
        Set<BeanDefinition> candidates = new LinkedHashSet<>();
        ClassLoader cl = plugin.getPluginClassLoader();
        String pluginBasePath = plugin.getPluginPath().toUri().toURL().toExternalForm();
        System.out.println(String.format("{%s}'getBeanDefinitions\tplugin["+plugin+"] basepath=["+pluginBasePath+"]",toString2()));
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(cl){
            @Override
            public Resource[] getResources(String locationPattern) throws IOException {
                Resource[] resources = super.getResources(locationPattern);
                return Arrays.stream(resources).filter(e->{
                    boolean flag =false;
                    try {
                        flag = e.getURL().toExternalForm().indexOf(pluginBasePath) > -1;
//                        System.out.println(String.format("{%s}\t++++++++++ resource=[" + e + "] basepath =[" + pluginBasePath + "] isFromPlugin[" + flag + "]", toString2()));
                    }catch (Exception e1) {
                        e1.printStackTrace();
                    }
                return flag;
                }).toArray(Resource[]::new);
            }
        };
        Resource[] resources = resourcePatternResolver.getResources("classpath*:/**/*.class");

        MetadataReaderFactory metadata=new SimpleMetadataReaderFactory();
        System.out.println(String.format("{%s}'getBeanDefinitions\t=================resources found["+resources.length+"]",toString2()));
        for(Resource resource:resources) {
            System.out.println(String.format("{%s}'getBeanDefinitions\t %s",toString2(),resource.toString()));
//            System.out.println(String.format("{%s}\t=================resource["+resource.getFilename()+"]",toString2()));
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

//            System.out.println(String.format("{%s}\t===============c2="+c2,toString2()));
            Service s=Class.forName(classname,false,cl).getAnnotation(Service.class);
            Component component=Class.forName(classname,false,cl).getAnnotation(Component.class);
//            if(c!=null ||s!=null ||component!=null)
//                System.out.println(String.format("{%s}\t %s",toString2(),classname));
        }
//        classLoader = old;

        return candidates;
    }

//    @Override

    /**
     * 注册extension到beanFactory，注意是使用单例模式。
     * PluginManager在扫描classpath时，系统中的extension类会被发现，系统插件的PluginManager和扩展插件的PluginManager都会发现，因此
     * 会出现重复注册的问题。此时，如果该extension已经注册过，则忽略
     * @param extensionClass
     */
    private void registerExtension0(Class<?> extensionClass) {
        System.out.println(String.format("{%s}'registerExtension\t %s",toString2(),extensionClass.getName()));
        if(beanFactory.getSingleton(extensionClass.getName())==null){
            System.out.println(String.format("{%s}'registerExtension\t getSingleton{%s} has registered",toString2(),extensionClass.getName()));
        try {
            super.registerExtension(extensionClass);
        }catch (Exception e){
            e.printStackTrace();
        }
        }
    }
    
    private String toString2(){
        return String.format("%s : %s",this.pluginManager.toString(), this.toString());        
    }
}
