package org.example.service;

import org.example.model.OsciloscopioConfig;
import org.example.model.OsciloscopioData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class OsciloscopioService {

    private static final Logger log = LoggerFactory.getLogger(OsciloscopioService.class);

    private final byte[] buffer = new byte[32768];
    private OsciloscopioConfig config = new OsciloscopioConfig();

    private TargetDataLine line;
    private Thread captureThread;
    private boolean capturando = false;

    public synchronized void iniciarCaptura() {
        log.info("Iniciando captura de audio con sampleRate: {} Hz", config.getSampleRate());
        detenerCaptura();

        try {
            AudioFormat format = new AudioFormat(config.getSampleRate(), 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, 4096);
            line.start();
            capturando = true;
            log.info("Línea de audio abierta correctamente. Buffer size: {} bytes", buffer.length);

            captureThread = new Thread(() -> {
                log.debug("Hilo de captura iniciado");
                byte[] fragmento = new byte[1024];
                while (!Thread.currentThread().isInterrupted() && capturando) {
                    if (!config.isCongelado()) {
                        int bytesLeidos = line.read(fragmento, 0, fragmento.length);
                        System.arraycopy(buffer, bytesLeidos, buffer, 0, buffer.length - bytesLeidos);
                        System.arraycopy(fragmento, 0, buffer, buffer.length - bytesLeidos, bytesLeidos);
                        log.trace("Bytes leídos del micrófono: {}", bytesLeidos);
                    } else {
                        log.trace("Captura congelada, omitiendo lectura de audio");
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        log.warn("Hilo de captura interrumpido");
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                log.debug("Hilo de captura finalizado");
            });
            captureThread.setPriority(Thread.MAX_PRIORITY);
            captureThread.setDaemon(true);
            captureThread.start();
            log.info("Captura de audio iniciada exitosamente");

        } catch (Exception e) {
            log.error("Error al iniciar captura con sampleRate {} Hz: {}", config.getSampleRate(), e.getMessage());
            throw new RuntimeException("No se pudo iniciar captura con sampleRate: "
                    + config.getSampleRate() + " Hz. Causa: " + e.getMessage());
        }
    }

    public synchronized void detenerCaptura() {
        if (!capturando && captureThread == null) {
            log.debug("No hay captura activa, omitiendo detención");
            return;
        }
        log.info("Deteniendo captura de audio...");
        capturando = false;
        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
            log.debug("Hilo de captura detenido");
        }
        if (line != null) {
            line.stop();
            line.close();
            line = null;
            log.debug("Línea de audio cerrada");
        }
        log.info("Captura de audio detenida correctamente");
    }

    public OsciloscopioData obtenerDatos() {
        log.debug("Obteniendo datos del buffer de audio...");
        List<Integer> muestras = new ArrayList<>();

        double samplesPerPixel = calcularSamplesPerPixel();
        double multY = calcularMultY();
        log.debug("samplesPerPixel: {}, multY: {}", samplesPerPixel, multY);

        int maxPuntos = 1000;
        int paso = 2;

        for (int i = 0; i < buffer.length - 4 && muestras.size() < maxPuntos; i += paso) {
            short s1 = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
            int valorNormalizado = (int) (s1 * 100.0 / 32768.0 * multY);
            muestras.add(valorNormalizado);
        }

        double amplitudMaxima = muestras.stream()
                .mapToInt(Math::abs)
                .max()
                .orElse(0);

        double frecuencia = estimarFrecuencia();

        log.info("Datos obtenidos: {} muestras, frecuencia estimada: {} Hz, amplitud máxima: {}",
                muestras.size(), String.format("%.2f", frecuencia), amplitudMaxima);

        return new OsciloscopioData(muestras, config, frecuencia, amplitudMaxima);
    }

    public OsciloscopioConfig actualizarConfig(OsciloscopioConfig nuevaConfig) {
        log.info("Actualizando configuración: sampleRate={}, escalaYIndex={}, msPerDivIndex={}, congelado={}, offsetX={}, offsetY={}",
                nuevaConfig.getSampleRate(), nuevaConfig.getEscalaYIndex(), nuevaConfig.getMsPerDivIndex(),
                nuevaConfig.isCongelado(), nuevaConfig.getOffsetX(), nuevaConfig.getOffsetY());

        boolean cambioSampleRate = nuevaConfig.getSampleRate() != config.getSampleRate();
        this.config = nuevaConfig;

        if (cambioSampleRate && capturando) {
            log.info("SampleRate cambió a {} Hz, reiniciando captura...", nuevaConfig.getSampleRate());
            iniciarCaptura();
        }

        log.info("Configuración actualizada correctamente");
        return this.config;
    }

    public OsciloscopioConfig obtenerConfig() {
        log.debug("Consultando configuración actual: sampleRate={}, congelado={}",
                config.getSampleRate(), config.isCongelado());
        return config;
    }

    public boolean isCapturando() {
        log.debug("Consultando estado de captura: {}", capturando);
        return capturando;
    }

    private double calcularSamplesPerPixel() {
        double samplesPerMs = config.getSampleRate() / 1000.0;
        return switch (config.getMsPerDivIndex()) {
            case 0 -> (1.0 * samplesPerMs) / 50.0;
            case 1 -> (5.0 * samplesPerMs) / 50.0;
            case 3 -> (10.0 * samplesPerMs) / 50.0;
            case 4 -> (50.0 * samplesPerMs) / 50.0;
            default -> (20.0 * samplesPerMs) / 50.0;
        };
    }

    private double calcularMultY() {
        return switch (config.getEscalaYIndex()) {
            case 0 -> 4.0;
            case 1 -> 2.0;
            default -> 1.0;
        };
    }

    private double estimarFrecuencia() {
        int cruces = 0;
        boolean positivo = false;
        for (int i = 0; i < buffer.length - 2; i += 2) {
            short muestra = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
            boolean ahora = muestra >= 0;
            if (ahora != positivo) {
                cruces++;
                positivo = ahora;
            }
        }
        int totalMuestras = buffer.length / 2;
        double duracionSegundos = totalMuestras / config.getSampleRate();
        double frecuencia = (cruces / 2.0) / duracionSegundos;
        log.debug("Frecuencia estimada: {} Hz (cruces={})", String.format("%.2f", frecuencia), cruces);
        return frecuencia;
    }
}
