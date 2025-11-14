package edu.com.co.Proyecto.Final.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.com.co.Proyecto.Final.Model.roles;

public interface rolRepository extends JpaRepository<roles, Long> {
	
	Optional<roles> findByNombreRol(String nombreRol);

}
