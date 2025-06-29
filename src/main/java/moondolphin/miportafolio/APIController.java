/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moondolphin.miportafolio;

/**
 *
 * @author Administrator
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List; // Importar List

@RestController
public class APIController {

    @Autowired // Inyecta automáticamente nuestro repositorio
    private ProyectoRepository proyectoRepository;

    @GetMapping("/api/saludo")
    public String saludar(@RequestParam(value = "nombre", defaultValue = "Mundo") String nombre) {
        return String.format("¡Hola, %s! Este saludo viene desde Java.", nombre);
    }

    // NUEVO ENDPOINT
    @GetMapping("/api/proyectos")
    public List<Proyecto> obtenerTodosLosProyectos() {
        // Busca todos los proyectos en la DB y los devuelve como JSON
        return proyectoRepository.findAll();
    }
}