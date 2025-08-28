package com.cgdev.tienda.controller;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int codigo;
    private String mensaje;
    private T informacion;

    public ApiResponse(int codigo, String mensaje, T informacion) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.informacion = informacion;
    }
}