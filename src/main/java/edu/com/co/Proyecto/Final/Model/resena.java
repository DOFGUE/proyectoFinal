package edu.com.co.Proyecto.Final.Model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "resenas")
public class resena {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idResena")
	private Long idResena;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idUsuario", nullable = false)
	private usuario usuario;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idProducto", nullable = false)
	private producto producto;
	
	@Column(nullable = false)
	private Integer calificacion; // 1-5 estrellas
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String comentarioResena;
	
	@Column(nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCreacionResena = new Date();
	
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaActualizacionResena = new Date();
	
	public resena() {}
	
	public resena(usuario usuario, producto producto, Integer calificacion) {
		this.usuario = usuario;
		this.producto = producto;
		this.calificacion = calificacion;
	}
	
	public Long getIdResena() {
		return idResena;
	}
	
	public void setIdResena(Long idResena) {
		this.idResena = idResena;
	}
	
	public usuario getUsuario() {
		return usuario;
	}
	
	public void setUsuario(usuario usuario) {
		this.usuario = usuario;
	}
	
	public producto getProducto() {
		return producto;
	}
	
	public void setProducto(producto producto) {
		this.producto = producto;
	}
	
	public Integer getCalificacion() {
		return calificacion;
	}
	
	public void setCalificacion(Integer calificacion) {
		this.calificacion = calificacion;
	}
	
	public String getComentarioResena() {
		return comentarioResena;
	}
	
	public void setComentarioResena(String comentarioResena) {
		this.comentarioResena = comentarioResena;
	}
	
	public Date getFechaCreacionResena() {
		return fechaCreacionResena;
	}
	
	public void setFechaCreacionResena(Date fechaCreacionResena) {
		this.fechaCreacionResena = fechaCreacionResena;
	}
	
	public Date getFechaActualizacionResena() {
		return fechaActualizacionResena;
	}
	
	public void setFechaActualizacionResena(Date fechaActualizacionResena) {
		this.fechaActualizacionResena = fechaActualizacionResena;
	}
	
	@PreUpdate
	protected void onUpdate() {
		fechaActualizacionResena = new Date();
	}
	
}
