package edu.com.co.Proyecto.Final.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Usuario", description = "Gestión de perfil de usuario y visualización de productos (requiere rol USER o ADMIN)")
public class UserController {
	
	@Autowired
	private usuarioService usuarioService;
	
	@Autowired
	private resenaService resenaService;
	
	@Autowired
	private productoService productoService;
	
	/**
	 * Obtener el usuario autenticado desde el nombre de usuario o email (OAuth2)
	 */
	private usuario obtenerUsuarioAutenticado(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		
		String identifier = authentication.getName();
		
		// Intentar buscar por nombre de usuario primero
		Optional<usuario> usuarioOpt = usuarioService.obtenerUsuarioPorNombre(identifier);
		
		// Si no se encuentra, intentar buscar por email (para OAuth2)
		if (usuarioOpt.isEmpty()) {
			usuarioOpt = usuarioService.obtenerUsuarioPorEmail(identifier);
		}
		
		return usuarioOpt.orElse(null);
	}
	
	/**
	 * Página de inicio del usuario
	 * Ruta: GET /user/home
	 * Muestra lista de productos disponibles
	 */
	@GetMapping("/home")
	@Operation(
		summary = "Página de inicio del usuario",
		description = "Muestra la página principal con todos los productos disponibles para el usuario autenticado"
	)
	@ApiResponse(responseCode = "200", description = "Página cargada exitosamente")
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
	@Operation(
		summary = "Ver todos los productos",
		description = "Lista todos los productos disponibles en el sistema"
	)
	@ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
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
	@Operation(
		summary = "Ver detalle de producto",
		description = "Muestra los detalles completos de un producto incluyendo sus reseñas"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Detalle del producto cargado"),
		@ApiResponse(responseCode = "302", description = "Producto no encontrado - Redirige a lista de productos")
	})
	public String verProductoDetalle(
		@Parameter(description = "ID del producto", required = true, example = "1")
		@PathVariable Long id, Model model) {
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
	@Operation(
		summary = "Ver perfil del usuario",
		description = "Muestra el perfil completo del usuario autenticado incluyendo sus reseñas"
	)
	@ApiResponse(responseCode = "200", description = "Perfil cargado exitosamente")
	public String userProfile(Model model, @Parameter(hidden = true) Authentication authentication) {
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
	@Operation(
		summary = "Formulario de edición de perfil",
		description = "Muestra el formulario para editar el perfil del usuario autenticado"
	)
	@ApiResponse(responseCode = "200", description = "Formulario cargado exitosamente")
	public String editProfileForm(Model model, @Parameter(hidden = true) Authentication authentication) {
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
	 * Todas las validaciones se ejecutan en el servicio
	 */
	@PostMapping("/profile/edit")
	@Operation(
		summary = "Actualizar perfil de usuario",
		description = "Procesa la actualización del perfil del usuario con validaciones completas de email, teléfono y descripción"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Perfil actualizado exitosamente - Redirige al perfil"),
		@ApiResponse(responseCode = "200", description = "Error de validación - Muestra formulario con errores")
	})
	public String updateProfile(
			@Parameter(description = "Email del usuario", required = true, example = "usuario@example.com")
			@RequestParam String emailUsuario,
			@Parameter(description = "Número de teléfono (mínimo 10 dígitos)", required = true, example = "3001234567")
			@RequestParam Long numeroTelefonoUsuario,
			@Parameter(description = "Descripción del usuario (máximo 500 caracteres)", required = false)
			@RequestParam(required = false) String descripcionUsuario,
			Model model,
			@Parameter(hidden = true) Authentication authentication) {
		
		try {
			usuario usuarioActual = obtenerUsuarioAutenticado(authentication);
			
			if (usuarioActual == null) {
				return "redirect:/login";
			}
			
			// Actualizar perfil usando servicio con validaciones completas
			usuarioService.actualizarPerfilUsuario(
				usuarioActual.getIdUsuario(), 
				emailUsuario, 
				numeroTelefonoUsuario, 
				descripcionUsuario
			);
			
			System.out.println("✓ Perfil actualizado exitosamente para usuario: " + usuarioActual.getNombreUsuario());
			return "redirect:/user/profile?success=profile_updated";
			
		} catch (IllegalArgumentException e) {
			// Errores de validación del servicio
			System.out.println("✗ Error de validación: " + e.getMessage());
			model.addAttribute("error", e.getMessage());
			model.addAttribute("usuario", obtenerUsuarioAutenticado(authentication));
			return "user/editProfile";
		} catch (Exception e) {
			// Errores inesperados
			System.out.println("✗ Error al actualizar perfil: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("error", "Error al actualizar el perfil. Por favor, intenta nuevamente.");
			model.addAttribute("usuario", obtenerUsuarioAutenticado(authentication));
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
	@Operation(
		summary = "Formulario para crear reseña",
		description = "Muestra el formulario para crear una nueva reseña de un producto. Si el usuario ya tiene una reseña para este producto, lo redirige al formulario de edición"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Formulario de nueva reseña mostrado"),
		@ApiResponse(responseCode = "302", description = "Reseña ya existe - Redirige a formulario de edición, o producto no encontrado")
	})
	public String newReviewForm(
		@Parameter(description = "ID del producto a reseñar", required = true, example = "1")
		@PathVariable Long productoId, Model model, @Parameter(hidden = true) Authentication authentication) {
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
	@Operation(
		summary = "Crear nueva reseña",
		description = "Procesa la creación de una nueva reseña para un producto. Valida que el producto exista, que el usuario no tenga ya una reseña para ese producto, y que la calificación esté entre 1 y 5"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Reseña creada exitosamente - Redirige al detalle del producto"),
		@ApiResponse(responseCode = "302", description = "Error de validación - Redirige con mensaje de error")
	})
	public String createReview(
			@Parameter(description = "ID del producto a reseñar", required = true, example = "1")
			@PathVariable Long productoId,
			@Parameter(description = "Calificación del producto (1-5 estrellas)", required = true, example = "5")
			@RequestParam Integer calificacion,
			@Parameter(description = "Comentario opcional sobre el producto", required = false, example = "Excelente producto, muy recomendado")
			@RequestParam(required = false) String comentarioResena,
			Model model,
			@Parameter(hidden = true) Authentication authentication) {
		
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
	@Operation(
		summary = "Formulario para editar reseña",
		description = "Muestra el formulario para editar una reseña existente. Solo el propietario de la reseña puede acceder"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Formulario de edición mostrado"),
		@ApiResponse(responseCode = "302", description = "Reseña no encontrada o usuario no autorizado - Redirige al perfil")
	})
	public String editReviewForm(
		@Parameter(description = "ID de la reseña a editar", required = true, example = "1")
		@PathVariable Long resenaId, Model model, @Parameter(hidden = true) Authentication authentication) {
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
	@Operation(
		summary = "Actualizar reseña existente",
		description = "Procesa la actualización de una reseña. Valida que el usuario sea el propietario, que la reseña exista y que la calificación esté entre 1 y 5"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Reseña actualizada exitosamente - Redirige al detalle del producto"),
		@ApiResponse(responseCode = "302", description = "Error de validación o no autorizado - Redirige con mensaje de error")
	})
	public String updateReview(
			@Parameter(description = "ID de la reseña a actualizar", required = true, example = "1")
			@PathVariable Long resenaId,
			@Parameter(description = "Nueva calificación del producto (1-5 estrellas)", required = true, example = "4")
			@RequestParam Integer calificacion,
			@Parameter(description = "Nuevo comentario opcional sobre el producto", required = false, example = "Actualicé mi opinión, sigue siendo muy bueno")
			@RequestParam(required = false) String comentarioResena,
			Model model,
			@Parameter(hidden = true) Authentication authentication) {
		
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
	@Operation(
		summary = "Eliminar reseña",
		description = "Elimina una reseña existente. Solo el propietario de la reseña puede eliminarla"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Reseña eliminada exitosamente - Redirige al perfil de usuario"),
		@ApiResponse(responseCode = "302", description = "Reseña no encontrada o usuario no autorizado - Redirige con mensaje de error")
	})
	public String deleteReview(
		@Parameter(description = "ID de la reseña a eliminar", required = true, example = "1")
		@PathVariable Long resenaId, @Parameter(hidden = true) Authentication authentication) {
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
	@Operation(
		summary = "Ver detalle de producto con reseñas",
		description = "Muestra el detalle completo de un producto incluyendo todas sus reseñas y el total de reseñas"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Detalle del producto con reseñas cargado exitosamente"),
		@ApiResponse(responseCode = "302", description = "Producto no encontrado - Redirige a la página principal")
	})
	public String viewProductDetail(
		@Parameter(description = "ID del producto a visualizar", required = true, example = "1")
		@PathVariable Long productoId, Model model) {
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
	@Operation(
		summary = "Buscar productos",
		description = "Busca productos por nombre, descripción o ingredientes. Si no se proporciona término de búsqueda, muestra todos los productos"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos exitosamente")
	})
	public String buscarProductos(
			@Parameter(description = "Término de búsqueda (nombre, descripción o ingredientes)", required = false, example = "chocolate")
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
	@Operation(
		summary = "Generar link de WhatsApp para pedido",
		description = "Genera un link de WhatsApp con mensaje predefinido para realizar un pedido del producto. Retorna JSON con la URL de WhatsApp"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Link de WhatsApp generado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Producto no encontrado")
	})
	public ResponseEntity<?> generarLinkWhatsApp(
		@Parameter(description = "ID del producto para ordenar", required = true, example = "1")
		@PathVariable Long productoId) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(productoId);
			
			if (productoOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Producto no encontrado"));
			}
			
			var producto = productoOpt.get();
			
			// Número de WhatsApp centralizado (puede venir de configuración o BD)
			String numeroWhatsApp = "573174865490";
			
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
