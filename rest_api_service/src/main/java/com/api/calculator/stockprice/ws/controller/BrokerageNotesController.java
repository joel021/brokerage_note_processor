package com.api.calculator.stockprice.ws.controller;

import com.api.calculator.stockprice.exceptions.*;
import com.api.calculator.stockprice.ws.data.model.PDFFile;
import com.api.calculator.stockprice.ws.data.model.User;
import com.api.calculator.stockprice.ws.data.service.PDFFileService;
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
                                                    @RequestParam("password") String password) throws NotAcceptedException {

        if(!Objects.requireNonNull(file.getContentType()).equalsIgnoreCase("application/pdf")){
            throw new NotAcceptedException("Accept only PDF files.");
        }

        Map<String, Object> response = new HashMap<>();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            response.put("fileId", pdfFileService.create(user, file,
                    new PDFFile(null,
                        user.getId(),
                        null,
                            password,
                            stockBroker,
                        null
            )).getFileId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InternalException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }catch (ResourceAlreadyExists e) {
            response.put("errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PatchMapping("/")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> fileInfo){

        HashMap<String, Object> response = new HashMap<>();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Date updatedAt = null;

        if (fileInfo.get("updatedAt") != null){
            updatedAt = Date.from(Instant.parse(fileInfo.get("updatedAt").toString()));
        }

        PDFFile pdfFile = new PDFFile(UUID.fromString(fileInfo.get("fileId").toString()), user.getId(), null,
                (String) fileInfo.get("password"), (String) fileInfo.get("stockBroker"), updatedAt);

        try {
            pdfFileService.update(user.getId(), pdfFile);
            response.put("fileId", pdfFile.getFileId());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (InternalException | NotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<?> download(@PathVariable String fileId) throws InternalException, NotAllowedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            Map<String, Object> result = pdfFileService.load(user.getId(), UUID.fromString(fileId));

            return ResponseEntity.ok()
                    .contentLength((long) result.get("length"))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body((InputStream)result.get("inputStream"));
        } catch (ResourceNotFoundException | InternalException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        } catch (NotAllowedException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
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
    public ResponseEntity<Map<String, List<String>>>  deleteById(@PathVariable String fileId) {
        Map<String, List<String>> respMap = new HashMap();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            pdfFileService.deleteById(user.getId(), UUID.fromString(fileId));
            respMap.put("message", Collections.singletonList("Deletado com sucesso"));
            return ResponseEntity.status(HttpStatus.OK).body(respMap);
        } catch (ResourceNotFoundException e) {
            respMap.put("errors", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(e.status).body(respMap);
        } catch (NotAllowedException e){
            respMap.put("errors", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(e.status).body(respMap);
        }
    }

}
