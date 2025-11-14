package edu.com.co.Proyecto.Final.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para subir imágenes
 * Permite que los administradores suban imágenes de productos
 */
@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {
	
	// Ruta donde se guardarán las imágenes
	private static final String UPLOAD_DIR = "src/main/resources/static/imagenes/";
	
	// Extensiones permitidas
	private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
	
	/**
	 * Subir una imagen
	 * POST /api/upload/imagen
	 * 
	 * @param file Archivo a subir
	 * @return JSON con el nombre del archivo guardado
	 */
	@PostMapping("/imagen")
	public ResponseEntity<Map<String, Object>> subirImagen(@RequestParam("file") MultipartFile file) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			// Validaciones básicas
			if (file.isEmpty()) {
				response.put("success", false);
				response.put("error", "El archivo está vacío");
				return ResponseEntity.badRequest().body(response);
			}
			
			// Validar tamaño
			if (file.getSize() > MAX_FILE_SIZE) {
				response.put("success", false);
				response.put("error", "El archivo excede el tamaño máximo de 5MB");
				return ResponseEntity.badRequest().body(response);
			}
			
			// Obtener nombre original y extensión
			String originalFilename = file.getOriginalFilename();
			String fileExtension = getFileExtension(originalFilename);
			
			// Validar extensión
			if (!isAllowedExtension(fileExtension)) {
				response.put("success", false);
				response.put("error", "Tipo de archivo no permitido. Use: jpg, jpeg, png, gif, webp");
				return ResponseEntity.badRequest().body(response);
			}
			
			// Generar nombre único para evitar conflictos
			String uniqueFileName = "img_" + UUID.randomUUID().toString() + "." + fileExtension;
			
			// Crear directorio si no existe
			File uploadDir = new File(UPLOAD_DIR);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}
			
			// Guardar el archivo
			Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
			Files.write(filePath, file.getBytes());
			
			// Respuesta exitosa con la ruta relativa
			response.put("success", true);
			response.put("filename", "imagenes/" + uniqueFileName);
			response.put("message", "Imagen subida exitosamente");
			response.put("url", "/imagenes/" + uniqueFileName);
			
			System.out.println("✓ Imagen subida: " + uniqueFileName);
			
			return ResponseEntity.ok(response);
			
		} catch (IOException e) {
			response.put("success", false);
			response.put("error", "Error al guardar el archivo: " + e.getMessage());
			System.out.println("✗ Error subiendo imagen: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	/**
	 * Obtener la extensión de un archivo
	 */
	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
	}
	
	/**
	 * Validar si la extensión está permitida
	 */
	private boolean isAllowedExtension(String extension) {
		for (String allowed : ALLOWED_EXTENSIONS) {
			if (allowed.equals(extension)) {
				return true;
			}
		}
		return false;
	}
}
