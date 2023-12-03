package com.api.calculator.stockprice.ws.data.service;

import com.api.calculator.stockprice.brokerage.note.operation.CsvGenerator;
import com.api.calculator.stockprice.brokerage.note.operation.OperationBalancer;
import com.api.calculator.stockprice.ws.data.model.PDFFile;
import com.api.calculator.stockprice.ws.data.repository.OperationRepository;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.ws.data.model.Operation;
import com.api.calculator.stockprice.ws.data.repository.PDFFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class OperationService {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private PDFFileRepository pdfFileRepository;

    public int countByUserId(UUID userId){
        return operationRepository.countByUserId(userId);
    }

    public List<Operation> findAllByUserId(UUID userId){
        return operationRepository.findByUserId(userId);
    }

    public ByteArrayInputStream exportAsCsv(UUID userId) throws IOException {

        Map<String, String> pdfFileMap = new HashMap<>();
        for(PDFFile pdfFile: pdfFileRepository.findByUserId(userId)){
            pdfFileMap.put(pdfFile.getFileId().toString(), pdfFile.getName());
        }

        List<CsvGenerator.OperationCSV> operationCSVList = new ArrayList<>();
        for(Operation operation: operationRepository.findByUserId(userId)){
            operationCSVList.add(new CsvGenerator.OperationCSV(operation, pdfFileMap.get(operation.getFileId().toString()) ));
        }

        return CsvGenerator.generateOperations(operationCSVList);
    }

    public List<Operation> findAllByUserId(UUID userId, int page, int quantity) {

        return operationRepository.findAllByUserId(userId,
                PageRequest.of(page, quantity, Sort.by(Sort.Direction.DESC, "closeMonth")));
    }
    public Operation save(UUID userId, Operation operation){
        operation.setUserId(userId);
        return operationRepository.save(operation);
    }

    public Operation update(UUID authUserId, Operation updateOperation) throws ResourceNotFoundException {

        Optional<Operation> optionalOperation = operationRepository.findByIdAndUserId(updateOperation.getId(), authUserId);

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
        operationRepository.deleteByFileIdAndUserId(userId, fileId);
    }

    public void deleteById(UUID userId, long operationId){
        operationRepository.deleteByIdAndUserId(operationId, userId);
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

    public void deleteAllByUserId(UUID userId){
        operationRepository.deleteAllByUserId(userId);
    }
}
