package edu.com.co.Proyecto.Final.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
	public String verProductoDetalle(@PathVariable Long id, Model model) {
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
	public String createRecipe(
			@RequestParam String nombreProducto,
			@RequestParam Double precioProducto,
			@RequestParam String rutaImagenProducto,
			@RequestParam String descripcionProducto,
			@RequestParam(required = false) String ingredientesProducto,
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
	public String updateRecipeForm(@PathVariable Long id, Model model) {
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
	public String updateRecipe(
			@PathVariable Long id,
			@RequestParam String nombreProducto,
			@RequestParam Double precioProducto,
			@RequestParam String rutaImagenProducto,
			@RequestParam String descripcionProducto,
			@RequestParam(required = false) String ingredientesProducto,
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
	public String deleteRecipe(@PathVariable Long id) {
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
	public String viewRecipe(@PathVariable Long id, Model model) {
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
