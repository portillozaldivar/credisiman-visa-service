package com.siman.credisiman.visa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolicitudReposicionTarjetaRequest {
    public String pais;
    public String numeroTarjeta;
    public String nombreEmbozar;
    public String razonReposicion;
}
