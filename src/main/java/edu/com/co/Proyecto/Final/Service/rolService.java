package edu.com.co.Proyecto.Final.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.com.co.Proyecto.Final.Model.roles;
import edu.com.co.Proyecto.Final.Repository.rolRepository;

@Service
public class rolService {
	
	@Autowired
	private rolRepository rolRepo;
	
	// Obtener rol por ID
	public Optional<roles> obtenerRolPorId(Long idRol) {
		return rolRepo.findById(idRol);
	}
	
	// Obtener rol por nombre
	public Optional<roles> obtenerRolPorNombre(String nombreRol) {
		if (nombreRol == null || nombreRol.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del rol no puede estar vacío");
		}
		return rolRepo.findByNombreRol(nombreRol);
	}
	
}
