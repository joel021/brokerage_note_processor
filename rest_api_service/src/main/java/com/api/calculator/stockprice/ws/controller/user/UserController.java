package com.api.calculator.stockprice.ws.controller.user;

import com.api.calculator.stockprice.exceptions.NotAcceptedException;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.ws.data.model.User;
import com.api.calculator.stockprice.ws.data.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PatchMapping("/")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> userUpdate) {
        User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            userService.update(authUser.getId(), userUpdate);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new HashMap<String, Object>());
        } catch (NotAcceptedException e) {
            Map<String, List<String>> errorObj = new HashMap<>();

            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(errorObj);
        }
    }
    @GetMapping("/")
    public ResponseEntity<?> fetchUser(){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userService.findById(user.getId()));
        }catch (ResourceNotFoundException e){
            HashMap<String, Object> errorBody = new HashMap<>();
            List<String> errors = new ArrayList<>();
            errors.add("Você não tem uma autenticação válida. Faça login e tente novamente.");
            errorBody.put("errors", errors);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorBody);
        }
    }


}
