package edu.com.co.Proyecto.Final.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.com.co.Proyecto.Final.Service.usuarioService;

/**
 * Controlador para rutas públicas
 * Gestiona acceso a páginas sin autenticación
 */
@Controller
@Tag(name = "Público", description = "Endpoints públicos - Acceso sin autenticación (login, registro, home)")
public class PublicController {
	
	@Autowired
	private usuarioService usuarioService;
	
	/**
	 * Página de inicio pública
	 * Ruta: GET /home
	 */
	@GetMapping("/home")
	@Operation(
		summary = "Página de inicio pública",
		description = "Muestra la página de inicio. Si el usuario está autenticado, lo redirige a su dashboard correspondiente según su rol"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Página de inicio mostrada"),
		@ApiResponse(responseCode = "302", description = "Usuario autenticado - Redirige a /admin/home o /user/home")
	})
	public String home(@Parameter(hidden = true) Authentication authentication) {
		// Si el usuario ya está autenticado, redirigirlo a su dashboard
		if (authentication != null && authentication.isAuthenticated() 
			&& !authentication.getName().equals("anonymousUser")) {
			
			// Verificar el rol y redirigir al home correspondiente
			if (authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
				return "redirect:/admin/home";
			} else {
				return "redirect:/user/home";
			}
		}
		
		return "home";
	}
	
	/**
	 * Página de login
	 * Ruta: GET /login
	 */
	@GetMapping("/login")
	@Operation(
		summary = "Página de inicio de sesión",
		description = "Muestra el formulario de login. Si el usuario ya está autenticado, lo redirige a su dashboard"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Formulario de login mostrado"),
		@ApiResponse(responseCode = "302", description = "Usuario ya autenticado - Redirige a su dashboard")
	})
	public String login(@Parameter(hidden = true) Authentication authentication) {
		// Si el usuario ya está autenticado, redirigirlo a su dashboard
		if (authentication != null && authentication.isAuthenticated() 
			&& !authentication.getName().equals("anonymousUser")) {
			
			if (authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
				return "redirect:/admin/home";
			} else {
				return "redirect:/user/home";
			}
		}
		
		return "login";
	}
	
	/**
	 * Página de registro
	 * Ruta: GET /signup
	 */
	@GetMapping("/signup")
	@Operation(
		summary = "Página de registro",
		description = "Muestra el formulario de registro de nuevos usuarios. Si el usuario ya está autenticado, lo redirige a su dashboard"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Formulario de registro mostrado"),
		@ApiResponse(responseCode = "302", description = "Usuario ya autenticado - Redirige a su dashboard")
	})
	public String signup(@Parameter(hidden = true) Authentication authentication) {
		// Si el usuario ya está autenticado, redirigirlo a su dashboard
		if (authentication != null && authentication.isAuthenticated() 
			&& !authentication.getName().equals("anonymousUser")) {
			
			if (authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
				return "redirect:/admin/home";
			} else {
				return "redirect:/user/home";
			}
		}
		
		return "signup";
	}
	
	/**
	 * Procesar registro de nuevo usuario
	 * Ruta: POST /signup
	 */
	@PostMapping("/signup")
	@Operation(
		summary = "Registrar nuevo usuario",
		description = "Procesa el registro de un nuevo usuario. Valida username único, formato de email, coincidencia de contraseñas y teléfono. Asigna automáticamente rol USER"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "302",
			description = "Usuario registrado exitosamente - Redirige a /login"
		),
		@ApiResponse(
			responseCode = "200",
			description = "Error de validación - Muestra formulario con errores",
			content = @Content(mediaType = "text/html")
		)
	})
	public String registrarUsuario(
			@Parameter(description = "Nombre de usuario único", required = true, example = "juanperez")
			@RequestParam String nombreUsuario,
			@Parameter(description = "Email válido del usuario", required = true, example = "juan@example.com")
			@RequestParam String emailUsuario,
			@Parameter(description = "Contraseña del usuario", required = true, example = "password123")
			@RequestParam String contrasenaUsuario,
			@Parameter(description = "Confirmación de contraseña (debe coincidir)", required = true, example = "password123")
			@RequestParam String confirmPassword,
			@Parameter(description = "Número de teléfono (mínimo 10 dígitos)", required = true, example = "3001234567")
			@RequestParam Long numeroTelefonoUsuario,
			@Parameter(description = "Descripción opcional del usuario", required = false, example = "Me encanta cocinar")
			@RequestParam(required = false) String descripcionUsuario,
			Model model) {
		
		try {
			// Registrar nuevo usuario usando el servicio
			usuarioService.registrarNuevoUsuario(nombreUsuario, emailUsuario, contrasenaUsuario, 
												  confirmPassword, numeroTelefonoUsuario, descripcionUsuario);
			
			model.addAttribute("success", "Usuario registrado exitosamente. Por favor inicia sesión.");
			return "redirect:/login";
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("nombreUsuario", nombreUsuario);
			model.addAttribute("emailUsuario", emailUsuario);
			model.addAttribute("numeroTelefonoUsuario", numeroTelefonoUsuario);
			model.addAttribute("descripcionUsuario", descripcionUsuario);
			return "signup";
		} catch (Exception e) {
			model.addAttribute("error", "Error al registrar usuario: " + e.getMessage());
			return "signup";
		}
	}
	
	/**
	 * Ruta raíz - redirige a home
	 * Ruta: GET /
	 */
	@GetMapping("/")
	@Operation(
		summary = "Ruta raíz",
		description = "Redirige automáticamente a la página de inicio /home"
	)
	@ApiResponse(responseCode = "302", description = "Redirige a /home")
	public String index() {
		return "redirect:/home";
	}

}
