package com.api.calculator.stockprice.service;

import com.api.calculator.stockprice.model.Operation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OperationServiceTests {


    @Autowired
    private OperationService operationService;

    private List<Operation> operations;

    private UUID userId;


    @Before
    public void setup(){
        operations = new ArrayList<>();
        userId = UUID.randomUUID();

        for(int i =0; i < 10; i++){
            Operation operation = new Operation(null, null, "OPTEST1", "type", 100, 100.0f,
                    new Date(new java.util.Date().getTime()),"typeOp",
                   "type Market", null, "CLOSED", "2022-01", null);

            operations.add(operationService.save(userId, operation));

        }
    }

    @After
    public void tearDown(){
        for(Operation operation: operations){
            operationService.deleteById(userId, operation.getOperationId());
        }
    }

    @Test
    public void sumTest(){

        assert operationService.countNonDeletedByUserId(userId) != 0;
        assert operationService.sumValuesPerActive(userId) != null;
        assert !operationService.sumValuesPerActive(userId).isEmpty();
        assert operationService.sumValuesPerMonth(userId) != null;
        assert !operationService.sumValuesPerMonth(userId).isEmpty();
        assert operationService.sumValuesPerMonthWhereTypeMarketAndTypeOp(userId, operations.get(0).getTypeMarket(),
                operations.get(0).getTypeOp()) != null;
        assert !operationService.sumValuesPerMonthWhereTypeMarketAndTypeOp(userId, operations.get(0).getTypeMarket(),
                operations.get(0).getTypeOp()).isEmpty();
    }

}
