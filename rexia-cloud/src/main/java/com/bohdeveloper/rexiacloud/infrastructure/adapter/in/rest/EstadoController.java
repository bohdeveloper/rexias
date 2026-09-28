package com.bohdeveloper.rexiacloud.infrastructure.adapter.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Adaptador de entrada de humo para la Fase 0: confirma a mano que el
 * contexto arranca y el servidor embebido responde, igual que el
 * "GET / -> 200" verificado en rexia-clasico. La Fase 1 trae el primer
 * caso de uso real detrás de application/port/in.
 */
@RestController
public class EstadoController {

    @GetMapping("/api/estado")
    public Map<String, String> estado() {
        return Map.of(
                "proyecto", "rexia-cloud",
                "estado", "ok"
        );
    }
}
