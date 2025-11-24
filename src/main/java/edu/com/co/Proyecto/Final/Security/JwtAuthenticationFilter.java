package edu.com.co.Proyecto.Final.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private usuarioDetailsService usuarioDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// Si ya hay una autenticación en el contexto (ej: OAuth2), no procesarla con JWT
		if (SecurityContextHolder.getContext().getAuthentication() != null 
			&& SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
			filterChain.doFilter(request, response);
			return;
		}
		
		// Obtener el header Authorization
		final String authorizationHeader = request.getHeader("Authorization");
		
		String username = null;
		String jwt = null;
		
		// Verificar si el header contiene un Bearer token
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			try {
				username = jwtUtil.extractUsername(jwt);
			} catch (Exception e) {
				// Token inválido o expirado
				logger.error("Error al extraer username del token: " + e.getMessage());
			}
		}
		
		// Si se extrajo el username y no hay autenticación en el contexto
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			
			// Cargar los detalles del usuario
			UserDetails userDetails = this.usuarioDetailsService.loadUserByUsername(username);
			
			// Validar el token
			if (jwtUtil.validateToken(jwt, userDetails)) {
				
				// Crear el objeto de autenticación
				UsernamePasswordAuthenticationToken authenticationToken = 
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				// Establecer la autenticación en el contexto de seguridad
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}
		
		// Continuar con la cadena de filtros
		filterChain.doFilter(request, response);
	}
}
