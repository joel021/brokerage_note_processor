package com.api.calculator.stockprice.api.controller;

import com.api.calculator.stockprice.exceptions.*;
import com.api.calculator.stockprice.api.persistence.model.PDFFile;
import com.api.calculator.stockprice.api.persistence.model.User;
import com.api.calculator.stockprice.api.persistence.service.PDFFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("api/users/brokerage_notes")
public class BrokerageNotesController {

    @Autowired
    private PDFFileService pdfFileService;

    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> save(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("stockBroker") String stockBroker,
                                                    @RequestParam("password") String password) throws NotAcceptedException, ResourceAlreadyExists, InternalException {

        if(!Objects.requireNonNull(file.getContentType()).equalsIgnoreCase("application/pdf")){
            throw new NotAcceptedException("Accept only PDF files.");
        }

        Map<String, Object> response = new HashMap<>();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        response.put("fileId", pdfFileService.create(user, file,
                new PDFFile(null,
                        user.getId(),
                        null,
                        password,
                        stockBroker,
                        null
                )).getFileId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> fileInfo) throws NotAllowedException, ResourceNotFoundException, InternalException {

        HashMap<String, Object> response = new HashMap<>();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Date updatedAt = null;

        if (fileInfo.get("updatedAt") != null){
            updatedAt = Date.from(Instant.parse(fileInfo.get("updatedAt").toString()));
        }

        PDFFile pdfFile = new PDFFile(UUID.fromString(fileInfo.get("fileId").toString()), user.getId(), null,
                (String) fileInfo.get("password"), (String) fileInfo.get("stockBroker"), updatedAt);

        pdfFileService.update(user.getId(), pdfFile);
        response.put("fileId", pdfFile.getFileId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<?> download(@PathVariable String fileId) throws ResourceNotFoundException, NotAllowedException,
            InternalException {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Map<String, Object> result = pdfFileService.load(user.getId(), UUID.fromString(fileId));

        return ResponseEntity.ok()
                .contentLength((long) result.get("length"))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body((InputStream)result.get("inputStream"));
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> listFilesOfUser(@RequestParam int page, @RequestParam int quantity){
        Map<String, Object> respMap = new HashMap<>();

        if (page < 0 || quantity < 0){
            ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON)
                    .body("Página (page) e quantidade(quantity) devem ser positivos.");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        respMap.put("files", pdfFileService.findByUserId(user.getId(), page, quantity));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(respMap);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countByUserId(){

        HashMap<String, Object> respMap = new HashMap<>();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        respMap.put("quantity", pdfFileService.countByUserId(user.getId()));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(respMap);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String>  deleteById(@PathVariable String fileId) throws NotAllowedException, ResourceNotFoundException {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        pdfFileService.deleteById(user.getId(), UUID.fromString(fileId));
        return ResponseEntity.status(HttpStatus.OK).body("Deletado com sucesso");
    }

}
