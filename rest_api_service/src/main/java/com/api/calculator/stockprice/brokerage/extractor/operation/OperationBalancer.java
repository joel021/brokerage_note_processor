package com.api.calculator.stockprice.brokerage.extractor.operation;

import com.api.calculator.stockprice.DateUtils;
import com.api.calculator.stockprice.api.persistence.model.Operation;

import java.util.*;

public class OperationBalancer {

    private final List<Operation> openedOperations;
    private final List<Operation> closedOperations;

    private final Map<String, List<Operation>> boughts;
    private final Map<String, List<Operation>> solds;

    public OperationBalancer(List<Operation> operations){
        this.openedOperations = new ArrayList<>();
        this.closedOperations = new ArrayList<>();
        this.boughts = new HashMap<>();
        this.solds = new HashMap<>();
        this.balanceOperations(operations);
    }

    public List<Operation> getOpenedOperations(){
        return openedOperations;
    }

    public List<Operation> getClosedOperations(){
        return closedOperations;
    }

    private void balanceOperations(List<Operation> operations) {
        Collections.sort(operations);
        for(Operation operation: operations){

            List<Operation> activeBoughtStack = getBoughtsStack(operation.getName());
            List<Operation> activeSoldStack = getSoldsStack(operation.getName());

            if (Constants.DAYTRADE.equals(operation.getTypeOp())){//already closed
                operation.setWallet(Constants.CLOSED);
                operation.setCloseMonth(operation.getDate().toString().substring(0,7));
                closedOperations.add(operation);
            }else if(operation.getValue() < 0){ //buy
                if (activeSoldStack.isEmpty()){
                    activeBoughtStack.add(operation);
                }else{
                    balance(activeSoldStack, activeBoughtStack, operation);
                }
            }else {//sell
                if (activeBoughtStack.isEmpty()){
                    activeSoldStack.add(operation);
                }else{
                    balance(activeBoughtStack, activeSoldStack, operation);
                }
            }
            boughts.put(operation.getName(), activeBoughtStack);
            solds.put(operation.getName(), activeSoldStack);
        }
        dumpOpenedOperations(boughts, Constants.BOUGHT);
        dumpOpenedOperations(solds, Constants.SOLD);
    }

    private List<Operation> getBoughtsStack(String activeName){
        List<Operation> activeBoughtStack = boughts.get(activeName);
        return activeBoughtStack != null ? activeBoughtStack : new ArrayList<>();
    }

    private List<Operation> getSoldsStack(String activeName){
        List<Operation> activeSoldStack = solds.get(activeName);
        return activeSoldStack != null ? activeSoldStack : new ArrayList<>();
    }

    private void dumpOpenedOperations(Map<String, List<Operation>> activeStacks, String wallet){
        java.sql.Date now = new java.sql.Date(System.currentTimeMillis());

        for (List<Operation> activeStack: activeStacks.values()){

            for(Operation operation: activeStack){
                operation.setTypeOp(Constants.SWINGTRADE);

                if (Constants.ACTIVE.equals(operation.getActiveType())){
                    operation.setWallet(wallet);
                    openedOperations.add(operation);
                } else {

                    String[] closeMonthArr = operation.getCloseMonth().split("-");
                    java.sql.Date expirationDate = DateUtils.thirdDayOfWeek(Integer.parseInt(closeMonthArr[0]),
                            Integer.parseInt("20"+closeMonthArr[1]), TimeZone.getDefault());
                    if (now.after(expirationDate)){
                        operation.setWallet(Constants.CLOSED);
                        closedOperations.add(operation);
                    }else{
                        openedOperations.add(operation);
                    }

                }
            }
        }
    }

    private void balance(List<Operation> reverseStack, List<Operation> currentOpStack, Operation operation){

        while(!reverseStack.isEmpty()){
            Operation stackOperation = reverseStack.remove(0);

            if (operation.getQtd() == stackOperation.getQtd()){
                close(operation, stackOperation);
                break;
            }else if(operation.getQtd() < stackOperation.getQtd()){
                abateReverse(operation, stackOperation, reverseStack);
                break;
            }else{
                abateCurrent(operation, stackOperation, currentOpStack);
            }
        }
    }

    private void abateCurrent(Operation operation, Operation reverseOperation, List<Operation> currentStack){
        Operation remainOperation = operation.clone();
        remainOperation.setQtd(remainOperation.getQtd() - reverseOperation.getQtd());
        remainOperation.setValue(operation.getValue() * remainOperation.getQtd() / operation.getQtd());
        currentStack.add(remainOperation);

        operation.setQtd(reverseOperation.getQtd());
        operation.setValue(operation.getValue() - remainOperation.getValue());
        operation.setTypeOp(operation.getDate().equals(reverseOperation.getDate()) ? Constants.DAYTRADE : Constants.SWINGTRADE);
        operation.setCloseMonth(operation.getDate().toString().substring(0,7));
        reverseOperation.setCloseMonth(operation.getCloseMonth());
        closedOperations.add(operation);
        closedOperations.add(reverseOperation);
    }
    private void abateReverse(Operation operation, Operation reverseOperation, List<Operation> reverseStack){
        Operation remainReverseOperation = reverseOperation.clone();
        remainReverseOperation.setQtd(remainReverseOperation.getQtd() - operation.getQtd());
        remainReverseOperation.setValue(reverseOperation.getValue() - operation.getValue());
        reverseStack.add(remainReverseOperation);

        reverseOperation.setValue(reverseOperation.getValue() * operation.getQtd() / reverseOperation.getQtd());
        reverseOperation.setQtd(operation.getQtd());
        reverseOperation.setCloseMonth(operation.getDate().toString().substring(0, 7));
        reverseOperation.setTypeOp(reverseOperation.getDate().equals(operation.getDate()) ? Constants.DAYTRADE : Constants.SWINGTRADE);

        operation.setTypeOp(reverseOperation.getTypeOp());
        operation.setCloseMonth(reverseOperation.getCloseMonth());

        closedOperations.add(operation);
        closedOperations.add(reverseOperation);
    }
    private void close(Operation operation, Operation stackOperation){
        operation.setTypeOp(operation.getDate().equals(stackOperation.getDate()) ? Constants.DAYTRADE : Constants.SWINGTRADE);
        stackOperation.setTypeOp(operation.getTypeOp());

        closedOperations.add(operation);
        stackOperation.setCloseMonth(operation.getDate().toString().substring(0, 7));
        closedOperations.add(stackOperation);
    }

}
