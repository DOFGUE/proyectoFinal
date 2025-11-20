package edu.com.co.Proyecto.Final.Controllers;

import edu.com.co.Proyecto.Final.Model.JwtResponse;
import edu.com.co.Proyecto.Final.Model.LoginRequest;
import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Security.JwtUtil;
import edu.com.co.Proyecto.Final.Service.usuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para autenticación JWT
 * Endpoints para login y obtención de tokens JWT
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private usuarioService usuarioService;
	
	/**
	 * Endpoint para iniciar sesión y obtener un token JWT
	 * POST /api/auth/login
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		try {
			// Autenticar al usuario
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginRequest.getUsername(),
					loginRequest.getPassword()
				)
			);
			
			// Si la autenticación es exitosa, generar el token
			UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
			String jwt = jwtUtil.generateToken(userDetails);
			
			// Obtener los roles del usuario
			String roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
			
			// Crear la respuesta
			JwtResponse response = new JwtResponse(jwt, userDetails.getUsername(), roles);
			
			return ResponseEntity.ok(response);
			
		} catch (BadCredentialsException e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", "Credenciales inválidas");
			error.put("message", "Usuario o contraseña incorrectos");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		} catch (Exception e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", "Error de autenticación");
			error.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}
	
	/**
	 * Endpoint para validar un token JWT
	 * GET /api/auth/validate
	 */
	@GetMapping("/validate")
	public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
		try {
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				String username = jwtUtil.extractUsername(token);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				
				if (jwtUtil.validateToken(token, userDetails)) {
					Map<String, Object> response = new HashMap<>();
					response.put("valid", true);
					response.put("username", username);
					response.put("roles", jwtUtil.extractRoles(token));
					return ResponseEntity.ok(response);
				}
			}
			
			Map<String, Object> response = new HashMap<>();
			response.put("valid", false);
			response.put("message", "Token inválido");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("valid", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}
	
	/**
	 * Endpoint para obtener información del usuario autenticado
	 * GET /api/auth/me
	 */
	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()) {
			String username = authentication.getName();
			usuario user = usuarioService.buscarPorNombreUsuario(username);
			
			if (user != null) {
				Map<String, Object> userInfo = new HashMap<>();
				userInfo.put("id", user.getIdUsuario());
				userInfo.put("username", user.getNombreUsuario());
				userInfo.put("email", user.getEmailUsuario());
				userInfo.put("telefono", user.getNumeroTelefonoUsuario());
				userInfo.put("descripcion", user.getDescripcionUsuario());
				userInfo.put("rol", user.getRol().getNombreRol());
				
				return ResponseEntity.ok(userInfo);
			}
		}
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
	}
}
