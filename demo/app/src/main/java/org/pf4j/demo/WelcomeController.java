package org.pf4j.demo;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.PluginManagerWithContext;
import org1.pf4j.demo.api.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class WelcomeController {
//    @Autowired
//    private AnnotationConfigServletWebServerApplicationContext annotationConfigServletWebServerApplicationContext;

//    @Autowired
//    private LoadService loadService;
    @Autowired
    private PluginManagerWithContext pluginManager;

    @Autowired
    private List<Greeting> greetings;
    
    @RequestMapping("/")
    public String index() {
//        System.out.println("Let's inspect the beans provided by Spring Boot:===========00000000000000===================="+annotationConfigServletWebServerApplicationContext.getParent());
//        System.out.println("=====11111111==="+annotationConfigServletWebServerApplicationContext.getParentBeanFactory());
//        System.out.println("=====2222222222==="+annotationConfigServletWebServerApplicationContext.getDefaultListableBeanFactory().getParentBeanFactory());
//
        StringBuilder sb = new StringBuilder(256);
        String[] beanNames = pluginManager.getApplicationContext().getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            sb.append("\r\n<br/>>>==>> "+beanName);
        }

        sb.append("\r\n<br>****==>"+((AbstractAutowireCapableBeanFactory)pluginManager.getApplicationContext().getAutowireCapableBeanFactory()).getBean("org1.pf4j.demo.welcome.WelcomePlugin$WelcomeGreeting"));
        sb.append("\r\n<br>****==>"+pluginManager.getApplicationContext().getBean("org1.pf4j.demo.welcome.WelcomePlugin$WelcomeGreeting"));

        List<PluginWrapper> systemPluginWrappers = pluginManager.getSystemPluginManager().getStartedPlugins();
        for(PluginWrapper pw: systemPluginWrappers){
            System.out.println(">> "+pw);
            sb.append(String.format("\r\n<br/> SystemPluginWrappers {%s}",pw));
        }

        List<PluginWrapper> extendedPluginWrappers = pluginManager.getExtendedPluginManager().getStartedPlugins();
        for(PluginWrapper pw: extendedPluginWrappers){
            System.out.println(">> "+pw);
            sb.append(String.format("\r\n<br/> pluginWrappers {%s}",pw));
        }


        Greetings greetings1 = pluginManager.getApplicationContext().getBean(Greetings.class);
        greetings1.printGreetings();
//
//
        sb.append(String.format("\r\n<br/>Found %d extensions for extension point '%s'", greetings.size(), Greeting.class.getName()));

        for (Greeting greeting : greetings) {
            sb.append("\r\n<br/>>>> " + greeting.getGreeting() + " ["+greeting);
        }
        System.out.println(sb.toString());


        return sb.toString();
    }

    @RequestMapping("/load0")
    public String load(){
//        loadService.register(new File("D:\\mypoc\\production\\gs-spring-boot\\hello2\\out\\artifacts\\hello2_jar\\hello2.jar"));
        return "load service hello2( url =/2) success!";
    }
    
}
