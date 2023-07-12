package com.api.calculator.stockprice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RandomTests {

    @Test
    public void mainTest(){

        Map<String, Object> mapTest = new HashMap<>();
        mapTest.put("sim", Collections.singletonList("Sim"));

        if (mapTest.get("nao") == null || ((List) mapTest.get("nao")).isEmpty()){
            System.out.println("\n\nEntry withou error!");

            assert 1==1;
        }
    }
}
