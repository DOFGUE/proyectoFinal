package edu.com.co.Proyecto.Final.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import edu.com.co.Proyecto.Final.Model.roles;
import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Model.producto;
import edu.com.co.Proyecto.Final.Repository.rolRepository;
import edu.com.co.Proyecto.Final.Repository.usuarioRepository;
import edu.com.co.Proyecto.Final.Repository.productoRepository;

@Component
public class dataInitializer implements CommandLineRunner {
	
	@Autowired
	private rolRepository rolRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private usuarioRepository usuarioRepository;
	
	@Autowired
	private productoRepository productoRepository;
	
	@Override
	public void run(String... args) throws Exception {
		// Inicializar roles si no existen
		initializeRoles();
		
		// Inicializar usuarios si no existen
		initializeUsuarios();
		
		// Inicializar productos si no existen
		initializeProductos();
	}
	
	private void initializeRoles() {
		// Crear rol ADMIN si no existe
		if (rolRepository.findByNombreRol("ADMIN").isEmpty()) {
			roles rolAdmin = new roles("ADMIN");
			rolRepository.save(rolAdmin);
			System.out.println("✓ Rol ADMIN creado exitosamente");
		}
		
		// Crear rol USER si no existe
		if (rolRepository.findByNombreRol("USER").isEmpty()) {
			roles rolUser = new roles("USER");
			rolRepository.save(rolUser);
			System.out.println("✓ Rol USER creado exitosamente");
		}
	}
	
	private void initializeUsuarios() {
		// Obtener los roles creados
		roles rolAdmin = rolRepository.findByNombreRol("ADMIN").orElse(null);
		roles rolUser = rolRepository.findByNombreRol("USER").orElse(null);
		
		if (rolAdmin == null || rolUser == null) {
			System.out.println("✗ Error: No se pudieron obtener los roles");
			return;
		}
		
		// Crear usuario administrador si no existe
		if (usuarioRepository.findByNombreUsuario("admin").isEmpty()) {
			usuario usuarioAdmin = new usuario();
			usuarioAdmin.setNombreUsuario("admin");
			usuarioAdmin.setContrasenaUsuario(passwordEncoder.encode("admin123"));
			usuarioAdmin.setEmailUsuario("admin@example.com");
			usuarioAdmin.setNumeroTelefonoUsuario(3001234567L);
			usuarioAdmin.setDescripcionUsuario("Usuario administrador del sistema");
			usuarioAdmin.setRol(rolAdmin);
			usuarioRepository.save(usuarioAdmin);
			System.out.println("✓ Usuario ADMIN creado exitosamente");
		}
		
		// Crear usuario regular si no existe
		if (usuarioRepository.findByNombreUsuario("user").isEmpty()) {
			usuario usuarioNormal = new usuario();
			usuarioNormal.setNombreUsuario("user");
			usuarioNormal.setContrasenaUsuario(passwordEncoder.encode("user123"));
			usuarioNormal.setEmailUsuario("user@example.com");
			usuarioNormal.setNumeroTelefonoUsuario(3009876543L);
			usuarioNormal.setDescripcionUsuario("Usuario regular del sistema");
			usuarioNormal.setRol(rolUser);
			usuarioRepository.save(usuarioNormal);
			System.out.println("✓ Usuario USER creado exitosamente");
		}
	}
	
