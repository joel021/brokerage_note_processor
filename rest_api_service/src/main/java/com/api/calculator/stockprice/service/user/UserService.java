package com.api.calculator.stockprice.service.user;

import com.api.calculator.stockprice.exceptions.NotAcceptedException;
import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.model.User;
import com.api.calculator.stockprice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByEmail(String email){
        List<User> users = userRepository.findByEmail(email);

        if (users.isEmpty()){
            return null;
        }else{
            return users.get(0);
        }
    }

    public User findById(UUID id) throws ResourceNotFoundException {
        Optional<User> result = userRepository.findById(id);
        if(!result.isPresent()){
            throw new ResourceNotFoundException("O usuário requisitado não existe.");
        }

        return result.get();
    }

    public void update(UUID userId, Map<String, Object> userMap) throws NotAcceptedException {

        String password = userMap.get("password") != null ? userMap.get("password").toString() : "";
        boolean passIsNotEmpty = !password.isEmpty();

        if (passIsNotEmpty && !password.equals(userMap.get("passwordConfirmation"))){
            throw new NotAcceptedException("As senhas devem conferir.");
        }

        String name = userMap.get("name") != null ? userMap.get("name").toString() : "";

        if (name.isEmpty()){
            throw new NotAcceptedException("Nome ou apelido é obrigatório.");
        }

        String email = userMap.get("email") != null ? userMap.get("email").toString() : "";

        if(email.isEmpty()){
            throw new NotAcceptedException("Email deve ser preenchido");
        }

        User oldUser = userRepository.findById(userId).get();

        User userUpdate = new User();

        if (passIsNotEmpty){
            userUpdate.setPassword(new BCryptPasswordEncoder().encode(password));
        }else{
            userUpdate.setPassword(oldUser.getPassword());
        }

        userUpdate.setName(name);
        userUpdate.setEmail(email);
        userUpdate.setVerificationCode(oldUser.getVerificationCode());
        userUpdate.setRole(oldUser.getRole());
        userUpdate.setId(userId);

        userRepository.save(userUpdate);
    }

}
