package com.api.calculator.stockprice.controller;

import com.api.calculator.stockprice.TestsUtils;
import com.api.calculator.stockprice.exceptions.ResourceAlreadyExists;
import com.api.calculator.stockprice.ws.data.model.User;
import com.api.calculator.stockprice.ws.data.repository.UserRepository;
import com.api.calculator.stockprice.ws.data.service.OperationService;
import com.api.calculator.stockprice.ws.data.service.PDFFileService;
import com.api.calculator.stockprice.ws.data.service.user.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BrokerageNotesControllerTests {

    @Inject
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PDFFileService pdfFileService;

    @Autowired
    private OperationService operationService;

    private User owner;

    private Map<String, Object> userAuth;

    private List<String> pdfFilesIds;


    @Before
    public void beforeAach() throws ResourceAlreadyExists {
        pdfFilesIds = new ArrayList<>();
        owner = authService.signup(new User("Owner PDF Tester", "owner-tester@gmail.com", "owner_password", "USER", null));
        owner.setPassword("owner_password");
        userAuth = authService.signin(owner);
    }

    @After
    public void afterEach() throws Exception {
        userRepository.deleteById(owner.getId());
        for(String fileId: pdfFilesIds){
            pdfFileService.deleteById(owner.getId(), UUID.fromString(fileId));
        }
    }

    @Test
    public void dreamUploadAndSave() throws Exception {

        Files.createDirectories(Paths.get("static/pdf_tests/pdfs"));

        if (Paths.get("static/pdf_tests/pdfs").toFile().listFiles() == null){
            return;
        }
        for(File file: Paths.get("static/pdf_tests/pdfs").toFile().listFiles()){

            final MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/pdf", new FileInputStream(file));

            final MvcResult result = mockMvc.perform(
                    multipart("/api/users/brokerage_notes/")
                            .file(multipartFile)
                            .param("password", "aa")
                            .param("stockBroker", "Corretora")
                            .contentType(TestsUtils.CONTENT_TYPE)
                            .header("authorization", "Bearer " + userAuth.get("token"))
            ).andExpect(status().isCreated()).andReturn();

            final HashMap<String, Object> responseBody = new ObjectMapper().readValue(result.getResponse().getContentAsString(), HashMap.class);
            assert(responseBody.get("fileId") != null);
            pdfFilesIds.add(responseBody.get("fileId").toString());
            assert !operationService.findAllByUserId(owner.getId(), 0, 20).isEmpty();
        }

    }

}
