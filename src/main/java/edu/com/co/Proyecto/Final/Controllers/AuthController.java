package edu.com.co.Proyecto.Final.Controllers;

import edu.com.co.Proyecto.Final.Model.JwtResponse;
import edu.com.co.Proyecto.Final.Model.LoginRequest;
import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Security.JwtUtil;
import edu.com.co.Proyecto.Final.Service.usuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticación JWT", description = "Endpoints para autenticación con JSON Web Tokens (JWT)")
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
	@Operation(
		summary = "Iniciar sesión con JWT",
		description = "Autentica un usuario con username y password, retornando un token JWT válido por 24 horas"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Autenticación exitosa - Token JWT generado",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = JwtResponse.class),
				examples = @ExampleObject(
					value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfVVNFUiIsInN1YiI6InVzZXIxIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.signature\",\"type\":\"Bearer\",\"username\":\"user1\",\"roles\":\"ROLE_USER\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "Credenciales inválidas - Usuario o contraseña incorrectos",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "{\"error\":\"Credenciales inválidas\",\"message\":\"Usuario o contraseña incorrectos\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "500",
			description = "Error interno del servidor",
			content = @Content(mediaType = "application/json")
		)
	})
	public ResponseEntity<?> login(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Credenciales de acceso del usuario",
			required = true,
			content = @Content(
				schema = @Schema(implementation = LoginRequest.class),
				examples = @ExampleObject(
					value = "{\"username\":\"admin\",\"password\":\"password123\"}"
				)
			)
		)
		@RequestBody LoginRequest loginRequest) {
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
	@Operation(
		summary = "Validar token JWT",
		description = "Verifica si un token JWT es válido y retorna información del usuario"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Token válido",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "{\"valid\":true,\"username\":\"admin\",\"roles\":\"ROLE_ADMIN\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "Token inválido o expirado",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "{\"valid\":false,\"message\":\"Token inválido\"}"
				)
			)
		)
	})
	public ResponseEntity<?> validateToken(
		@Parameter(
			description = "Token JWT en formato: Bearer <token>",
			required = true,
			example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
		)
		@RequestHeader("Authorization") String authHeader) {
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
	@Operation(
		summary = "Obtener perfil del usuario autenticado",
		description = "Retorna la información completa del usuario que está autenticado con el token JWT",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Información del usuario obtenida exitosamente",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "{\"id\":1,\"username\":\"admin\",\"email\":\"admin@example.com\",\"telefono\":3001234567,\"descripcion\":\"Administrador del sistema\",\"rol\":\"ADMIN\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "No autenticado - Token no válido o no proporcionado",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "\"No autenticado\""
				)
			)
		)
	})
	public ResponseEntity<?> getCurrentUser(
		@Parameter(hidden = true) Authentication authentication) {
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
