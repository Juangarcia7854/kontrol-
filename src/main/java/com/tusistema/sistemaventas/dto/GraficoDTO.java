package com.tusistema.sistemaventas.dto;

import java.util.List;

public class GraficoDTO {
    private List<String> labels;
    private List<? extends Number> data;

    public GraficoDTO(List<String> labels, List<? extends Number> data) {
        this.labels = labels;
        this.data = data;
    }

    // Getters y Setters
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<? extends Number> getData() { return data; }
    public void setData(List<? extends Number> data) { this.data = data; }
}