package com.pf4j.demo.hello;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Hello2Controller {
//    @Autowired
//    private LoadService loadService;
    @Autowired
    private SpringPluginManager pluginManager;
    
    @RequestMapping("/hello2")
    public String hello() {
//        System.out.println("Let's inspect the beans provided by Spring Boot:===========00000000000000===================="+annotationConfigServletWebServerApplicationContext.getParent());
//        System.out.println("=====11111111==="+annotationConfigServletWebServerApplicationContext.getParentBeanFactory());
//        System.out.println("=====2222222222==="+annotationConfigServletWebServerApplicationContext.getDefaultListableBeanFactory().getParentBeanFactory());
//
//        String[] beanNames = pluginManager.getApplicationContext().getBeanDefinitionNames();
//        Arrays.sort(beanNames);
//        for (String beanName : beanNames) {
//            System.out.println(">> "+beanName);
//        }
        List<PluginWrapper> pluginWrappers = pluginManager.getStartedPlugins();
        for(PluginWrapper pw: pluginWrappers){
            System.out.println(">>hello222222222 "+pw);
        }

        return "Greetings from hello2plugin!";
    }    
}
