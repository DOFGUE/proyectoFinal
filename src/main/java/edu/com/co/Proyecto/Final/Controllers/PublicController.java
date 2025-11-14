package edu.com.co.Proyecto.Final.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
public class PublicController {
	
	@Autowired
	private usuarioService usuarioService;
	
	/**
	 * Página de inicio pública
	 * Ruta: GET /home
	 */
	@GetMapping("/home")
	public String home() {
		return "home";
	}
	
	/**
	 * Página de login
	 * Ruta: GET /login
	 */
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	/**
	 * Página de registro
	 * Ruta: GET /signup
	 */
	@GetMapping("/signup")
	public String signup() {
		return "signup";
	}
	
	/**
	 * Procesar registro de nuevo usuario
	 * Ruta: POST /signup
	 */
	@PostMapping("/signup")
	public String registrarUsuario(
			@RequestParam String nombreUsuario,
			@RequestParam String emailUsuario,
			@RequestParam String contrasenaUsuario,
			@RequestParam String confirmPassword,
			@RequestParam Long numeroTelefonoUsuario,
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
	public String index() {
		return "redirect:/home";
	}

}
