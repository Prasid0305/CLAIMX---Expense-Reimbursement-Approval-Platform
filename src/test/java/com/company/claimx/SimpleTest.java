package com.company.claimx;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SimpleTest {

    @Test
    public void loadContext(){
        System.out.println("connection successful!!!@@####");
    }
}
