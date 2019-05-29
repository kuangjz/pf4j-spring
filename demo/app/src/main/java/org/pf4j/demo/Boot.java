/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.demo;

import org.apache.commons.lang.StringUtils;
import org.pf4j.spring.PluginManagerWithContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
@SpringBootApplication
@ComponentScan
public class Boot {

    public static void main(String[] args) {
        // print logo
        printLogo();

        ApplicationContext applicationContext =SpringApplication.run(Boot.class, args);

        // retrieves the spring application context
//        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        // retrieves automatically the extensions for the Greeting.class extension point
//        Greetings greetings1 = applicationContext.getBean(Greetings.class);
//        greetings1.printGreetings();
//
//        Greetings2 greetings2 = applicationContext.getBean(Greetings2.class);
//        greetings2.printGreetings();

        // stop plugins
//        PluginManager pluginManager = applicationContext.getBean(PluginManager.class);
        /*
        // retrieves manually the extensions for the Greeting.class extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        System.out.println("greetings.size() = " + greetings.size());
        */
//        pluginManager.stopPlugins();
    }

    private static void printLogo() {
        System.out.println(StringUtils.repeat("#", 40));
        System.out.println(StringUtils.center("PF4J-SPRING", 40));
        System.out.println(StringUtils.repeat("#", 40));
    }
    @Bean
    public PluginManagerWithContext pluginManager() {
        SpringBootPluginManager pm = new SpringBootPluginManager();
//        pm.init();
        return pm;
    }

    @Bean
    @DependsOn("pluginManager")
    public Greetings greetings() {
        return new Greetings();
    }

    @Bean
    @DependsOn ("pluginManager")
    public Greetings2 greetings2() {
        return new Greetings2();
    }

}
