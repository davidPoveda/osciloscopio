package org.example.controller;

import org.example.model.OsciloscopioConfig;
import org.example.model.OsciloscopioData;
import org.example.service.OsciloscopioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/osciloscopio")
@CrossOrigin(origins = "*")
public class OsciloscopioController {

    private static final Logger log = LoggerFactory.getLogger(OsciloscopioController.class);

    private final OsciloscopioService service;

    public OsciloscopioController(OsciloscopioService service) {
        this.service = service;
    }

    @GetMapping("/datos")
    public ResponseEntity<OsciloscopioData> obtenerDatos() {
        log.info("GET /api/osciloscopio/datos - Solicitud de datos recibida");
        OsciloscopioData datos = service.obtenerDatos();
        log.info("GET /api/osciloscopio/datos - Respondiendo con {} muestras", datos.getMuestras().size());
        return ResponseEntity.ok(datos);
    }

    @GetMapping("/config")
    public ResponseEntity<OsciloscopioConfig> obtenerConfig() {
        log.info("GET /api/osciloscopio/config - Solicitud de configuración recibida");
        OsciloscopioConfig config = service.obtenerConfig();
        log.info("GET /api/osciloscopio/config - sampleRate={}, congelado={}", config.getSampleRate(), config.isCongelado());
        return ResponseEntity.ok(config);
    }

    @PutMapping("/config")
    public ResponseEntity<OsciloscopioConfig> actualizarConfig(@RequestBody OsciloscopioConfig config) {
        log.info("PUT /api/osciloscopio/config - Actualizando configuración: {}", config);
        OsciloscopioConfig actualizada = service.actualizarConfig(config);
        log.info("PUT /api/osciloscopio/config - Configuración actualizada correctamente");
        return ResponseEntity.ok(actualizada);
    }

    @PostMapping("/iniciar")
    public ResponseEntity<Map<String, String>> iniciarCaptura() {
        log.info("POST /api/osciloscopio/iniciar - Solicitud de inicio de captura recibida");
        service.iniciarCaptura();
        log.info("POST /api/osciloscopio/iniciar - Captura iniciada correctamente");
        return ResponseEntity.ok(Map.of("estado", "captura iniciada"));
    }

    @PostMapping("/detener")
    public ResponseEntity<Map<String, String>> detenerCaptura() {
        log.info("POST /api/osciloscopio/detener - Solicitud de detención de captura recibida");
        service.detenerCaptura();
        log.info("POST /api/osciloscopio/detener - Captura detenida correctamente");
        return ResponseEntity.ok(Map.of("estado", "captura detenida"));
    }

    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstado() {
        log.info("GET /api/osciloscopio/estado - Solicitud de estado recibida");
        boolean capturando = service.isCapturando();
        float sampleRate = service.obtenerConfig().getSampleRate();
        log.info("GET /api/osciloscopio/estado - capturando={}, sampleRate={}", capturando, sampleRate);
        return ResponseEntity.ok(Map.of("capturando", capturando, "sampleRate", sampleRate));
    }
}
