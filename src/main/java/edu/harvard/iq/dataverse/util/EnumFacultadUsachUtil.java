package edu.harvard.iq.dataverse.util;

public enum EnumFacultadUsachUtil implements java.io.Serializable {

    FACULTAD_ADMS_Y_ECONOMÍA(60, "FAE"),
    FACULTAD_DE_CIENCIA(50, "FCIENCIA"),
    FACULTAD_DE_CIENCIAS_MEDICAS(83, "FCM"),
    FACULTAD_DE_DERECHO(125, "FDERECHO"),
    FACULTAD_DE_HUMANIDADES(55, "FAHU"),
    FACULTAD_DE_INGENIERÍA(40, "FING"),
    FACULTAD_DE_QUÍMICA_Y_BIOLOGÍA(95, "FQYB"),
    FACULTAD_TECNOLÓGICA(65, "FACTEC"),
    ESCUELA_DE_ARQUITECTURA(81, "ARQUITECTURA"),
    PROGRAMA_DE_BACHILLERATO(54, "BACHI"),
    VICERRECTORIA_ACADEMICA(15, "VRA");

    private final Integer codigoFactultad;
    private final String codigoAffiliation;

    EnumFacultadUsachUtil(Integer codigoFactultad, String codigoAffiliation) {
        this.codigoAffiliation = codigoAffiliation;
        this.codigoFactultad = codigoFactultad;
    }

    public Integer getCodigoFactultad() {
        return codigoFactultad;
    }

    public String getCodigoAffiliation() {
        return codigoAffiliation;
    }
}