	private void initializeProductos() {
		// Cheesecake de Fresa
		if (productoRepository.findByNombreProducto("Cheesecake de Fresa").isEmpty()) {
			producto cheesecakeFresa = new producto();
			cheesecakeFresa.setNombreProducto("Cheesecake de Fresa");
			cheesecakeFresa.setPrecioProducto(45000.0);
			cheesecakeFresa.setRutaImagenProducto("Cheesecake-de-fresa.webp");
			cheesecakeFresa.setDescripcionProducto("Delicioso cheesecake con una base de galleta horneada y cubierta con fresas frescas. Una combinación perfecta de cremosidad y sabor frutal.");
			cheesecakeFresa.setCalificacionProducto(4.8);
			cheesecakeFresa.setIngredientesProducto("Queso crema, galletas digestivas, mantequilla, fresas, azúcar, vainilla, harina de maíz");
			productoRepository.save(cheesecakeFresa);
			System.out.println("✓ Producto Cheesecake de Fresa creado exitosamente");
		}
		
		// Cholado
		if (productoRepository.findByNombreProducto("Cholado").isEmpty()) {
			producto cholado = new producto();
			cholado.setNombreProducto("Cholado");
			cholado.setPrecioProducto(15000.0);
			cholado.setRutaImagenProducto("cholado.webp");
			cholado.setDescripcionProducto("Bebida refrescante hecha a base de hielo, jarabe de frutas, conservas, soda y leche condensada. Tradicional de Colombia.");
			cholado.setCalificacionProducto(4.7);
			cholado.setIngredientesProducto("Hielo, jarabe de frutas, conservas de frutas, soda, leche condensada, frutas variadas");
			productoRepository.save(cholado);
			System.out.println("✓ Producto Cholado creado exitosamente");
		}
		
		// Churros
		if (productoRepository.findByNombreProducto("Churros").isEmpty()) {
			producto churros = new producto();
			churros.setNombreProducto("Churros");
			churros.setPrecioProducto(12000.0);
			churros.setRutaImagenProducto("churros.webp");
			churros.setDescripcionProducto("Churros crujientes y dorados, perfectos para acompañar con chocolate caliente o café. Clásico de la pastelería.");
			churros.setCalificacionProducto(4.6);
			churros.setIngredientesProducto("Harina, agua, sal, aceite, azúcar, canela");
			productoRepository.save(churros);
			System.out.println("✓ Producto Churros creado exitosamente");
		}
		
		// Crema de Papaya
		if (productoRepository.findByNombreProducto("Crema de Papaya").isEmpty()) {
			producto cremaPapaya = new producto();
			cremaPapaya.setNombreProducto("Crema de Papaya");
			cremaPapaya.setPrecioProducto(28000.0);
			cremaPapaya.setRutaImagenProducto("crema_de_papaya.webp");
			cremaPapaya.setDescripcionProducto("Postre cremoso elaborado con pulpa de papaya fresca, nata y leche. Un toque tropical y refrescante.");
			cremaPapaya.setCalificacionProducto(4.5);
			cremaPapaya.setIngredientesProducto("Papaya fresca, nata, leche, azúcar, gelatina, vainilla");
			productoRepository.save(cremaPapaya);
			System.out.println("✓ Producto Crema de Papaya creado exitosamente");
		}
		
		// Fondant au Chocolat
		if (productoRepository.findByNombreProducto("Fondant au Chocolat").isEmpty()) {
			producto fondantChocolate = new producto();
			fondantChocolate.setNombreProducto("Fondant au Chocolat");
			fondantChocolate.setPrecioProducto(38000.0);
			fondantChocolate.setRutaImagenProducto("fondant-au-chocolat.webp");
			fondantChocolate.setDescripcionProducto("Exquisito postre francés de chocolate con interior líquido y cremoso. Acompañado con salsa de chocolate.");
			fondantChocolate.setCalificacionProducto(4.9);
			fondantChocolate.setIngredientesProducto("Chocolate oscuro, mantequilla, huevos, harina, azúcar, vainilla, cacao en polvo");
			productoRepository.save(fondantChocolate);
			System.out.println("✓ Producto Fondant au Chocolat creado exitosamente");
		}
		
		// Gelato
		if (productoRepository.findByNombreProducto("Gelato").isEmpty()) {
			producto gelato = new producto();
			gelato.setNombreProducto("Gelato");
			gelato.setPrecioProducto(18000.0);
			gelato.setRutaImagenProducto("gelato.webp");
			gelato.setDescripcionProducto("Helado italiano artesanal con textura cremosa y intenso sabor. Disponible en diversos sabores.");
			gelato.setCalificacionProducto(4.8);
			gelato.setIngredientesProducto("Leche, nata, azúcar, yemas de huevo, saborizantes naturales");
			productoRepository.save(gelato);
			System.out.println("✓ Producto Gelato creado exitosamente");
		}
		
		// Milhojas
		if (productoRepository.findByNombreProducto("Milhojas").isEmpty()) {
			producto milhojas = new producto();
			milhojas.setNombreProducto("Milhojas");
			milhojas.setPrecioProducto(32000.0);
			milhojas.setRutaImagenProducto("milhojas.webp");
			milhojas.setDescripcionProducto("Pastel tradicional de mil hojas con crema pastelera entre capas de hojaldre crujiente y cobertura de merengue.");
			milhojas.setCalificacionProducto(4.7);
			milhojas.setIngredientesProducto("Hojaldre, crema pastelera, leche, huevos, harina, mantequilla, azúcar, merengue");
			productoRepository.save(milhojas);
			System.out.println("✓ Producto Milhojas creado exitosamente");
		}
		
		// Panna Cotta
		if (productoRepository.findByNombreProducto("Panna Cotta").isEmpty()) {
			producto pannaCotta = new producto();
			pannaCotta.setNombreProducto("Panna Cotta");
			pannaCotta.setPrecioProducto(35000.0);
			pannaCotta.setRutaImagenProducto("pana_cotta.webp");
			pannaCotta.setDescripcionProducto("Postre italiano cremoso y suave elaborado con nata fresca. Se sirve con salsa de frutas rojas.");
			pannaCotta.setCalificacionProducto(4.8);
			pannaCotta.setIngredientesProducto("Nata fresca, leche, azúcar, gelatina, vainilla, frutas rojas");
			productoRepository.save(pannaCotta);
			System.out.println("✓ Producto Panna Cotta creado exitosamente");
		}
		
		// Pastafrola
		if (productoRepository.findByNombreProducto("Pastafrola").isEmpty()) {
			producto pastafrola = new producto();
			pastafrola.setNombreProducto("Pastafrola");
			pastafrola.setPrecioProducto(22000.0);
			pastafrola.setRutaImagenProducto("pastafrola.webp");
			pastafrola.setDescripcionProducto("Dulce tradicional latinoamericano con masa de sablé rellena de dulce de membrillo, cubierta con tiras de masa entrecruzadas.");
			pastafrola.setCalificacionProducto(4.6);
			pastafrola.setIngredientesProducto("Harina, mantequilla, azúcar, huevos, dulce de membrillo, vainilla, sal");
			productoRepository.save(pastafrola);
			System.out.println("✓ Producto Pastafrola creado exitosamente");
		}
		
		// Pastéis de Belém
		if (productoRepository.findByNombreProducto("Pastéis de Belém").isEmpty()) {
			producto pasteisBelem = new producto();
			pasteisBelem.setNombreProducto("Pastéis de Belém");
			pasteisBelem.setPrecioProducto(24000.0);
			pasteisBelem.setRutaImagenProducto("pasteis_belem.webp");
			pasteisBelem.setDescripcionProducto("Pastelillos portugueses con hojaldre crujiente rellenos de crema de huevo y canela. Espolvoreados con canela y azúcar.");
			pasteisBelem.setCalificacionProducto(4.7);
			pasteisBelem.setIngredientesProducto("Hojaldre, yemas de huevo, leche, azúcar, canela, vainilla");
			productoRepository.save(pasteisBelem);
			System.out.println("✓ Producto Pastéis de Belém creado exitosamente");
		}
		
		// Pavlova
		if (productoRepository.findByNombreProducto("Pavlova").isEmpty()) {
			producto pavlova = new producto();
			pavlova.setNombreProducto("Pavlova");
			pavlova.setPrecioProducto(40000.0);
			pavlova.setRutaImagenProducto("pavlola.webp");
			pavlova.setDescripcionProducto("Postre elegante de merengue crujiente con interior suave, coronado con nata y frutas frescas.");
			pavlova.setCalificacionProducto(4.9);
			pavlova.setIngredientesProducto("Claras de huevo, azúcar, nata, frutas frescas, vainilla, almidón de maíz");
			productoRepository.save(pavlova);
			System.out.println("✓ Producto Pavlova creado exitosamente");
		}
		
		// Tiramisú
		if (productoRepository.findByNombreProducto("Tiramisú").isEmpty()) {
			producto tiramisu = new producto();
			tiramisu.setNombreProducto("Tiramisú");
			tiramisu.setPrecioProducto(36000.0);
			tiramisu.setRutaImagenProducto("tiramisu.webp");
			tiramisu.setDescripcionProducto("Postre italiano capas de bizcocho remojado en café, mascarpone cremoso y cacao en polvo. Un clásico irresistible.");
			tiramisu.setCalificacionProducto(4.9);
			tiramisu.setIngredientesProducto("Bizcocho, café, mascarpone, huevos, azúcar, cacao en polvo, vainilla");
			productoRepository.save(tiramisu);
			System.out.println("✓ Producto Tiramisú creado exitosamente");
		}
		
		// Torta Tres Leches
		if (productoRepository.findByNombreProducto("Torta Tres Leches").isEmpty()) {
			producto tortaTresLeches = new producto();
			tortaTresLeches.setNombreProducto("Torta Tres Leches");
			tortaTresLeches.setPrecioProducto(42000.0);
			tortaTresLeches.setRutaImagenProducto("torta-tres-leches.webp");
			tortaTresLeches.setDescripcionProducto("Torta clásica latinoamericana humedecida con tres tipos de leche, cubierta con merengue o nata. Suave y deliciosa.");
			tortaTresLeches.setCalificacionProducto(4.8);
			tortaTresLeches.setIngredientesProducto("Harina, huevos, azúcar, leche evaporada, leche condensada, crema de leche, vainilla");
			productoRepository.save(tortaTresLeches);
			System.out.println("✓ Producto Torta Tres Leches creado exitosamente");
		}
		
		// Torta de Chocolate
		if (productoRepository.findByNombreProducto("Torta de Chocolate").isEmpty()) {
			producto tortaChocolate = new producto();
			tortaChocolate.setNombreProducto("Torta de Chocolate");
			tortaChocolate.setPrecioProducto(38000.0);
			tortaChocolate.setRutaImagenProducto("torta_chocolate.webp");
			tortaChocolate.setDescripcionProducto("Torta húmeda de chocolate intenso con capas de ganache de chocolate y cobertura espejo. Para los amantes del chocolate.");
			tortaChocolate.setCalificacionProducto(4.9);
			tortaChocolate.setIngredientesProducto("Chocolate oscuro, harina, huevos, azúcar, mantequilla, cacao en polvo, vainilla");
			productoRepository.save(tortaChocolate);
			System.out.println("✓ Producto Torta de Chocolate creado exitosamente");
		}
	}

}
