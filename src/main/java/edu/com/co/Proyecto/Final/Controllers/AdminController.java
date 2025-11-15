package edu.com.co.Proyecto.Final.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import edu.com.co.Proyecto.Final.Model.producto;
import edu.com.co.Proyecto.Final.Service.productoService;
import edu.com.co.Proyecto.Final.Service.resenaService;

/**
 * Controlador para rutas administrativas
 * Requiere rol ADMIN
 * Gestiona el panel de control y creación/edición de recetas (productos)
 */
@Controller
@RequestMapping("/admin")
@Tag(name = "Administrador", description = "API para gestión administrativa - Panel de control, productos y reseñas")
public class AdminController {
	
	@Autowired
	private productoService productoService;
	
	@Autowired
	private resenaService resenaService;
	
	/**
	 * Panel de control administrativo
	 * Ruta: GET /admin/panel
	 * Muestra información general del sistema y gestión de productos
	 */
	@GetMapping("/panel")
	@Operation(summary = "Panel de control administrativo", description = "Muestra el panel principal del administrador con estadísticas y gestión de productos")
	@ApiResponse(responseCode = "200", description = "Panel cargado exitosamente")
	public String controlPanel(Model model) {
		try {
			// Obtener estadísticas del sistema
			long totalProductos = productoService.obtenerTodosProductos().size();
			long totalResenas = resenaService.obtenerTodasResenas().size();
			
			model.addAttribute("totalProductos", totalProductos);
			model.addAttribute("totalResenas", totalResenas);
			
			// Obtener lista de productos para mostrar
			model.addAttribute("productos", productoService.obtenerTodosProductos());
			
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar el panel: " + e.getMessage());
		}
		
		return "admin/controlPanel";
	}
	
	/**
	 * Página de inicio para admin - Tienda normal (como usuario)
	 * Ruta: GET /admin/home
	 * Muestra lista de productos disponibles
	 */
	@GetMapping("/home")
	@Operation(summary = "Página de inicio para administrador", description = "Muestra el inicio del administrador con la lista de todos los productos disponibles")
	@ApiResponse(responseCode = "200", description = "Página cargada exitosamente")
	public String adminHome(Model model) {
		try {
			// Obtener todos los productos para mostrar
			model.addAttribute("productos", productoService.obtenerTodosProductos());
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
		}
		
		return "admin/home";
	}
	
