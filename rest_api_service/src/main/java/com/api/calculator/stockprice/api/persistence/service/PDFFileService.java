package com.api.calculator.stockprice.api.persistence.service;

import com.api.calculator.stockprice.brokerage.extractor.operation.BrokerageOperationsHandler;
import com.api.calculator.stockprice.api.persistence.model.ExtractionError;
import com.api.calculator.stockprice.api.persistence.model.Operation;
import com.api.calculator.stockprice.api.persistence.model.PDFFile;
import com.api.calculator.stockprice.api.persistence.model.User;
import com.api.calculator.stockprice.api.persistence.repository.PDFFileRepository;
import com.api.calculator.stockprice.exceptions.NotAllowedException;
import com.api.calculator.stockprice.exceptions.ResourceAlreadyExists;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.exceptions.InternalException;

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

    @Autowired
    private PdfExtractorService pdfExtractorService;

    public void init() throws InternalException {
        try {
            Files.createDirectories(root);
        }catch (IOException e){
            throw new InternalException("O sistema não pôde definir um local para salvar o seu arquivo. Por favor, contate o admin.");
        }
    }

    private PDFFile findByIdOfUser(UUID fileId, UUID userId) throws ResourceNotFoundException {

        Optional<PDFFile> optionalPDFFile = pdfFileRepository.findByFileIdAndUserId(fileId,userId);
        if (optionalPDFFile.isPresent()) {
            return optionalPDFFile.get();
        }
        throw new ResourceNotFoundException("File not found with the current id provided.");
    }

    boolean isAbleToBeExtracted(PDFFile pdfFileExists, PDFFile newPdfFile){

        return pdfFileExists.getUpdatedAt() == null || (newPdfFile.getUpdatedAt() != null
                && pdfFileExists.getExtractedAt() == null && pdfFileExists.getDeletedAt() == null
                && TimeUnit.DAYS.convert(new Date().getTime() - pdfFileExists.getUpdatedAt().getTime(),
                TimeUnit.MILLISECONDS) > 1);
    }

    public void update(UUID userId, PDFFile pdfFile) throws ResourceNotFoundException, NotAllowedException, InternalException {

        PDFFile pdfFileExists = findByIdOfUser(pdfFile.getFileId(), userId);

        if (isAbleToBeExtracted(pdfFileExists, pdfFile)){
            pdfExtractorService.requestToExtractAllOfUser(userId, root.toUri().toString());
        }
        pdfFile.setUpdatedAt(new Date());
        pdfFile.setName(pdfFileExists.getName());

        pdfFileRepository.save(pdfFile);
    }

    public PDFFile create(User owner, MultipartFile multipartFile, PDFFile fileToSave) throws InternalException, ResourceAlreadyExists {

        fileToSave.setUserId(owner.getId());
        fileToSave.setName(multipartFile.getOriginalFilename()+"-"+owner.getId());
        fileToSave.setUpdatedAt(null);

        try {
            InputStream pdfInputStream = multipartFile.getInputStream();
            OutputStream pdfOutputStream = new FileOutputStream(this.root.resolve(fileToSave.getName()).toFile());
            PDDocument pdfDocument = PDDocument.load(pdfInputStream, fileToSave.getPassword());
            pdfDocument.save(pdfOutputStream);
            pdfOutputStream.close();
            pdfDocument.close();
        } catch (IOException e) {
            throw new InternalException("O arquivo está corrompido ou não foi enviado completamente. A senha também pode" +
                    " estar errada.");
        }

        return pdfFileRepository.save(fileToSave);
    }

    public Map<String, Object> load(UUID authUserId, UUID fileId)
            throws InternalException, NoSuchElementException, NotAllowedException, ResourceNotFoundException {

        PDFFile pdfFile = findByIdOfUser(fileId, authUserId);

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
    }

    public void deleteById(UUID authUserId, UUID fileId) throws ResourceNotFoundException, NotAllowedException {

        PDFFile pdfFile = findByIdOfUser(fileId, authUserId);

        try {
            Resource resource = new UrlResource(root.resolve(pdfFile.getName()).toUri());
            new File(resource.getURI().getPath()).delete();
        } catch (IOException ignored) {}

        pdfFile.setDeletedAt(new Date());
        pdfFileRepository.save(pdfFile);
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
