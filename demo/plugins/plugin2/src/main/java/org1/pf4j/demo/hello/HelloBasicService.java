package org1.pf4j.demo.hello;

import org.springframework.stereotype.Service;

@Service
public class HelloBasicService {
    public String doService(String name){
        return "BasiceService for "+name;
    }
}

