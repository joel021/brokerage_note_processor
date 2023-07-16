package com.api.calculator.stockprice.controller;

import com.api.calculator.stockprice.exceptions.NotAcceptedException;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.model.Operation;
import com.api.calculator.stockprice.model.User;
import com.api.calculator.stockprice.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/users/operations")
public class OperationController {

    @Autowired
    private OperationService operationService;

    @PostMapping("/")
    public ResponseEntity<Operation> saveOperation(@RequestBody Operation operation){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(operationService.save(user.getId(), operation));
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> listOperations(@RequestParam int page, @RequestParam int quantity){
        Map<String, Object> resp = new HashMap<>();

        if (page < 0 || quantity < 1){
            resp.put("error", "A página mínima é Nº0. A quantidade deve ser maior ou igual a 1.");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON)
                    .body(resp);
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        resp.put("operations", operationService.findAllByUserId(user.getId(), page, quantity));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(resp);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countOperations(){
        Map<String, Object> resp = new HashMap<>();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        resp.put("quantity", operationService.countByUserId(user.getId()));

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(resp);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @RequestBody Map<String, Object> operationMap) throws ResourceNotFoundException {
        Map<String, Object> resp = new HashMap<>();
        if (operationMap.get("id") == null){
            resp.put("errors", Collections.singletonList("operationId não pode ser nulo."));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp);
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(operationService.update(user.getId(), Operation.get(operationMap)));
        } catch (NotAcceptedException e) {
            resp.put("errors", e.getErrors());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON).body(resp);
        }
    }

    @DeleteMapping("/by_file/{fileId}")
    public ResponseEntity<Map<String, String>> deleteByFileId(@PathVariable String fileId){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        operationService.deleteByFileId(user.getId(), UUID.fromString(fileId));

        Map<String,String> respMap = new HashMap<>();
        respMap.put("message", "Deletados, caso existiram.");
        return ResponseEntity.status(HttpStatus.OK).body(respMap);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteOperations(@PathVariable long operationId){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        operationService.deleteById(user.getId(), operationId);
        Map<String,String> respMap = new HashMap<>();
        respMap.put("message", "Deletados, caso existiram.");

        return ResponseEntity.status(HttpStatus.OK).body(respMap);
    }

    @GetMapping("/overall_profit_by_month")
    public ResponseEntity<List<Object>> overallProfitByMonth(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(operationService.sumValuesPerMonth(user.getId()));
    }

    @GetMapping("/profit_month_typeop_typemarket")
    public ResponseEntity<List<Object>> sumValuesPerMonthWhereTypeMarketAndTypeOp(@RequestParam String typeMarket,
                                                                                  @RequestParam String typeOp){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(HttpStatus.OK)
                .body(operationService.sumValuesPerMonthWhereTypeMarketAndTypeOp(user.getId(), typeMarket, typeOp));
    }

    @GetMapping("/profit_per_active")
    public ResponseEntity<List<Object>> activePerActive(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(HttpStatus.OK)
                .body(operationService.sumValuesPerActive(user.getId()));
    }

}
