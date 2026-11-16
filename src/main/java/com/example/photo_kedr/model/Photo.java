package com.example.photo_kedr.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
	private int id;
	private int albumId;
	private String fileName;
	private String storageName;
	private String contentType;
	private String description;
	private LocalDate photoDate;
}
