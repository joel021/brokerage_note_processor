package com.api.calculator.stockprice.api.persistence.service.user;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.api.calculator.stockprice.api.persistence.repository.UserRepository;
import com.api.calculator.stockprice.api.persistence.service.google.GmailService;
import com.api.calculator.stockprice.api.persistence.service.google.GmailServiceImpl;
import com.api.calculator.stockprice.exceptions.InternalException;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.exceptions.UnauthorizedException;
import com.api.calculator.stockprice.api.persistence.model.GmailCredentials;
import com.api.calculator.stockprice.api.security.JwtTokenProvider;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.calculator.stockprice.exceptions.ResourceAlreadyExists;
import com.api.calculator.stockprice.api.persistence.model.Role;
import com.api.calculator.stockprice.api.persistence.model.User;

import javax.mail.MessagingException;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService searchUser;

    @Value("${google.auth.email}")
    private String userEmail;

    @Value("${google.auth.client.id}")
    private String clientId;
    @Value("${google.auth.client.secret}")
    private String clientSecret;
    @Value("${google.auth.access.token}")
    private String accessToken;
    @Value("${google.auth.refresh.token}")
    private String refreshToken;

    public HashMap<String, Object> signin(User loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();

        HashMap<String, Object> credentials = new HashMap<>();
        credentials.put("name", userDetails.getName());
        credentials.put("email", userDetails.getEmail());
        credentials.put("userId", userDetails.getId().toString());
        credentials.put("token", jwt);
        credentials.put("role", userDetails.getRole());

        return credentials;
    }

    public User signup(User user) throws ResourceAlreadyExists {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        User userFound = searchUser.findByEmail(user.getEmail());
        if (userFound != null) {
            throw new ResourceAlreadyExists(null);
        }

        return userRepository.save(
                new User(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        bCryptPasswordEncoder.encode(user.getPassword()),
                        Role.USER,
                        null)
        );
    }

    public void sendVerificationCode(String email) throws ResourceNotFoundException, InternalException {

        List<User> users = userRepository.findByEmail(email);

        if (!users.isEmpty()){
            try {
                GmailService gmailService = new GmailServiceImpl(GoogleNetHttpTransport.newTrustedTransport());

                GmailCredentials credentials = new GmailCredentials(userEmail,clientId,clientSecret, accessToken,refreshToken);
                gmailService.setGmailCredentials(credentials);

                String verificationCode = ""+ (int) Math.floor(Math.random() * 9001 + 1000);

                User user = users.get(0);
                gmailService.sendMessage(user.getEmail(),
                        "Código de verificação conta",
                        "Olá!\n" +
                        "Seu código de verificação na plataforma Relatório Leão é:\n" +
                        ""+verificationCode);
                user.setVerificationCode(new BCryptPasswordEncoder().encode(verificationCode));

                userRepository.save(user);
            } catch (GeneralSecurityException | IOException | MessagingException e) {
                throw new InternalException("Não foi possível enviar um email para você. Por favor, Contate o adminstrador.");
            }
        }else{
            throw new ResourceNotFoundException("Não encontrei uma conta com esse email. Por favor, verifique se digitou corretamente.");
        }
    }

    public HashMap<String, Object> confirmAccount(String email, String code) throws UnauthorizedException, ResourceNotFoundException {

        List<User> users = userRepository.findByEmail(email);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!users.isEmpty()){
            if (users.get(0).getVerificationCode().equals(encoder.encode(code))){

                String verificationCode = ""+ (int) Math.floor(Math.random() * 9001 + 1000);

                User user = new User(users.get(0).getId(), users.get(0).getName(), users.get(0).getEmail(),
                        verificationCode, users.get(0).getRole(), null);

                userRepository.saveAndFlush(new User(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        new BCryptPasswordEncoder().encode(verificationCode),
                        Role.USER,
                        user.getVerificationCode()));

                return signin(user);
            }else{
                throw new UnauthorizedException("Os códigos não conferem.");
            }
        }

        throw new ResourceNotFoundException("Este email ainda não foi cadastrado.");
    }

    public Map<String, Object> authWithGoogleAccount(User user) {

        List<User> users = userRepository.findByGoogleUserId(user.getGoogleUserId());
        Map<String, Object> userAuthenticated = new HashMap<>();

        if (users.isEmpty()){

            User userFound = searchUser.findByEmail(user.getEmail());
            if (userFound != null) {
                userFound.setGoogleUserId(user.getGoogleUserId());
                user.setId(userFound.getId());
                userRepository.save(userFound);
            }else{
                user.setRole(Role.USER);
                user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
                User result = userRepository.save(user);
                user.setId(result.getId());
            }
        }else{
            user.setId(users.get(0).getId());
        }

        String token = jwtTokenProvider.generateJwtToken(user);

        userAuthenticated.put("role", user.getRole());
        userAuthenticated.put("name", user.getName());
        userAuthenticated.put("email", user.getEmail());
        userAuthenticated.put("userId", user.getId().toString());
        userAuthenticated.put("token", token);

        return userAuthenticated;
    }
}
