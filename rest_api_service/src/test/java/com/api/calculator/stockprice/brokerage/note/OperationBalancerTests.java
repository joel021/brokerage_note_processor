package com.api.calculator.stockprice.brokerage.note;

import com.api.calculator.stockprice.brokerage.note.operation.Constants;
import com.api.calculator.stockprice.brokerage.note.operation.OperationBalancer;
import com.api.calculator.stockprice.ws.data.model.Operation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OperationBalancerTests {

    @Test
    public void testSortOperation(){
        List<Operation> operations = new ArrayList<>();

        for(int i = 10; i > 0; i--){
            Operation operation = new Operation(1L, null, "ACTIVE", "ACTIVE", 100, 1000f,
                    new Date(new GregorianCalendar(2023, 1, i).getTimeInMillis()), "",
                    "active", null, Constants.SOLD, null, null);
            operations.add(operation);
        }
        Collections.sort(operations);

        for (int i = 1; i < operations.size(); i++){
            System.out.println(operations.get(i).getDate());
            assert operations.get(i).getDate().toLocalDate().getDayOfMonth() > operations.get(i-1).getDate().toLocalDate().getDayOfMonth();
        }
    }

    @Test
    public void testBalancer(){
        List<Operation> openedOperations = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(2023, 2, 1);

        openedOperations.add(new Operation(null, null, "ACTIVE1", Constants.ACTIVE, 200, 400f,
                new Date(cal.getTime().getTime()), Constants.SWINGTRADE,
                Constants.CASH_MARKET, UUID.randomUUID(), Constants.SOLD, null, null));

        openedOperations.add(new Operation(null, null, "ACTIVE2", Constants.ACTIVE, 200, 400f,
                new Date(cal.getTime().getTime()), Constants.SWINGTRADE,
                Constants.CASH_MARKET, UUID.randomUUID(), Constants.SOLD, null, null));

        List<Operation> newOperations = new ArrayList<>();
        newOperations.add(new Operation(null, null, "ACTIVE1", Constants.ACTIVE, 200, 400f,
                new Date(cal.getTime().getTime()), Constants.SWINGTRADE,
                Constants.CASH_MARKET, UUID.randomUUID(), Constants.SOLD, null, null));

        newOperations.add(new Operation(null, null, "ACTIVE2", Constants.ACTIVE, 100, -100f,
                new Date(cal.getTime().getTime()), Constants.SWINGTRADE,
                Constants.CASH_MARKET, UUID.randomUUID(), Constants.BOUGHT, null, null));

        openedOperations.addAll(newOperations);
        OperationBalancer operationBalancer = new OperationBalancer(openedOperations);

        float sumValue = 0;
        for(Operation operation: operationBalancer.getClosedOperations()){
            sumValue += operation.getValue();
        }
        assert sumValue == 100f;

        assert operationBalancer.getClosedOperations().size() == 2;
        assert operationBalancer.getOpenedOperations().size() == 3;
    }
}
