package org.example.model;

public class OsciloscopioConfig {

    private float sampleRate = 44100f;
    private int escalaYIndex = 2;   // 0=0.1V/div, 1=0.5V/div, 2=1.0V/div
    private int msPerDivIndex = 2;  // 0=1ms, 1=5ms, 2=10ms, 3=20ms, 4=50ms
    private boolean congelado = false;
    private int offsetX = 0;
    private int offsetY = 0;

    public float getSampleRate() { return sampleRate; }
    public void setSampleRate(float sampleRate) { this.sampleRate = sampleRate; }

    public int getEscalaYIndex() { return escalaYIndex; }
    public void setEscalaYIndex(int escalaYIndex) { this.escalaYIndex = escalaYIndex; }

    public int getMsPerDivIndex() { return msPerDivIndex; }
    public void setMsPerDivIndex(int msPerDivIndex) { this.msPerDivIndex = msPerDivIndex; }

    public boolean isCongelado() { return congelado; }
    public void setCongelado(boolean congelado) { this.congelado = congelado; }

    public int getOffsetX() { return offsetX; }
    public void setOffsetX(int offsetX) { this.offsetX = offsetX; }

    public int getOffsetY() { return offsetY; }
    public void setOffsetY(int offsetY) { this.offsetY = offsetY; }
}
