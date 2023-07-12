package com.api.calculator.stockprice.service;

import com.api.calculator.stockprice.brokerage.note.OperationBalancer;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.model.Operation;
import com.api.calculator.stockprice.repository.OperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OperationService {

    @Autowired
    private OperationRepository operationRepository;

    public int countNonDeletedByUserId(UUID userId){
        return operationRepository.countByUserIdAndDeletedAt(userId, null);
    }

    public List<Operation> findAllByUserId(UUID userId){
        return operationRepository.findByUserId(userId);
    }

    public List<Operation> findNonDeletedByUserId(UUID userId, int page, int quantity) {

        return operationRepository.findAllByUserIdAndDeletedAt(userId, null, PageRequest.of(page, quantity, Sort.by(Sort.Direction.DESC, "closeMonth")));
    }
    public Operation save(UUID userId, Operation operation){
        operation.setUserId(userId);
        return operationRepository.save(operation);
    }

    public Operation update(UUID authUserId, Operation updateOperation) throws ResourceNotFoundException {

        Optional<Operation> optionalOperation = operationRepository.findByOperationIdAndUserId(updateOperation.getOperationId(), authUserId);

        if (optionalOperation.isPresent()){
            Operation oldOperation = optionalOperation.get();

            if (updateOperation.getDate() != null) oldOperation.setDate(updateOperation.getDate());

            if (updateOperation.getCloseMonth() != null) oldOperation.setCloseMonth(updateOperation.getCloseMonth());

            if (updateOperation.getName() != null) oldOperation.setName(updateOperation.getName());

            if (updateOperation.getActiveType() != null) oldOperation.setActiveType(updateOperation.getActiveType());

            if (updateOperation.getTypeMarket() != null) oldOperation.setTypeMarket(updateOperation.getTypeMarket());

            if (updateOperation.getTypeOp() != null) oldOperation.setTypeOp(updateOperation.getTypeOp());

            if (updateOperation.getWallet() != null) oldOperation.setWallet(updateOperation.getWallet());

            oldOperation.setQtd(updateOperation.getQtd());
            oldOperation.setValue(updateOperation.getValue());

            Operation operationSaved = operationRepository.save(oldOperation);
            rebalanceOperations(authUserId);
            return operationSaved;
        }else{
            throw new ResourceNotFoundException("A operação requisitada não existe.");
        }
    }

    private void rebalanceOperations(UUID userId){
        OperationBalancer operationBalancer = new OperationBalancer(operationRepository.findByUserId(userId));

        for(Operation operation: operationBalancer.getClosedOperations()){
            save(userId, operation);
        }

        for(Operation operation: operationBalancer.getOpenedOperations()){
            save(userId, operation);
        }
    }

    public void deleteByFileId(UUID userId, UUID fileId) {
        operationRepository.setDeletedAtByUserIdAndFileId(new Date(), userId, fileId);
    }

    public void deleteById(UUID userId, long operationId){
        operationRepository.setDeletedAtByUserIdAndId(new Date(), userId, operationId);
    }

    public List<Object> sumValuesPerMonth(UUID userId){
        return operationRepository.sumValuesPerMonthByUserId(userId);
    }

    public List<Object> sumValuesPerMonthWhereTypeMarketAndTypeOp(UUID userId, String typeMarket, String typeOp){
        return operationRepository.sumValuesPerMonthByUserIdAndTypeMarketAndTypeOp(userId, typeMarket, typeOp);
    }

    public List<Object> sumValuesPerActive(UUID userId){
        return operationRepository.sumValuesPerActive(userId);
    }
}
