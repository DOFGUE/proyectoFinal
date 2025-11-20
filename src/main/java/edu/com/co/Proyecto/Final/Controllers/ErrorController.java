package edu.com.co.Proyecto.Final.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para manejar errores de acceso denegado
 */
@Controller
@Tag(name = "Manejo de Errores", description = "Endpoints para gestionar errores de acceso y autenticación")
public class ErrorController {
	
	@GetMapping("/access-denied")
	@Operation(
		summary = "Página de acceso denegado",
		description = "Muestra la página de error cuando un usuario intenta acceder a un recurso para el cual no tiene permisos"
	)
	@ApiResponse(responseCode = "200", description = "Página de acceso denegado mostrada")
	public String accessDenied(Model model, @Parameter(hidden = true) Authentication authentication) {
		boolean isAuthenticated = authentication != null && authentication.isAuthenticated() 
			&& !authentication.getName().equals("anonymousUser");
		
		String userRole = null;
		if (isAuthenticated) {
			userRole = authentication.getAuthorities().stream()
				.map(auth -> auth.getAuthority())
				.findFirst()
				.orElse("ROLE_USER");
		}
		
		model.addAttribute("isAuthenticated", isAuthenticated);
		model.addAttribute("userRole", userRole);
		
		return "access-denied";
	}
}
