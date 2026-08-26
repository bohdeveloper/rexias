package com.bohdeveloper.rexia.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador placeholder de la Fase 0: solo confirma que Spring MVC + Tiles
 * responden en "/". Sustituir/ampliar en la Fase 1.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }
}
