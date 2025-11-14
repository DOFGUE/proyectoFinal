package edu.com.co.Proyecto.Final.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.com.co.Proyecto.Final.Model.resena;

public interface resenaRepository extends JpaRepository<resena, Long> {
	
	Optional<resena> findByIdResena(Long idResena);

}
