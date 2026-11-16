package com.example.photo_kedr.controllers;

import com.example.photo_kedr.model.Photo;
import com.example.photo_kedr.services.PhotoService;
import com.example.photo_kedr.services.UserService;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/photos")
@Controller
public class PhotoController {
	private final PhotoService photoService;
	private final UserService userService;

	public PhotoController(
		PhotoService photoService,
		UserService userService
	) {
		this.photoService = photoService;
		this.userService = userService;
	}

	@GetMapping("/{id}")
	public String viewPhoto(
		@PathVariable int id,
		@RequestParam(required = false) String error,
		Authentication authentication,
		Model model
	) {
		Photo photo = photoService
			.getPhoto(id, currentUserId(authentication));
		model.addAttribute("photo", photo);
		model.addAttribute(
			"photoForm",
			new PhotoForm(
				photo.getDescription(),
				photo.getPhotoDate()
			)
		);
		if (error != null) {
			model.addAttribute("error", error);
		}
		return "photo";
	}

	@GetMapping("/{id}/image")
	public ResponseEntity<Resource> image(
		@PathVariable int id,
		Authentication authentication
	) throws IOException {
		int ownerId = currentUserId(authentication);
		Photo photo = photoService.getPhoto(id, ownerId);
		Path path = photoService.getPhotoFile(photo, ownerId);
		return ResponseEntity.ok()
			.contentType(
				MediaType.parseMediaType(photo.getContentType())
			)
			.header("X-Content-Type-Options", "nosniff")
			.cacheControl(CacheControl.noCache())
			.body(new FileSystemResource(path));
	}

	@PostMapping("/{id}")
	public String updatePhoto(
		@PathVariable int id,
		@ModelAttribute PhotoForm photoForm,
		Authentication authentication
	) {
		try {
			photoService.updateDetails(
				id,
				currentUserId(authentication),
				photoForm.getDescription(),
				photoForm.getDate()
			);
			return "redirect:/photos/" + id;
		} catch (IllegalArgumentException exception) {
			return "redirect:/photos/" + id + "?error="
				+ java.net.URLEncoder.encode(
					exception.getMessage(),
					java.nio.charset.StandardCharsets.UTF_8
				);
		}
	}

	private int currentUserId(Authentication authentication) {
		return userService.getProfile(authentication.getName()).id();
	}

	public static class PhotoForm {
		private String description;
		private LocalDate date;

		public PhotoForm() {
		}

		public PhotoForm(String description, LocalDate date) {
			this.description = description;
			this.date = date;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public LocalDate getDate() {
			return date;
		}

		public void setDate(LocalDate date) {
			this.date = date;
		}
	}
}
