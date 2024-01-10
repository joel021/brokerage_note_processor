package com.api.calculator.stockprice.api.controller.user;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.api.calculator.stockprice.exceptions.*;
import com.api.calculator.stockprice.api.persistence.model.User;
import com.api.calculator.stockprice.api.persistence.model.UserExtended;
import com.api.calculator.stockprice.api.persistence.model.GoogleAuthCredentials;
import com.api.calculator.stockprice.api.persistence.service.user.AuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/users")
public class AuthController {

    @Autowired
    private AuthService authService;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @PostMapping("/verification_code/{email}")
    public ResponseEntity<?> verificationCode(@PathVariable String email) throws ResourceNotFoundException, InternalException, NotAcceptedException {
        Map<String, String> resp = new HashMap<>();

        if (email != null){
            authService.sendVerificationCode(email);
            resp.put("message", "Código enviado para o e-mail fornecido.");
            return ResponseEntity.status(HttpStatus.OK).body(resp);
        }else{
            throw new NotAcceptedException("Você deve fornecer seu e-mail para recuperar a sua conta.");
        }
    }

    @PostMapping("/confirm_account")
    public ResponseEntity<?> confirmAccount(@RequestBody Map<String, Object> userCredentials) throws NotAllowedException, UnauthorizedException, ResourceNotFoundException {

        if (userCredentials.get("email") != null && userCredentials.get("code") != null){
            Map<String, Object> userAuthenticated = authService.confirmAccount((String) userCredentials.get("email"),
                    userCredentials.get("code").toString());

            return ResponseEntity.status(HttpStatus.OK).body(userAuthenticated);
        }else{
            throw new NotAllowedException("Você precisa fornecer o email e o código de confirmação.");
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody @Valid Map<String, String> loginRequest) {
        User user = new User();
        user.setPassword(loginRequest.get("password"));
        user.setEmail(loginRequest.get("email"));
        return ResponseEntity.status(HttpStatus.OK).body(authService.signin(user));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody @Valid UserExtended user)  {
        
        if (!user.getPassword().equals(user.getPasswordConfirmation())) {
            HashMap<String, Object> resp = new HashMap<>();
            resp.put("errors",
                    new ArrayList<>(Collections.singletonList("As senhas devem ser iguais.")));
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(resp);
        }

        try {
            authService.signup(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.signin(user));
        }catch(ResourceAlreadyExists err){
            HashMap<String, Object> resp = new HashMap<>();
            resp.put("errors", new ArrayList<>(Collections.singletonList("Este usuário já existe")));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }
        
    }

    @PostMapping("/auth_with_google")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> userToAuth) {
        HttpTransport transport;
        HashMap<String, Object> resp = new HashMap<>();

        User user = new User();
        user.setGoogleUserId(userToAuth.get("userGoogleId"));
        user.setName(userToAuth.get("name"));
        user.setEmail(userToAuth.get("email"));

        try {
            transport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            resp.put("error", "Erro ao validar o seu acesso.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }

        GoogleIdToken idToken;

        try {
            idToken = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
                    .setAudience(Collections.singletonList(GoogleAuthCredentials.clientId))
                    .build().verify(userToAuth.get("googleIdToken"));
        } catch (GeneralSecurityException | IOException e) {
            resp.put("error", "Erro ao validar suas credenciais.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }

        if (idToken != null) {
            user.setGoogleUserId(idToken.getPayload().getSubject());
            if (userToAuth.get("password") == null || userToAuth.get("password").isEmpty()){
                user.setPassword(userToAuth.get("googleIdToken").substring(0, 12));
            }else{
                user.setPassword(userToAuth.get("password"));
            }

            return ResponseEntity.ok(authService.authWithGoogleAccount(user));
        } else {
            resp.put("error", "Você não foi autenticado. Tente novamente.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

}
