package com.example.photo_kedr.controllers;

import com.example.photo_kedr.model.Album;
import com.example.photo_kedr.services.AlbumService;
import com.example.photo_kedr.services.PhotoService;
import com.example.photo_kedr.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/albums")
@Controller
public class AlbumController {
	private final AlbumService albumService;
	private final PhotoService photoService;
	private final UserService userService;

	public AlbumController(
		AlbumService albumService,
		PhotoService photoService,
		UserService userService
	) {
		this.albumService = albumService;
		this.photoService = photoService;
		this.userService = userService;
	}

	@GetMapping("/new")
	public String newAlbumForm(Model model) {
		model.addAttribute("albumForm", new AlbumForm());
		return "album-form";
	}

	@PostMapping
	public String createAlbum(
		@ModelAttribute AlbumForm albumForm,
		Authentication authentication,
		Model model
	) {
		try {
			int ownerId = currentUserId(authentication);
			Album album = albumService
				.createAlbum(ownerId, albumForm.getName());
			return "redirect:/albums/" + album.getId();
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			return "album-form";
		}
	}

	@GetMapping("/{id}")
	public String viewAlbum(
		@PathVariable int id,
		@RequestParam(required = false) String error,
		Authentication authentication,
		Model model
	) {
		int ownerId = currentUserId(authentication);
		model.addAttribute("album", albumService.getAlbum(id, ownerId));
		model.addAttribute(
			"photos",
			photoService.getPhotosForAlbum(id, ownerId)
		);
		if (error != null) {
			model.addAttribute("error", error);
		}
		return "album";
	}

	@PostMapping("/{id}/photos")
	public String uploadPhoto(
		@PathVariable int id,
		@RequestParam MultipartFile file,
		@RequestParam(required = false) Long lastModified,
		Authentication authentication
	) {
		try {
			photoService.upload(
				id,
				currentUserId(authentication),
				file,
				lastModified
			);
			return "redirect:/albums/" + id;
		} catch (IllegalArgumentException exception) {
			return "redirect:/albums/" + id + "?error="
				+ java.net.URLEncoder.encode(
					exception.getMessage(),
					java.nio.charset.StandardCharsets.UTF_8
				);
		}
	}

	private int currentUserId(Authentication authentication) {
		return userService.getProfile(authentication.getName()).id();
	}

	public static class AlbumForm {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
