/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moondolphin.miportafolio;

/**
 *
 * @author Administrator
 */
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController // Le dice a Spring que esta clase manejará peticiones web
public class APIController {

    @GetMapping("/api/saludo") // Esta función se ejecutará cuando alguien visite /api/saludo
    public String saludar(@RequestParam(value = "nombre", defaultValue = "Mundo") String nombre) {
        return String.format("¡Hola, %s! Este saludo viene desde Java.", nombre);
    }
}