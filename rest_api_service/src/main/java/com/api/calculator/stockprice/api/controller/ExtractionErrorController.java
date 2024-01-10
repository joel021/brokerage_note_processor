package com.api.calculator.stockprice.api.controller;


import com.api.calculator.stockprice.api.persistence.model.ExtractionError;
import com.api.calculator.stockprice.api.persistence.service.ExtractionErrorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("api/users/brokerage_notes/errors")
public class ExtractionErrorController {

    @Autowired
    private ExtractionErrorService extractionErrorService;

    @GetMapping("/by_file_id/{fileId}")
    public ResponseEntity<Map<String, List<ExtractionError>>> getByFileId(@PathVariable @Valid UUID fileId){
        Map<String, List<ExtractionError>> errors = new HashMap<>();
        errors.put("errors", extractionErrorService.findByFileId(fileId));
        return ResponseEntity.ok().body(errors);
    }

    @GetMapping("/count_by_file_id/{fileId}")
    public ResponseEntity<Map<String, Object>> countByFileId(@PathVariable @Valid UUID fileId){
        Map<String, Object> count = new HashMap<>();
        count.put("fileId", fileId.toString());
        count.put("count", extractionErrorService.countByFileId(fileId));
        return ResponseEntity.ok().body(count);
    }
}
