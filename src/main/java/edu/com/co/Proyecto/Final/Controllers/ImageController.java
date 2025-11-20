package edu.com.co.Proyecto.Final.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
 * Controlador unificado para gestionar imágenes
 * Maneja tanto la subida como la descarga/servicio de imágenes
 * 
 * Funcionalidades:
 * - POST /api/images/upload - Subir nuevas imágenes
 * - GET /imagenes/{nombreArchivo} - Descargar/servir imágenes
 */
@RestController
@Tag(name = "Gestión de Imágenes", description = "Endpoints para subir y servir imágenes del sistema")
public class ImageController {
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	// Ruta donde se guardarán las imágenes
	private static final String UPLOAD_DIR = "src/main/resources/static/imagenes/";
	
	// Extensiones permitidas
	private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "svg"};
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
	
	// ==================== SUBIDA DE IMÁGENES ====================
	
	/**
	 * Subir una imagen
	 * POST /api/images/upload
	 * 
	 * @param file Archivo a subir
	 * @return JSON con el nombre del archivo guardado
	 */
	@PostMapping("/api/images/upload")
	@Operation(
		summary = "Subir imagen",
		description = "Sube una imagen al servidor. Formatos permitidos: jpg, jpeg, png, gif, webp, svg. Tamaño máximo: 5MB"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Imagen subida exitosamente",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "{\"success\":true,\"filename\":\"imagenes/img_123e4567-e89b-12d3-a456-426614174000.jpg\",\"message\":\"Imagen subida exitosamente\",\"url\":\"/imagenes/img_123e4567-e89b-12d3-a456-426614174000.jpg\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "Error de validación - Archivo vacío, extensión no permitida o tamaño excedido",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "{\"success\":false,\"error\":\"El archivo excede el tamaño máximo de 5MB\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "500",
			description = "Error interno al guardar el archivo",
			content = @Content(mediaType = "application/json")
		)
	})
	public ResponseEntity<Map<String, Object>> subirImagen(
		@Parameter(
			description = "Archivo de imagen a subir (jpg, jpeg, png, gif, webp, svg - máx 5MB)",
			required = true
		)
		@RequestParam("file") MultipartFile file) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			// Validaciones básicas
			if (file.isEmpty()) {
				response.put("success", false);
				response.put("error", "El archivo está vacío");
				System.out.println("✗ Intento de subida: archivo vacío");
				return ResponseEntity.badRequest().body(response);
			}
			
			// Validar tamaño
			if (file.getSize() > MAX_FILE_SIZE) {
				response.put("success", false);
				response.put("error", "El archivo excede el tamaño máximo de 5MB");
				System.out.println("✗ Intento de subida: archivo > 5MB");
				return ResponseEntity.badRequest().body(response);
			}
			
			// Obtener nombre original y extensión
			String originalFilename = file.getOriginalFilename();
			String fileExtension = getFileExtension(originalFilename);
			
			// Validar extensión
			if (!isAllowedExtension(fileExtension)) {
				response.put("success", false);
				response.put("error", "Tipo de archivo no permitido. Use: jpg, jpeg, png, gif, webp, svg");
				System.out.println("✗ Intento de subida: extensión no permitida - " + fileExtension);
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
			
			System.out.println("✓ Imagen subida: " + uniqueFileName + " (tamaño: " + formatFileSize(file.getSize()) + ")");
			
			return ResponseEntity.ok(response);
			
		} catch (IOException e) {
			response.put("success", false);
			response.put("error", "Error al guardar el archivo: " + e.getMessage());
			System.out.println("✗ Error subiendo imagen: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	// ==================== DESCARGA DE IMÁGENES ====================
	
	/**
	 * Obtener imagen por nombre
	 * GET /imagenes/{nombreArchivo}
	 * Intenta buscar la imagen en múltiples ubicaciones
	 */
	@GetMapping("/imagenes/{nombreArchivo}")
	@Operation(
		summary = "Obtener imagen",
		description = "Retorna una imagen almacenada en el servidor. Busca automáticamente en múltiples ubicaciones y determina el tipo MIME correcto"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Imagen encontrada y retornada exitosamente",
			content = {
				@Content(mediaType = "image/jpeg"),
				@Content(mediaType = "image/png"),
				@Content(mediaType = "image/gif")
			}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Nombre de archivo inválido o inseguro",
			content = @Content(mediaType = "application/json")
		),
		@ApiResponse(
			responseCode = "404",
			description = "Imagen no encontrada",
			content = @Content(mediaType = "application/json")
		)
	})
	public ResponseEntity<Resource> obtenerImagen(
		@Parameter(
			description = "Nombre del archivo de imagen (ej: img_123.jpg)",
			required = true,
			example = "img_123e4567-e89b-12d3-a456-426614174000.jpg"
		)
		@PathVariable String nombreArchivo) {
		try {
			System.out.println("=== Obteniendo imagen ===");
			System.out.println("Buscando: " + nombreArchivo);
			
			// Validar que el nombre del archivo sea seguro (sin caracteres peligrosos)
			if (nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
				System.out.println("✗ ERROR: Nombre de archivo inválido");
				return ResponseEntity.badRequest().build();
			}
			
			// Intentar múltiples rutas
			String[] rutasIntento = {
				"classpath:/static/imagenes/" + nombreArchivo,
				"classpath:/imagenes/" + nombreArchivo,
				"file:src/main/resources/static/imagenes/" + nombreArchivo
			};
			
			Resource resource = null;
			String rutaExitosa = null;
			
			for (String ruta : rutasIntento) {
				System.out.println("  Intentando: " + ruta);
				Resource temp = resourceLoader.getResource(ruta);
				
				if (temp.exists() && temp.isReadable()) {
					resource = temp;
					rutaExitosa = ruta;
					System.out.println("  ✓ Encontrada en: " + ruta);
					break;
				}
			}
			
			// Verificar que el archivo existe y es legible
			if (resource == null || !resource.exists() || !resource.isReadable()) {
				System.out.println("✗ Imagen no encontrada: " + nombreArchivo);
				return ResponseEntity.notFound().build();
			}
			
			// Determinar el tipo de contenido
			MediaType mediaType = determinarMediaType(nombreArchivo);
			System.out.println("✓ Tipo MIME: " + mediaType);
			
			// Retornar la imagen
			System.out.println("✓ Retornando imagen exitosamente");
			return ResponseEntity.ok()
					.contentType(mediaType)
					.header("Cache-Control", "public, max-age=3600")
					.body(resource);
					
		} catch (Exception e) {
			System.out.println("✗ ERROR al obtener imagen: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}
	
	// ==================== MÉTODOS PRIVADOS ====================
	
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
	
	/**
	 * Determinar el tipo MIME basado en la extensión
	 */
	private MediaType determinarMediaType(String nombreArchivo) {
		String nombreLowercase = nombreArchivo.toLowerCase();
		
		if (nombreLowercase.endsWith(".jpg") || nombreLowercase.endsWith(".jpeg")) {
			return MediaType.IMAGE_JPEG;
		} else if (nombreLowercase.endsWith(".png")) {
			return MediaType.IMAGE_PNG;
		} else if (nombreLowercase.endsWith(".gif")) {
			return MediaType.IMAGE_GIF;
		} else if (nombreLowercase.endsWith(".webp")) {
			return MediaType.valueOf("image/webp");
		} else if (nombreLowercase.endsWith(".svg")) {
			return MediaType.valueOf("image/svg+xml");
		} else {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}
	
	/**
	 * Formatear tamaño de archivo a formato legible
	 */
	private String formatFileSize(long bytes) {
		if (bytes <= 0) return "0 B";
		final String[] units = new String[]{"B", "KB", "MB", "GB"};
		int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
		return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
	}
}

