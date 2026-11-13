package com.example.photo_kedr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class TemporaryController {
	@GetMapping("/")
	String index() {
		return "index";
	}

	@GetMapping("/hello")
	String hello() {
		return "hello";
	}
}
