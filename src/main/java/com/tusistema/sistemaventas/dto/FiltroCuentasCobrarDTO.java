package com.tusistema.sistemaventas.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class FiltroCuentasCobrarDTO {
    
    private int page = 0;
    private int size = 10;
    private String term = ""; 
    private String status = "PENDIENTE";

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Pageable getPageable() {
        return PageRequest.of(page, size);
    }
}