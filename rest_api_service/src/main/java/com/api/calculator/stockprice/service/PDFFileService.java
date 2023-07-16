package com.api.calculator.stockprice.service;

import com.api.calculator.stockprice.brokerage.note.BrokerageOperationsHandler;
import com.api.calculator.stockprice.exceptions.NotAllowedException;
import com.api.calculator.stockprice.exceptions.ResourceAlreadyExists;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.model.*;
import com.api.calculator.stockprice.exceptions.InternalException;

import com.api.calculator.stockprice.repository.PDFFileRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PDFFileService implements BrokerageOperationsHandler.Callback {

    private final Path root = Paths.get("static/uploads");

    @Autowired
    private PDFFileRepository pdfFileRepository;

    @Autowired
    private OperationService operationService;

    @Autowired
    private ExtractionErrorService extractionErrorService;

    public void init() throws InternalException {
        try {
            Files.createDirectories(root);
        }catch (IOException e){
            throw new InternalException("O sistema não pôde definir um local para salvar o seu arquivo. Por favor, contate o admin.");
        }
    }

    public void update(UUID userId, PDFFile pdfFile) throws ResourceNotFoundException, NotAllowedException {
        Optional<PDFFile> optionalPDFFile = pdfFileRepository.findById(pdfFile.getFileId());

        if (optionalPDFFile.isPresent()){
            PDFFile pdfFileExists = optionalPDFFile.get();

            if (pdfFileExists.getUserId().equals(userId)){

                if (pdfFileExists.getUpdatedAt() == null || (pdfFile.getUpdatedAt() != null
                        && pdfFileExists.getExtractedAt() == null && pdfFileExists.getDeletedAt() == null
                        && TimeUnit.DAYS.convert(new Date().getTime() - pdfFileExists.getUpdatedAt().getTime(),
                        TimeUnit.MILLISECONDS) > 1)){

                    new BrokerageOperationsHandler().processPdfFile(this, userId, pdfFileExists.getFileId(),
                            this.root.resolve(pdfFileExists.getName()).toFile().toString(), operationService.findAllByUserId(userId));
                    pdfFile.setUpdatedAt(new Date());

                    pdfFile.setExtractedAt(new Date(System.currentTimeMillis()));
                } else {
                    pdfFile.setUpdatedAt(pdfFileExists.getUpdatedAt());
                }

                pdfFile.setName(pdfFileExists.getName());
                pdfFile.setExtractedAt(pdfFileExists.getExtractedAt());
                pdfFile.setDeletedAt(pdfFileExists.getDeletedAt());

                pdfFileRepository.save(pdfFile);
            } else {
                throw new NotAllowedException("An internal error occurred. You do not have the right permissions.");
            }
        }else{
            throw new ResourceNotFoundException("File not found with the current id provided.");
        }
    }

    public PDFFile create(User owner, MultipartFile multipartFile, PDFFile fileToSave) throws InternalException, ResourceAlreadyExists {

        fileToSave.setUserId(owner.getId());
        fileToSave.setName(owner.getEmail()+"-"+multipartFile.getOriginalFilename());
        fileToSave.setUpdatedAt(null);

        try {
            InputStream pdfInputStream = multipartFile.getInputStream();
            OutputStream pdfOutputStream = new FileOutputStream(this.root.resolve(fileToSave.getName()).toFile());
            PDDocument pdfDocument = PDDocument.load(pdfInputStream, fileToSave.getPassword());
            pdfDocument.setAllSecurityToBeRemoved(true);
            pdfDocument.save(pdfOutputStream);
            pdfOutputStream.close();
            pdfDocument.close();
        } catch (IOException e) {
            throw new InternalException("O arquivo está corrompido ou não foi enviado completamente. A senha também pode" +
                    " estar errada.");
        }
        new BrokerageOperationsHandler().processPdfFile(this, owner.getId(), fileToSave.getFileId(),
                this.root.resolve(fileToSave.getName()).toFile().toString(), operationService.findAllByUserId(owner.getId()));
        fileToSave.setExtractedAt(new Date(new GregorianCalendar().getTimeInMillis()));
        return pdfFileRepository.save(fileToSave);
    }

    public Map<String, Object> load(UUID authUserId, UUID fileId) throws InternalException, NoSuchElementException, NotAllowedException {

        PDFFile pdfFile = pdfFileRepository.findById(fileId).get();

        if (authUserId.equals(pdfFile.getUserId())){
            try {
                Path file = root.resolve(pdfFile.getName());
                UrlResource resource = new UrlResource(file.toUri());

                if (resource.exists() || resource.isReadable()) {

                    HashMap<String, Object> result = new HashMap<>();
                    result.put("length", resource.getFile().length());
                    result.put("inputStream", resource.getInputStream());
                    return result;
                } else {
                    throw new InternalException("Não pude ler o arquivo!");
                }
            } catch (IOException e) {
                throw new InternalException("Houve um problema interno. Perdi seu arquivo. Envie ele novamente, caso queira fazer alguma ação com ele neste sistema.");
            }
        }else{
            throw new NotAllowedException("Você não pode acessar este arquivo.");
        }
    }

    public void deleteById(UUID authUserId, UUID fileId) throws ResourceNotFoundException, NotAllowedException {

        Optional<PDFFile> pdfFileOptional = pdfFileRepository.findById(fileId);

        if (pdfFileOptional.isPresent()){

            PDFFile pdfFile = pdfFileOptional.get();

            if(pdfFile.getUserId().equals(authUserId)){
                try {
                    Resource resource = new UrlResource(root.resolve(pdfFile.getName()).toUri());
                    new File(resource.getURI().getPath()).delete();
                } catch (IOException ignored) {}

                pdfFile.setDeletedAt(new Date());
                pdfFileRepository.save(pdfFile);
            }else{
                throw new NotAllowedException("Você não tem acesso a este arquivo.");
            }
        }else{
            throw new ResourceNotFoundException("Não consigo encontrar um arquivo com essa identificação.");
        }

    }

    public List<PDFFile> findByUserId(UUID userId, int page, int quatity){
        return pdfFileRepository.findAllByUserId(userId, PageRequest.of(page, quatity));
    }
    public long countByUserId(UUID userId){
        return pdfFileRepository.countByUserId(userId);
    }

    @Override
    public void onProcessBrokerage(UUID userId, List<Operation> closedOperations, List<Operation> openedOperations, List<ExtractionError> errors) {

        for(Operation operation: closedOperations){
            operationService.save(userId, operation);
        }

        for(Operation operation: openedOperations){
            operationService.save(userId, operation);
        }

        for(ExtractionError error: errors){
            extractionErrorService.save(error);
        }
    }
}
