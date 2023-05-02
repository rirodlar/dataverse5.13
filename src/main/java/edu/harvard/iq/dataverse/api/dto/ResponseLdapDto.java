package edu.harvard.iq.dataverse.api.dto;


public class ResponseLdapDto {
    private boolean success;
    private String message;
    private Data data = new Data();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public String getRut() {
        return this.data.getRut();
    }

    public String getUser() {
        return this.data.getUser();
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", data=" + data +
                '}';
    }
}

class Data {
    private String user;
    private String password;
    private String tipo;
    private String rut;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }
}
