package edu.com.co.Proyecto.Final.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Service.usuarioService;
import edu.com.co.Proyecto.Final.Service.resenaService;
import edu.com.co.Proyecto.Final.Service.productoService;

import java.util.Optional;

/**
 * Controlador para rutas de usuario
 * Requiere rol USER o ADMIN
 * Gestiona perfil, reseñas y visualización de productos
 */
@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private usuarioService usuarioService;
	
	@Autowired
	private resenaService resenaService;
	
	@Autowired
	private productoService productoService;
	
	/**
	 * Obtener el usuario autenticado desde el nombre de usuario
	 */
	private usuario obtenerUsuarioAutenticado(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		
		String username = authentication.getName();
		Optional<usuario> usuarioOpt = usuarioService.obtenerUsuarioPorNombre(username);
		return usuarioOpt.orElse(null);
	}
	
	/**
	 * Página de inicio del usuario
	 * Ruta: GET /user/home
	 * Muestra lista de productos disponibles
	 */
	@GetMapping("/home")
	public String userHome(Model model) {
		try {
			// Obtener todos los productos para mostrar
			model.addAttribute("productos", productoService.obtenerTodosProductos());
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
		}
		
		return "user/home";
	}
	
	/**
	 * Ver todos los productos disponibles
	 * Ruta: GET /user/productos
	 */
	@GetMapping("/productos")
	public String verProductos(Model model) {
		try {
			model.addAttribute("productos", productoService.obtenerTodosProductos());
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
		}
		
		return "user/productos";
	}
	
	/**
	 * Ver detalle de un producto específico
	 * Ruta: GET /user/producto/{id}
	 */
	@GetMapping("/producto/{id}")
	public String verProductoDetalle(@PathVariable Long id, Model model) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(id);
			
			if (productoOpt.isEmpty()) {
				return "redirect:/user/productos?error=product_not_found";
			}
			
			model.addAttribute("producto", productoOpt.get());
			// Obtener reseñas del producto
			model.addAttribute("resenas", resenaService.obtenerResenasPorProducto(id));
			return "user/productoDetalle";
			
		} catch (Exception e) {
			return "redirect:/user/productos?error=error_loading_product";
		}
	}
	
	// ==================== GESTIÓN DE PERFIL ====================
	
	/**
	 * Visualizar perfil del usuario autenticado
	 * Ruta: GET /user/profile
	 */
	@GetMapping("/profile")
	public String userProfile(Model model, Authentication authentication) {
		try {
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual != null) {
				model.addAttribute("usuario", usuarioActual);
				
				// Obtener reseñas del usuario usando el servicio
				var resenas = resenaService.obtenerResenasPorUsuario(usuarioActual.getIdUsuario());
				model.addAttribute("resenas", resenas);
				model.addAttribute("totalResenas", resenas.size());
			}
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
		}
		
		return "user/profile";
	}
	
	/**
	 * Mostrar formulario para actualizar perfil
	 * Ruta: GET /user/profile/edit
	 */
	@GetMapping("/profile/edit")
	public String editProfileForm(Model model, Authentication authentication) {
		try {
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual != null) {
				model.addAttribute("usuario", usuarioActual);
			}
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
		}
		
		return "user/editProfile";
	}
	
	/**
	 * Procesar actualización de perfil
	 * Ruta: POST /user/profile/edit
	 */
	@PostMapping("/profile/edit")
	public String updateProfile(
			@RequestParam String emailUsuario,
			@RequestParam Long numeroTelefonoUsuario,
			@RequestParam(required = false) String descripcionUsuario,
			Model model,
			Authentication authentication) {
		
		try {
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual == null) {
				return "redirect:/login";
			}
			
			// Crear objeto con los datos actualizados
			usuario usuarioActualizado = new usuario();
			usuarioActualizado.setEmailUsuario(emailUsuario);
			usuarioActualizado.setNumeroTelefonoUsuario(numeroTelefonoUsuario);
			usuarioActualizado.setDescripcionUsuario(descripcionUsuario);
			
			// Actualizar usando el servicio
			usuarioService.actualizarUsuario(usuarioActual.getIdUsuario(), usuarioActualizado);
			
			return "redirect:/user/profile?success=profile_updated";
			
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "user/editProfile";
		} catch (Exception e) {
			model.addAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
			return "user/editProfile";
		}
	}
	
	// ==================== GESTIÓN DE RESEÑAS ====================
	
	/**
	 * Mostrar formulario para crear nueva reseña
	 * Si el usuario ya tiene una reseña, redirige al formulario de edición
	 * Ruta: GET /user/reviews/new/{productoId}
	 */
	@GetMapping("/reviews/new/{productoId}")
	public String newReviewForm(@PathVariable Long productoId, Model model, Authentication authentication) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(productoId);
			
			if (productoOpt.isEmpty()) {
				return "redirect:/user/home?error=product_not_found";
			}
			
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual == null) {
				return "redirect:/login";
			}
			
			// Verificar si el usuario ya tiene una reseña en este producto
			var resenaExistente = resenaService.obtenerResenaUsuarioProducto(usuarioActual.getIdUsuario(), productoId);
			
			if (resenaExistente.isPresent()) {
				// Si ya existe reseña, redirigir al formulario de edición
				return "redirect:/user/reviews/edit/" + resenaExistente.get().getIdResena();
			}
			
			model.addAttribute("producto", productoOpt.get());
			model.addAttribute("modo", "crear");
			return "user/newReviewForm";
			
		} catch (Exception e) {
			return "redirect:/user/home?error=error_loading_product";
		}
	}
	
	/**
	 * Procesar creación de nueva reseña
	 * Ruta: POST /user/reviews/new/{productoId}
	 */
	@PostMapping("/reviews/new/{productoId}")
	public String createReview(
			@PathVariable Long productoId,
			@RequestParam Integer calificacion,
			@RequestParam(required = false) String comentarioResena,
			Model model,
			Authentication authentication) {
		
		try {
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual == null) {
				return "redirect:/login";
			}
			
			// Verificar que el producto existe
			if (productoService.obtenerProductoPorId(productoId).isEmpty()) {
				return "redirect:/user/home?error=product_not_found";
			}
			
			// Verificar si ya existe una reseña del usuario para este producto
			var resenaExistente = resenaService.obtenerResenaUsuarioProducto(usuarioActual.getIdUsuario(), productoId);
			if (resenaExistente.isPresent()) {
				// Redirigir al formulario de edición
				return "redirect:/user/reviews/edit/" + resenaExistente.get().getIdResena() + "?info=review_exists";
			}
			
			// Crear reseña usando el servicio (valida todo)
			resenaService.crearResena(usuarioActual.getIdUsuario(), productoId, calificacion, comentarioResena);
			
			System.out.println("✓ Reseña creada exitosamente para producto " + productoId);
			return "redirect:/user/producto/" + productoId + "?success=review_created";
			
		} catch (IllegalArgumentException e) {
			System.out.println("✗ Error de validación al crear reseña: " + e.getMessage());
			return "redirect:/user/reviews/new/" + productoId + "?error=" + e.getMessage();
		} catch (Exception e) {
			System.out.println("✗ Error al crear reseña: " + e.getMessage());
			e.printStackTrace();
			return "redirect:/user/reviews/new/" + productoId + "?error=creation_failed";
		}
	}
	
	/**
	 * Mostrar formulario para editar reseña existente
	 * Ruta: GET /user/reviews/edit/{resenaId}
	 */
	@GetMapping("/reviews/edit/{resenaId}")
	public String editReviewForm(@PathVariable Long resenaId, Model model, Authentication authentication) {
		try {
			var resenaOpt = resenaService.obtenerResenaPorId(resenaId);
			
			if (resenaOpt.isEmpty()) {
				return "redirect:/user/profile?error=review_not_found";
			}
			
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual == null) {
				return "redirect:/login";
			}
			
			// Verificar que el usuario sea el propietario de la reseña
			if (!usuarioActual.getIdUsuario().equals(resenaOpt.get().getUsuario().getIdUsuario())) {
				return "redirect:/user/profile?error=unauthorized";
			}
			
			model.addAttribute("resena", resenaOpt.get());
			model.addAttribute("producto", resenaOpt.get().getProducto());
			
			return "user/editReviewForm";
			
		} catch (Exception e) {
			return "redirect:/user/profile?error=error_loading_review";
		}
	}
	
	/**
	 * Procesar actualización de reseña
	 * Ruta: POST /user/reviews/edit/{resenaId}
	 */
	@PostMapping("/reviews/edit/{resenaId}")
	public String updateReview(
			@PathVariable Long resenaId,
			@RequestParam Integer calificacion,
			@RequestParam(required = false) String comentarioResena,
			Model model,
			Authentication authentication) {
		
		try {
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual == null) {
				return "redirect:/login";
			}
			
			var resenaOpt = resenaService.obtenerResenaPorId(resenaId);
			
			if (resenaOpt.isEmpty()) {
				return "redirect:/user/profile?error=review_not_found";
			}
			
			// Verificar que el usuario sea el propietario
			if (!usuarioActual.getIdUsuario().equals(resenaOpt.get().getUsuario().getIdUsuario())) {
				return "redirect:/user/profile?error=unauthorized";
			}
			
			Long productoId = resenaOpt.get().getProducto().getIdProducto();
			
			// Actualizar reseña usando el servicio
			resenaService.actualizarResena(resenaId, calificacion, comentarioResena);
			
			return "redirect:/user/producto/" + productoId + "?success=review_updated";
			
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "redirect:/user/reviews/edit/" + resenaId + "?error=validation_failed";
		} catch (Exception e) {
			model.addAttribute("error", "Error al actualizar la reseña: " + e.getMessage());
			return "redirect:/user/reviews/edit/" + resenaId + "?error=update_failed";
			
		}
	}
	
	/**
	 * Eliminar una reseña
	 * Ruta: POST /user/reviews/delete/{resenaId}
	 */
	@PostMapping("/reviews/delete/{resenaId}")
	public String deleteReview(@PathVariable Long resenaId, Authentication authentication) {
		try {
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual == null) {
				return "redirect:/login";
			}
			
			var resenaOpt = resenaService.obtenerResenaPorId(resenaId);
			
			if (resenaOpt.isEmpty()) {
				return "redirect:/user/profile?error=review_not_found";
			}
			
			// Verificar que el usuario sea el propietario
			if (!usuarioActual.getIdUsuario().equals(resenaOpt.get().getUsuario().getIdUsuario())) {
				return "redirect:/user/profile?error=unauthorized";
			}
			
			// Eliminar reseña usando el servicio
			resenaService.eliminarResena(resenaId);
			
			return "redirect:/user/profile?success=review_deleted";
			
		} catch (Exception e) {
			return "redirect:/user/profile?error=delete_failed";
		}
	}
	
	/**
	 * Visualizar detalle de un producto
	 * Ruta: GET /user/products/{productoId}
	 */
	@GetMapping("/products/{productoId}")
	public String viewProductDetail(@PathVariable Long productoId, Model model) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(productoId);
			
			if (productoOpt.isEmpty()) {
				return "redirect:/user/home?error=product_not_found";
			}
			
			model.addAttribute("producto", productoOpt.get());
			
			// Obtener reseñas del producto usando el servicio
			var resenas = resenaService.obtenerResenasPorProducto(productoId);
			model.addAttribute("resenas", resenas);
			model.addAttribute("totalResenas", resenas.size());
			
			return "user/productDetail";
			
		} catch (Exception e) {
			return "redirect:/user/home?error=error_loading_product";
		}
	}
	
	/**
	 * Buscar productos por nombre, descripción o ingredientes
	 * Ruta: GET /user/buscar
	 * 
	 * @param query Término de búsqueda
	 * @return Página con resultados de búsqueda
	 */
	@GetMapping("/buscar")
	public String buscarProductos(
			@RequestParam(required = false, defaultValue = "") String q,
			Model model) {
		try {
			if (q == null || q.trim().isEmpty()) {
				model.addAttribute("productos", productoService.obtenerTodosProductos());
				model.addAttribute("busqueda", "");
				model.addAttribute("sinResultados", false);
			} else {
				var resultados = productoService.buscarProductos(q.trim());
				model.addAttribute("productos", resultados);
				model.addAttribute("busqueda", q.trim());
				model.addAttribute("sinResultados", resultados.isEmpty());
				
				if (!resultados.isEmpty()) {
					System.out.println("✓ Búsqueda encontró " + resultados.size() + " productos para: " + q);
				} else {
					System.out.println("⚠ Búsqueda sin resultados para: " + q);
				}
			}
		} catch (Exception e) {
			model.addAttribute("error", "Error al buscar: " + e.getMessage());
			model.addAttribute("productos", productoService.obtenerTodosProductos());
		}
		
		return "user/buscar";
	}
	
	/**
	 * Generar link de WhatsApp para ordenar un producto
	 * Ruta: GET /user/api/whatsapp-link/{productoId}
	 * Retorna un JSON con el link de WhatsApp
	 * Esta lógica se mueve del frontend al backend para mayor seguridad
	 */
	@GetMapping("/api/whatsapp-link/{productoId}")
	@ResponseBody
	public ResponseEntity<?> generarLinkWhatsApp(@PathVariable Long productoId) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(productoId);
			
			if (productoOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Producto no encontrado"));
			}
			
			var producto = productoOpt.get();
			
			// Número de WhatsApp centralizado (puede venir de configuración o BD)
			String numeroWhatsApp = "573160423358";
			
			// Construir mensaje
			String nombreProducto = producto.getNombreProducto();
			String precioProducto = String.format("%.2f", producto.getPrecioProducto());
			String mensaje = String.format("Hola! Me gustaría ordenar: %s. Precio: $%s", 
				nombreProducto, precioProducto);
			
			// Generar URL de WhatsApp
			String urlWhatsApp = String.format("https://wa.me/%s?text=%s", 
				numeroWhatsApp, 
				java.net.URLEncoder.encode(mensaje, "UTF-8"));
			
			// Retornar JSON con el link
			Map<String, String> response = new HashMap<>();
			response.put("whatsappLink", urlWhatsApp);
			response.put("mensaje", mensaje);
			response.put("producto", nombreProducto);
			response.put("precio", precioProducto);
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Error al generar el link: " + e.getMessage()));
		}
	}
}
