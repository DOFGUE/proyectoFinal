package edu.com.co.Proyecto.Final.Controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

/**
 * Controlador para servir imágenes desde la carpeta static/imagenes/
 */
@RestController
@RequestMapping("/imagenes")
public class ImageController {
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	/**
	 * Obtener imagen por nombre
	 * Ruta: GET /imagenes/{nombreArchivo}
	 * Intenta buscar la imagen en múltiples ubicaciones
	 */
	@GetMapping("/{nombreArchivo}")
	public ResponseEntity<Resource> obtenerImagen(@PathVariable String nombreArchivo) {
		try {
			System.out.println("=== ImageController ===");
			System.out.println("Buscando imagen: " + nombreArchivo);
			
			// Validar que el nombre del archivo sea seguro (sin caracteres peligrosos)
			if (nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
				System.out.println("ERROR: Nombre de archivo inválido");
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
				System.out.println("Intentando ruta: " + ruta);
				Resource temp = resourceLoader.getResource(ruta);
				
				if (temp.exists() && temp.isReadable()) {
					resource = temp;
					rutaExitosa = ruta;
					System.out.println("✓ Recurso encontrado en: " + ruta);
					break;
				}
			}
			
			// Verificar que el archivo existe y es legible
			if (resource == null || !resource.exists() || !resource.isReadable()) {
				System.out.println("ERROR: Imagen no encontrada en ninguna ubicación: " + nombreArchivo);
				return ResponseEntity.notFound().build();
			}
			
			// Determinar el tipo de contenido
			MediaType mediaType = determinarMediaType(nombreArchivo);
			System.out.println("Tipo MIME: " + mediaType);
			
			// Retornar la imagen
			System.out.println("Retornando imagen exitosamente desde: " + rutaExitosa);
			return ResponseEntity.ok()
					.contentType(mediaType)
					.header("Cache-Control", "public, max-age=3600")
					.body(resource);
					
		} catch (Exception e) {
			System.out.println("ERROR en ImageController: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
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
}

