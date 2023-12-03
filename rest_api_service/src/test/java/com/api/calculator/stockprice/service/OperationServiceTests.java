package com.api.calculator.stockprice.service;

import com.api.calculator.stockprice.ws.data.model.Operation;
import com.api.calculator.stockprice.ws.data.repository.OperationRepository;
import com.api.calculator.stockprice.ws.data.service.OperationService;
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
    private OperationRepository operationRepository;

    @Autowired
    private OperationService operationService;

    private List<Operation> operations;

    private UUID userId;

    private UUID fileId;

    @Before
    public void setup(){
        operations = new ArrayList<>();
        userId = UUID.randomUUID();

        for(int i =0; i < 10; i++){
            Operation operation = new Operation(null, null, "OPTEST1", "type", 100, 100.0f,
                    new Date(new java.util.Date().getTime()),"typeOp",
                   "type Market", null, "CLOSED", "2022-01", "0000");

            operations.add(operationService.save(userId, operation));

        }
    }

    @After
    public void tearDown(){
        for(Operation operation: operations){
            try {
                operationService.deleteById(userId, operation.getId());
            }catch (Exception e){}
        }
    }

    @Test
    public void sumTest(){

        assert operationService.countByUserId(userId) != 0;
        assert operationService.sumValuesPerActive(userId) != null;
        assert !operationService.sumValuesPerActive(userId).isEmpty();
        assert operationService.sumValuesPerMonth(userId) != null;
        assert !operationService.sumValuesPerMonth(userId).isEmpty();
        assert operationService.sumValuesPerMonthWhereTypeMarketAndTypeOp(userId, operations.get(0).getTypeMarket(),
                operations.get(0).getTypeOp()) != null;
        assert !operationService.sumValuesPerMonthWhereTypeMarketAndTypeOp(userId, operations.get(0).getTypeMarket(),
                operations.get(0).getTypeOp()).isEmpty();
    }

    @Test
    public void findsTest(){
        assert !operationService.findAllByUserId(userId).isEmpty();
        assert !operationService.findAllByUserId(userId, 1, 1).isEmpty();
    }

    @Test
    public void deletesTest(){
        operationService.deleteById(userId, operations.get(0).getId());
        operationService.deleteByFileId(userId, fileId);

        assert !operationRepository.findById(operations.get(0).getId()).isPresent();
        assert operationRepository.findById(operations.get(1).getId()).isPresent();
    }

}
