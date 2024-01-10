package com.api.calculator.stockprice.controller;

import com.api.calculator.stockprice.TestsUtils;
import com.api.calculator.stockprice.exceptions.ResourceAlreadyExists;
import com.api.calculator.stockprice.api.persistence.model.Operation;
import com.api.calculator.stockprice.api.persistence.model.User;
import com.api.calculator.stockprice.api.persistence.repository.OperationRepository;
import com.api.calculator.stockprice.api.persistence.repository.UserRepository;
import com.api.calculator.stockprice.api.persistence.service.user.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OperationsControllerTests {

    @Inject
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private OperationRepository operationRepository;

    private User owner;

    private Map<String, Object> userAuth;


    @Before
    public void beforeAach() throws ResourceAlreadyExists {

        owner = authService.signup(new User("Operation tester", "operationtester@gmail.com", "password", "USER", null));
        owner.setPassword("password");
        userAuth = authService.signin(owner);

        for(int i = 0; i < 4; i++){
            operationRepository.save(new Operation(null, owner.getId(), "operation", "active", 100, 1000f,
                    new java.sql.Date(System.currentTimeMillis()), "SWING TRADE", "VISTA", null,
                    "CLOSED", "2023-07","000000"));
        }
    }

    @After
    public void afterEach() {
        userRepository.deleteById(owner.getId());
        operationRepository.deleteAllByUserId(owner.getId());
    }

    @Test
    public void saveWithEmptiesTest() throws Exception {

        Operation operation = new Operation(null, null, "", "", 0, 0f, null, "","",
                null, "", null, null);

        final MvcResult result2 = mockMvc.perform(
                post("/api/users/operations/").contentType(TestsUtils.CONTENT_TYPE)
                        .content(TestsUtils.objectToJson(operation)).header("authorization", "Bearer " + userAuth.get("token"))
        ).andReturn();

        final HashMap<String, Object> responseEmpty = new ObjectMapper().readValue(result2.getResponse().getContentAsString(), HashMap.class);

        assert (responseEmpty.get("errors") != null);
    }

    @Test
    public void saveWithNullsTest() throws Exception {

        Operation operation = new Operation(null, null, null, null, 1, 1f, null, null,
                null, null, null, null, null);

        final MvcResult result2 = mockMvc.perform(
                post("/api/users/operations/").contentType(TestsUtils.CONTENT_TYPE)
                        .content(TestsUtils.objectToJson(operation)).header("authorization", "Bearer " + userAuth.get("token"))
        ).andExpect(status().isBadRequest()).andReturn();

        final HashMap<String, Object> responseEmpty = new ObjectMapper().readValue(result2.getResponse().getContentAsString(), HashMap.class);

        assert (responseEmpty.get("errors") != null);
    }

    @Test
    public void dreamSaveTest() throws Exception {

        Operation operation = new Operation(null, null, "ACTIVENAME23", "ACTIVE", 100, 8000f,
                new java.sql.Date(new Date().getTime()), "SWINGTRADE","VISTA",
                null, "SOLD", "2023-04", null);

        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> operatioMap = oMapper.convertValue(operation, Map.class);

        final MvcResult result2 = mockMvc.perform(
                post("/api/users/operations/").contentType(TestsUtils.CONTENT_TYPE)
                        .content(TestsUtils.objectToJson(operatioMap)).header("authorization", "Bearer " + userAuth.get("token"))
        ).andExpect(status().isOk()).andReturn();

        final HashMap<String, Object> operationFromResponse = new ObjectMapper().readValue(result2.getResponse().getContentAsString(), HashMap.class);
        assert (operationFromResponse.get("id") != null);
        operationRepository.deleteById(Long.parseLong(operationFromResponse.get("id").toString()));
    }

    @Test
    public void updateTest() throws Exception {
        Operation operation = new Operation(null, UUID.fromString(userAuth.get("userId").toString()),
                "ACTIVENAME23", "ACTIVE", 100, 8000f,
                new java.sql.Date(new GregorianCalendar().getTimeInMillis()), "SWINGTRADE","VISTA",
                null, "SOLD", "2023-04", null);
        operation = operationRepository.save(operation);

        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> operatioMap = oMapper.convertValue(operation, Map.class);
        operatioMap.put("date", "2023-05-18");
        mockMvc.perform(
                patch("/api/users/operations/" + operation.getId()).contentType(TestsUtils.CONTENT_TYPE)
                        .content(TestsUtils.objectToJson(operatioMap)).header("authorization", "Bearer " + userAuth.get("token"))
        ).andExpect(status().isOk());

        operationRepository.deleteById(operation.getId());
    }

    @Test
    public void updateExistentButIsNotFromTheUserTest() throws Exception {

        Operation operation = new Operation(null, null, "ACTIVENAME23", "ACTIVE", 100, 8000f,
                new java.sql.Date(new GregorianCalendar().getTimeInMillis()), "SWINGTRADE","VISTA",
                null, "SOLD", "2023-04", null);
        operation = operationRepository.save(operation);

        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> operationMap = oMapper.convertValue(operation, Map.class);
        operationMap.put("date", "2023-05-18");
        mockMvc.perform(
                patch("/api/users/operations/" + operation.getId()).contentType(TestsUtils.CONTENT_TYPE)
                        .content(TestsUtils.objectToJson(operationMap))
                        .header("authorization", "Bearer " + userAuth.get("token"))
        ).andExpect(status().isNotFound());
    }

    @Test
    public void exportAsCsvTest() throws Exception {

        MockHttpServletResponse response = mockMvc.perform(get("/api/users/operations/export_as_csv")
                .header("authorization", "Bearer " + userAuth.get("token")))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assert "application/csv".equals(response.getContentType());
        System.out.println(response.getContentAsString());
    }

}
