package org.pf4j.demo;

import org1.pf4j.demo.api.Greeting;

public class ChineseGreeting implements Greeting {

    @Override
    public String getGreeting() {
        return "Chinese Greeting!!";
    }
}
