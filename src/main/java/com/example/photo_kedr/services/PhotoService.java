package com.example.photo_kedr.services;

import com.example.photo_kedr.model.Album;
import com.example.photo_kedr.model.Photo;
import com.example.photo_kedr.repositories.AlbumRepository;
import com.example.photo_kedr.repositories.PhotoRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoService {
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
	private static final int MAX_DESCRIPTION_LENGTH = 500;

	private final PhotoRepository photoRepository;
	private final AlbumRepository albumRepository;
	private final Path uploadDirectory;

	public PhotoService(
		PhotoRepository photoRepository,
		AlbumRepository albumRepository,
		@Value("${photo.upload-dir:./data/photos}") String uploadDirectory
	) {
		this.photoRepository = photoRepository;
		this.albumRepository = albumRepository;
		this.uploadDirectory = Path.of(uploadDirectory)
			.toAbsolutePath()
			.normalize();
	}

	public List<Photo> getPhotosForAlbum(int albumId, int ownerId) {
		getOwnedAlbum(albumId, ownerId);
		return photoRepository.findByAlbumId(albumId);
	}

	public Photo getPhoto(int photoId, int ownerId) {
		Photo photo = findPhoto(photoId);
		getOwnedAlbum(photo.getAlbumId(), ownerId);
		return photo;
	}

	@Transactional
	public Photo upload(
		int albumId,
		int ownerId,
		MultipartFile file,
		Long lastModifiedMillis
	) {
		getOwnedAlbum(albumId, ownerId);
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException(
				"Выберите файл фотографии."
			);
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException(
				"Размер фотографии не должен превышать 10 МБ."
			);
		}

		String originalName = Path
			.of(
				file.getOriginalFilename() == null
					? "фотография"
					: file.getOriginalFilename()
			)
			.getFileName()
			.toString();
		String extension = extensionOf(originalName);
		String contentType = allowedContentType(extension);
		if (contentType == null
			|| !hasExpectedSignature(file, contentType)) {
			throw new IllegalArgumentException(
				"Поддерживаются только фотографии JPEG, PNG и GIF."
			);
		}

		LocalDate date = fileDate(lastModifiedMillis);
		String storageName = UUID.randomUUID() + "." + extension;
		Path destination = uploadDirectory.resolve(storageName)
			.normalize();
		if (!destination.startsWith(uploadDirectory)) {
			throw new IllegalArgumentException(
				"Недопустимый файл."
			);
		}
		try {
			Files.createDirectories(uploadDirectory);
			file.transferTo(destination);
		} catch (IOException exception) {
			throw new IllegalStateException(
				"Не удалось сохранить фотографию.",
				exception
			);
		}

		try {
			return photoRepository.add(
				new Photo(
					0,
					albumId,
					originalName,
					storageName,
					contentType,
					originalName,
					date
				)
			);
		} catch (RuntimeException exception) {
			try {
				Files.deleteIfExists(destination);
			} catch (IOException cleanupException) {
				exception.addSuppressed(cleanupException);
			}
			throw exception;
		}
	}

	public Path getPhotoFile(Photo photo, int ownerId) {
		Photo ownedPhoto = getPhoto(photo.getId(), ownerId);
		Path file = uploadDirectory.resolve(ownedPhoto.getStorageName())
			.normalize();
		if (!file.startsWith(uploadDirectory)
			|| !Files.isRegularFile(file)) {
			throw new PhotoNotFoundException();
		}
		return file;
	}

	@Transactional
	public void updateDetails(
		int photoId,
		int ownerId,
		String description,
		LocalDate date
	) {
		Photo photo = getPhoto(photoId, ownerId);
		String normalizedDescription = description == null
			? ""
			: description.trim();
		if (normalizedDescription.isBlank()) {
			throw new IllegalArgumentException(
				"Укажите описание фотографии."
			);
		}
		if (normalizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
			throw new IllegalArgumentException(
				"Описание должно содержать не более 500 символов."
			);
		}
		if (date == null || date.isAfter(LocalDate.now().plusDays(1))) {
			throw new IllegalArgumentException(
				"Укажите корректную дату фотографии."
			);
		}
		photoRepository
			.updateDetails(photoId, normalizedDescription, date);
	}

	private Photo findPhoto(int photoId) {
		return photoRepository.findById(photoId)
			.orElseThrow(PhotoNotFoundException::new);
	}

	private Album getOwnedAlbum(int albumId, int ownerId) {
		return albumRepository.findById(albumId)
			.filter(album -> album.getOwner() == ownerId)
			.orElseThrow(PhotoNotFoundException::new);
	}

	private LocalDate fileDate(Long millis) {
		if (millis == null || millis <= 0) {
			return LocalDate.now();
		}
		try {
			LocalDate date = Instant.ofEpochMilli(millis)
				.atZone(ZoneOffset.UTC)
				.toLocalDate();
			if (date.isAfter(LocalDate.now().plusDays(1))) {
				return LocalDate.now();
			}
			return date;
		} catch (RuntimeException exception) {
			return LocalDate.now();
		}
	}

	private String extensionOf(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return dot < 0
			? ""
			: fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	private String allowedContentType(String extension) {
		return switch (extension) {
			case "jpg", "jpeg" -> "image/jpeg";
			case "png" -> "image/png";
			case "gif" -> "image/gif";
			default -> null;
		};
	}

	private boolean hasExpectedSignature(
		MultipartFile file,
		String contentType
	) {
		try {
			byte[] header = file.getBytes();
			return switch (contentType) {
				case "image/jpeg" -> header.length >= 3
					&& (header[0] & 0xff) == 0xff
					&& (header[1] & 0xff) == 0xd8
					&& (header[2] & 0xff) == 0xff;
				case "image/png" -> header.length >= 8
					&& (header[0] & 0xff) == 0x89
					&& header[1] == 'P' && header[2] == 'N'
					&& header[3] == 'G' && header[4] == 13
					&& header[5] == 10 && header[6] == 26
					&& header[7] == 10;
				case "image/gif" -> header.length >= 6
					&& header[0] == 'G' && header[1] == 'I'
					&& header[2] == 'F' && header[3] == '8'
					&& (header[4] == '7'
						|| header[4] == '9')
					&& header[5] == 'a';
				default -> false;
			};
		} catch (IOException exception) {
			return false;
		}
	}

	public static class PhotoNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public PhotoNotFoundException() {
			super("Фотография не найдена.");
		}
	}
}
