package edu.harvard.iq.dataverse.api.dto;

public class ResponseUserDto {
    private String run;
    private String primerApellido;
    private String segundoApellido;
    private String nombres;
    private String email;
    private String nombreCentroCostoContrato;
    private String planta;
    private int codigoUnidadMayorContrato;
    private String nombreUnidadMayorContrato;

    private String affiliation;

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreCentroCostoContrato() {
        return nombreCentroCostoContrato;
    }

    public void setNombreCentroCostoContrato(String nombreCentroCostoContrato) {
        this.nombreCentroCostoContrato = nombreCentroCostoContrato;
    }

    public int getCodigoUnidadMayorContrato() {
        return codigoUnidadMayorContrato;
    }

    public void setCodigoUnidadMayorContrato(int codigoUnidadMayorContrato) {
        this.codigoUnidadMayorContrato = codigoUnidadMayorContrato;
    }

    public String getNombreUnidadMayorContrato() {
        return nombreUnidadMayorContrato;
    }

    public void setNombreUnidadMayorContrato(String nombreUnidadMayorContrato) {
        this.nombreUnidadMayorContrato = nombreUnidadMayorContrato;
    }

    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

}
