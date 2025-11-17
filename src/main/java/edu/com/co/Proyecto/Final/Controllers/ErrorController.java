package edu.com.co.Proyecto.Final.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para manejar errores de acceso denegado
 */
@Controller
public class ErrorController {
	
	@GetMapping("/access-denied")
	public String accessDenied(Model model, Authentication authentication) {
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
