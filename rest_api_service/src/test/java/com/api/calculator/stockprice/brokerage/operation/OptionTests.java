package com.api.calculator.stockprice.brokerage.operation;

import com.api.calculator.stockprice.brokerage.extractor.operation.Options;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionTests {

    @Test
    public void getActiveNameTest() throws ResourceNotFoundException {

        Options options = new Options();

        String name = options.getActiveName("VALE3K859");
        assertEquals("VALE3", name);
    }

    @Test
    public void getActiveNameOfExceptions() throws ResourceNotFoundException {

        Options options = new Options();

        String name = options.getActiveName("AMERC859");
        assertEquals("AMER3", name);
    }

    @Test
    public void getActiveNameWhenNull() {

        Options options = new Options();

        assertThrows(ResourceNotFoundException.class, () -> {
            options.getActiveName(null);
        });
    }

    @Test
    public void getActiveNameWhenEmpty() {

        Options options = new Options();

        assertThrows(ResourceNotFoundException.class, () -> {
            options.getActiveName("");
        });
    }

    @Test
    public void getActiveNameNotExits() {

        Options options = new Options();

        assertThrows(ResourceNotFoundException.class, () -> {
            options.getActiveName("u39ru9hfValekkkkkk");
        });
    }
}
