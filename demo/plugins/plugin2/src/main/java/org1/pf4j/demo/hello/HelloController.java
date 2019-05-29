package org1.pf4j.demo.hello;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.PluginManagerWithContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class HelloController {
//    @Autowired
//    private AnnotationConfigServletWebServerApplicationContext annotationConfigServletWebServerApplicationContext;

//    @Autowired
//    private LoadService loadService;
    @Autowired
    private PluginManagerWithContext pluginManager;
    
    @RequestMapping("/hello")
    public String hello() {
//        System.out.println("Let's inspect the beans provided by Spring Boot:===========00000000000000===================="+annotationConfigServletWebServerApplicationContext.getParent());
//        System.out.println("=====11111111==="+annotationConfigServletWebServerApplicationContext.getParentBeanFactory());
//        System.out.println("=====2222222222==="+annotationConfigServletWebServerApplicationContext.getDefaultListableBeanFactory().getParentBeanFactory());
//
        String[] beanNames = pluginManager.getApplicationContext().getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(">> "+beanName);
        }
        List<PluginWrapper> pluginWrappers = pluginManager.getStartedPlugins();
        for(PluginWrapper pw: pluginWrappers){
            System.out.println(String.format("{%s}\t>> "+pw,getClass().getName()));
        }

        return "Greetings from Spring Boot!";
    }

    @RequestMapping("/load")
    public String load(){
//        loadService.register(new File("D:\\mypoc\\production\\gs-spring-boot\\hello2\\out\\artifacts\\hello2_jar\\hello2.jar"));
        return "load service hello2( url =/2) success!";
    }
    
}
