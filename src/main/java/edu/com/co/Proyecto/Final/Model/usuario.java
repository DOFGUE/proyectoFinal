package edu.com.co.Proyecto.Final.Model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class usuario {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idUsuario")
	private Long idUsuario;
	
	@Column(nullable = false, unique = true, length = 100)
	private String nombreUsuario;
	
	@Column(nullable = false, length = 100)
	private String contrasenaUsuario;
	
	@Column(nullable = false, unique = true, length = 100)
	private String emailUsuario;
	
	@Column(nullable = false)
	private Long numeroTelefonoUsuario;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String descripcionUsuario;
	
	@ManyToOne
	@JoinColumn(name = "idRol", nullable = false)
	private roles rol;
	
	@OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<resena> resenas;
	
	
	
	public usuario() {}
	
	public usuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}
	
	public Long getIdUsuario() {
		return idUsuario;
	}
	
	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}
	
	public String getNombreUsuario() {
		return nombreUsuario;
	}
	
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}
	
	public String getContrasenaUsuario() {
		return contrasenaUsuario;
	}
	
	public void setContrasenaUsuario(String contrasenaUsuario) {
		this.contrasenaUsuario = contrasenaUsuario;
	}
	
	public String getEmailUsuario() {
		return emailUsuario;
	}
	
	public void setEmailUsuario(String emailUsuario) {
		this.emailUsuario = emailUsuario;
	}
	
	public Long getNumeroTelefonoUsuario() {
		return numeroTelefonoUsuario;
	}
	
	public void setNumeroTelefonoUsuario(Long numeroTelefonoUsuario) {
		this.numeroTelefonoUsuario = numeroTelefonoUsuario;
	}
	
	public String getDescripcionUsuario() {
		return descripcionUsuario;
	}
	
	public void setDescripcionUsuario(String descripcionUsuario) {
		this.descripcionUsuario = descripcionUsuario;
	}
	
	public roles getRol() {
		return rol;
	}
	
	public void setRol(roles rol) {
		this.rol = rol;
	}
	
	public List<resena> getResenas() {
		return resenas;
	}
	
	public void setResenas(List<resena> resenas) {
		this.resenas = resenas;
	}
	
}
