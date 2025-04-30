package com.example.photo_kedr.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.photo_kedr.model.User;
import com.example.photo_kedr.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class RestAPI {
    private final UserRepository userRepository;
    
    RestAPI(UserRepository userRepository){
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
    
    /*@PostMapping
    String post(@RequestBody String newData){
    }*/
}
