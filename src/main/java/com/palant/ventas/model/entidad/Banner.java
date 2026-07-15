package com.palant.ventas.model.entidad;

import java.util.Date;

public class Banner {
    private int idBanner;
    private String imagenBase64;
    private Date fechaSubida;

    public int getIdBanner() { return idBanner; }
    public void setIdBanner(int idBanner) { this.idBanner = idBanner; }
    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
    public Date getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(Date fechaSubida) { this.fechaSubida = fechaSubida; }
}
