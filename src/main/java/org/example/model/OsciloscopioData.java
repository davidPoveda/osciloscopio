package org.example.model;

import java.util.List;

public class OsciloscopioData {

    private List<Integer> muestras;  // valores de amplitud procesados
    private OsciloscopioConfig config;
    private double frecuenciaEstimadaHz;
    private double amplitudMaxima;

    public OsciloscopioData() {}

    public OsciloscopioData(List<Integer> muestras, OsciloscopioConfig config,
                             double frecuenciaEstimadaHz, double amplitudMaxima) {
        this.muestras = muestras;
        this.config = config;
        this.frecuenciaEstimadaHz = frecuenciaEstimadaHz;
        this.amplitudMaxima = amplitudMaxima;
    }

    public List<Integer> getMuestras() { return muestras; }
    public void setMuestras(List<Integer> muestras) { this.muestras = muestras; }

    public OsciloscopioConfig getConfig() { return config; }
    public void setConfig(OsciloscopioConfig config) { this.config = config; }

    public double getFrecuenciaEstimadaHz() { return frecuenciaEstimadaHz; }
    public void setFrecuenciaEstimadaHz(double frecuenciaEstimadaHz) {
        this.frecuenciaEstimadaHz = frecuenciaEstimadaHz;
    }

    public double getAmplitudMaxima() { return amplitudMaxima; }
    public void setAmplitudMaxima(double amplitudMaxima) { this.amplitudMaxima = amplitudMaxima; }
}
