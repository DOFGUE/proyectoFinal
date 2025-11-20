package edu.com.co.Proyecto.Final.Model;

/**
 * DTO para las respuestas de autenticación JWT
 */
public class JwtResponse {
	
	private String token;
	private String type = "Bearer";
	private String username;
	private String roles;
	
	public JwtResponse() {}
	
	public JwtResponse(String token, String username, String roles) {
		this.token = token;
		this.username = username;
		this.roles = roles;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getRoles() {
		return roles;
	}
	
	public void setRoles(String roles) {
		this.roles = roles;
	}
}
