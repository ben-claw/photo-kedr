package com.example.photo_kedr.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.photo_kedr.model.User;
import com.example.photo_kedr.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/debug-api")
public class DebugAPI {
    private final UserRepository userRepository;
    
    DebugAPI(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping("/users/list")
    List<User> listUsers(){
        return userRepository.findAll();
    }
    
    @GetMapping("/users")
    Optional<User> listUsers(@RequestParam String login){
        log.info("Request user " + login);
        return userRepository.findByLogin(login);
    }
    
    @PostMapping("/users/add")
    User post(@RequestBody User newUser){
         if (userRepository.findByLogin(newUser.getLogin()).isEmpty()) {
             return userRepository.add(newUser);
         } else {
             log.warn("User " + newUser.getLogin() + " already exists!");
             return null;
         }
    }

    @PostMapping("/users/update")
    void update(@RequestBody User user){
         userRepository.update(user);
    }
}
