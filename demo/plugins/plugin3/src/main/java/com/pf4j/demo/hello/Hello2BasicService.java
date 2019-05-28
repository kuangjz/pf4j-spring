package com.pf4j.demo.hello;

import org.springframework.stereotype.Service;

@Service
public class Hello2BasicService {
    public String doService(String name){
        return "2BasiceService for "+name;
    }
}