	/**
	 * Ver detalle de un producto (como usuario)
	 * Ruta: GET /admin/producto/{id}
	 */
	@GetMapping("/producto/{id}")
	@Operation(summary = "Detalle de producto para administrador", description = "Muestra los detalles completos de un producto incluyendo sus reseñas")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Detalle obtenido exitosamente"),
		@ApiResponse(responseCode = "404", description = "Producto no encontrado")
	})
	public String verProductoDetalle(@PathVariable @Parameter(description = "ID del producto") Long id, Model model) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(id);
			
			if (productoOpt.isEmpty()) {
				return "redirect:/admin/home?error=product_not_found";
			}
			
			model.addAttribute("producto", productoOpt.get());
			// Obtener reseñas del producto
			model.addAttribute("resenas", resenaService.obtenerResenasPorProducto(id));
			return "admin/productoDetalle";
			
		} catch (Exception e) {
			return "redirect:/admin/home?error=error_loading_product";
		}
	}
	
	// ==================== GESTIÓN DE RECETAS/PRODUCTOS ====================
	
	/**
	 * Mostrar formulario para crear nueva receta
	 * Ruta: GET /admin/recipes/new
	 */
	@GetMapping("/recipes/new")
	@Operation(summary = "Formulario de creación de producto", description = "Muestra el formulario para crear un nuevo producto/receta")
	@ApiResponse(responseCode = "200", description = "Formulario cargado exitosamente")
	public String newRecipeForm() {
		return "admin/newRecipeForm";
	}
	
	/**
	 * Procesar creación de nueva receta
	 * Ruta: POST /admin/recipes/new
	 * 
	 * @param nombreProducto Nombre de la receta
	 * @param precioProducto Precio de la receta
	 * @param rutaImagenProducto Ruta de la imagen
	 * @param descripcionProducto Descripción de la receta
	 * @param ingredientesProducto Ingredientes de la receta
	 * @return Redirige al panel de control
	 */
	@PostMapping("/recipes/new")
	@Operation(summary = "Crear nuevo producto", description = "Crea un nuevo producto/receta en el sistema")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Redirección después de creación exitosa"),
		@ApiResponse(responseCode = "400", description = "Error de validación en los datos del producto")
	})
	public String createRecipe(
			@RequestParam @Parameter(description = "Nombre del producto") String nombreProducto,
			@RequestParam @Parameter(description = "Precio del producto") Double precioProducto,
			@RequestParam @Parameter(description = "Ruta de la imagen del producto") String rutaImagenProducto,
			@RequestParam @Parameter(description = "Descripción del producto") String descripcionProducto,
			@RequestParam(required = false) @Parameter(description = "Ingredientes del producto") String ingredientesProducto,
			Model model) {
		
		try {
			// Crear nuevo producto usando el servicio
			producto nuevoProducto = new producto();
			nuevoProducto.setNombreProducto(nombreProducto);
			nuevoProducto.setPrecioProducto(precioProducto);
			nuevoProducto.setRutaImagenProducto(rutaImagenProducto);
			nuevoProducto.setDescripcionProducto(descripcionProducto);
			nuevoProducto.setIngredientesProducto(ingredientesProducto);
			nuevoProducto.setCalificacionProducto(0.0);
			
			// Guardar usando el servicio (hace todas las validaciones)
			productoService.crearProducto(nuevoProducto);
			
			return "redirect:/admin/panel?success=recipe_created";
			
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "admin/newRecipeForm";
		} catch (Exception e) {
			model.addAttribute("error", "Error al crear la receta: " + e.getMessage());
			return "admin/newRecipeForm";
		}
	}
	
	/**
	 * Mostrar formulario para editar receta existente
	 * Ruta: GET /admin/recipes/update/{id}
	 */
	@GetMapping("/recipes/update/{id}")
	@Operation(summary = "Formulario de edición de producto", description = "Muestra el formulario para editar un producto existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Formulario cargado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Producto no encontrado")
	})
	public String updateRecipeForm(@PathVariable @Parameter(description = "ID del producto") Long id, Model model) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(id);
			
			if (productoOpt.isEmpty()) {
				return "redirect:/admin/?error=recipe_not_found";
			}
			
			model.addAttribute("producto", productoOpt.get());
			return "admin/updateRecipeForm";
			
		} catch (Exception e) {
			return "redirect:/admin/?error=error_loading_recipe";
		}
	}
	
	/**
	 * Procesar actualización de receta existente
	 * Ruta: POST /admin/recipes/update/{id}
	 */
	@PostMapping("/recipes/update/{id}")
	@Operation(summary = "Actualizar producto", description = "Actualiza la información de un producto existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Redirección después de actualización exitosa"),
		@ApiResponse(responseCode = "400", description = "Error de validación en los datos"),
		@ApiResponse(responseCode = "404", description = "Producto no encontrado")
	})
	public String updateRecipe(
			@PathVariable @Parameter(description = "ID del producto") Long id,
			@RequestParam @Parameter(description = "Nombre del producto") String nombreProducto,
			@RequestParam @Parameter(description = "Precio del producto") Double precioProducto,
			@RequestParam @Parameter(description = "Ruta de la imagen del producto") String rutaImagenProducto,
			@RequestParam @Parameter(description = "Descripción del producto") String descripcionProducto,
			@RequestParam(required = false) @Parameter(description = "Ingredientes del producto") String ingredientesProducto,
			Model model) {
		
		try {
			// Crear objeto con los datos actualizados
			producto productoActualizado = new producto();
			productoActualizado.setNombreProducto(nombreProducto);
			productoActualizado.setPrecioProducto(precioProducto);
			productoActualizado.setRutaImagenProducto(rutaImagenProducto);
			productoActualizado.setDescripcionProducto(descripcionProducto);
			productoActualizado.setIngredientesProducto(ingredientesProducto);
			
			// Actualizar usando el servicio
			productoService.actualizarProducto(id, productoActualizado);
			
			return "redirect:/admin/panel?success=recipe_updated";
			
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "admin/updateRecipeForm";
		} catch (Exception e) {
			model.addAttribute("error", "Error al actualizar la receta: " + e.getMessage());
			return "admin/updateRecipeForm";
		}
	}
	
	/**
	 * Eliminar una receta
	 * Ruta: POST /admin/recipes/delete/{id}
	 */
	@PostMapping("/recipes/delete/{id}")
	@Operation(summary = "Eliminar producto", description = "Elimina un producto del sistema")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Redirección después de eliminación exitosa"),
		@ApiResponse(responseCode = "404", description = "Producto no encontrado")
	})
	public String deleteRecipe(@PathVariable @Parameter(description = "ID del producto") Long id) {
		try {
			// Eliminar usando el servicio
			productoService.eliminarProducto(id);
			return "redirect:/admin/panel?success=recipe_deleted";
			
		} catch (IllegalArgumentException e) {
			return "redirect:/admin/panel?error=recipe_not_found";
		} catch (Exception e) {
			return "redirect:/admin/panel?error=delete_failed";
		}
	}
	
	/**
	 * Visualizar detalle de una receta
	 * Ruta: GET /admin/recipes/{id}
	 */
	@GetMapping("/recipes/{id}")
	@Operation(summary = "Ver detalle de producto", description = "Muestra los detalles completos de un producto incluyendo todas sus reseñas")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Detalle obtenido exitosamente"),
		@ApiResponse(responseCode = "404", description = "Producto no encontrado")
	})
	public String viewRecipe(@PathVariable @Parameter(description = "ID del producto") Long id, Model model) {
		try {
			var productoOpt = productoService.obtenerProductoPorId(id);
			
			if (productoOpt.isEmpty()) {
				return "redirect:/admin/panel?error=recipe_not_found";
			}
			
			producto producto = productoOpt.get();
			model.addAttribute("producto", producto);
			
			// Obtener reseñas del producto usando el servicio
			model.addAttribute("resenas", resenaService.obtenerResenasPorProducto(id));
			
			return "admin/recipeDetail";
			
		} catch (Exception e) {
			return "redirect:/admin/panel?error=error_loading_recipe";
		}
	}
	
	/**
	 * Buscar productos por nombre, descripción o ingredientes
	 * Ruta: GET /admin/buscar
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
					System.out.println("✓ Búsqueda Admin encontró " + resultados.size() + " productos para: " + q);
				} else {
					System.out.println("⚠ Búsqueda Admin sin resultados para: " + q);
				}
			}
		} catch (Exception e) {
			model.addAttribute("error", "Error al buscar: " + e.getMessage());
			model.addAttribute("productos", productoService.obtenerTodosProductos());
		}
		
		return "admin/buscar";
	}
}
