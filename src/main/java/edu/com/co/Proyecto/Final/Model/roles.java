package edu.com.co.Proyecto.Final.Model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "roles")
public class roles {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idRol")
	private Long idRol;
	
	@Column(nullable = false, unique = true, length = 100)
	private String nombreRol;
	
	@OneToMany(mappedBy = "rol", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<usuario> usuarios;
	
	public roles() {}
	
	public roles(String nombreRol) {
		this.nombreRol = nombreRol;
	}
	
	public Long getIdRol() {
		return idRol;
	}
	
	public void setIdRol(Long idRol) {
		this.idRol = idRol;
	}
	
	public String getNombreRol() {
		return nombreRol;
	}
	
	public void setNombreRol(String nombreRol) {
		this.nombreRol = nombreRol;
	}
	
	public List<usuario> getUsuarios() {
		return usuarios;
	}
	
	public void setUsuarios(List<usuario> usuarios) {
		this.usuarios = usuarios;
	}

}
