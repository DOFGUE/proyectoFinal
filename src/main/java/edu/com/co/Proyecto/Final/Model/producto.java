package edu.com.co.Proyecto.Final.Model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "productos")	
public class producto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idProducto")
	private Long idProducto;
	
	@Column(nullable = false, unique = true, length = 100)
	private String nombreProducto;
	
	@Column(nullable = false)
	private Double precioProducto;
	
	@Column(nullable = false, length = 255)
	private String rutaImagenProducto;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String descripcionProducto;
	
	@Column(nullable = false)
	private Double calificacionProducto = 0.0;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String ingredientesProducto;
	
	@OneToMany(mappedBy = "producto", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<resena> resenas;
	
	public producto() {}
	
	public producto(String nombreProducto) {
		this.nombreProducto = nombreProducto;
	}
	
	public Long getIdProducto() {
		return idProducto;
	}
	
	public void setIdProducto(Long idProducto) {
		this.idProducto = idProducto;
	}
	
	public String getNombreProducto() {
		return nombreProducto;
	}
	
	public void setNombreProducto(String nombreProducto) {
		this.nombreProducto = nombreProducto;
	}
	
	public Double getPrecioProducto() {
		return precioProducto;
	}
	
	public void setPrecioProducto(Double precioProducto) {
		this.precioProducto = precioProducto;
	}
	
	public String getRutaImagenProducto() {
		return rutaImagenProducto;
	}
	
	public void setRutaImagenProducto(String rutaImagenProducto) {
		this.rutaImagenProducto = rutaImagenProducto;
	}
	
	public String getDescripcionProducto() {
		return descripcionProducto;
	}
	
	public void setDescripcionProducto(String descripcionProducto) {
		this.descripcionProducto = descripcionProducto;
	}
	
	public Double getCalificacionProducto() {
		return calificacionProducto;
	}
	
	public void setCalificacionProducto(Double calificacionProducto) {
		this.calificacionProducto = calificacionProducto;
	}
	
	public String getIngredientesProducto() {
		return ingredientesProducto;
	}
	
	public void setIngredientesProducto(String ingredientesProducto) {
		this.ingredientesProducto = ingredientesProducto;
	}
	
	public List<resena> getResenas() {
		return resenas;
	}
	
	public void setResenas(List<resena> resenas) {
		this.resenas = resenas;
	}
}
