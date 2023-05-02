package edu.harvard.iq.dataverse;


import com.google.gson.Gson;
import edu.harvard.iq.dataverse.api.AbstractApiBean;
import edu.harvard.iq.dataverse.api.dto.ResponseApiAcademico;
import edu.harvard.iq.dataverse.api.dto.ResponseLdapDto;
import edu.harvard.iq.dataverse.api.dto.UserResponseDto;
import edu.harvard.iq.dataverse.util.EnumFacultadUsachUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import static edu.harvard.iq.dataverse.util.Constant.*;

@Stateless
@Named
public class RestUsachXServiceBean extends AbstractApiBean implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(RestUsachXServiceBean.class.getCanonicalName());
    public static final String X_DATAVERSE_KEY = "X-Dataverse-key";
    public static final String APPLICATION_JSON = "application/json";

    private Gson gson = new Gson();

    public Response activate(String user, String password) throws Exception {


        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            return error(Response.Status.BAD_REQUEST, "invalid request");
        }


        //validate Ldap
        ResponseLdapDto responseLdapDto = this.connectionLdap(user, password);
        if (!responseLdapDto.isSuccess()) {
            return error(Response.Status.BAD_REQUEST, responseLdapDto.getMessage());
        }
        //Api Academico
        ResponseApiAcademico responseApiAcademico = this.connectionApiAcademic(responseLdapDto.getRut());
        //rev si usuario existe en BD
        if (!this.isUserRegister(user)) {
            //register user
            String afiliation = this.getAffiliation(responseApiAcademico.getCodigoUnidadMayorContrato());

            UserResponseDto userResponseDto = this.createUser(responseApiAcademico, responseLdapDto);
            this.assignRol(userResponseDto.getIdentifier(), afiliation);

            return ok("Activation OK :" + responseApiAcademico.getRun());

        } else {
            return error(Response.Status.BAD_REQUEST, "username :" + user + " already exists");
        }

    }

    public ResponseLdapDto connectionLdap(String user, String password) throws Exception {

        JSONObject json = new JSONObject();
        json.put("user", user);
        json.put("password", calculateSHA1(password));
        HttpPost request = new HttpPost(URL_LDAP_SEGIC);
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("Authorization", getBasicAuthenticationHeader(USER_LDAP, PASSWORD_LDAP));
        request.setEntity(params);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);
            ResponseLdapDto responseLdapDto = gson.fromJson(retSrc, ResponseLdapDto.class);

            if (statusCode == 200 && entity != null) {
                return responseLdapDto;
            }
            throw new Exception("Error LDAP");
        }
    }

    public ResponseApiAcademico connectionApiAcademic(String rut) throws Exception {

        HttpGet request = new HttpGet(URL_API_ACADEMICO_MOCK + rut);
        request.addHeader("Authorization", getBasicAuthenticationHeader(USER_API_ACADEMICO, PASSWORD_API_ACADEMICO));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);

            if (statusCode == 200 && entity != null) {
                ResponseApiAcademico responseApiAcademico = gson.fromJson(retSrc, ResponseApiAcademico.class);
                return responseApiAcademico;
            }
            throw new Exception("Error Api Academic");
        }
    }


    public UserResponseDto createUser(ResponseApiAcademico responseApiAcademico, ResponseLdapDto responseLdapDto) throws Exception {


        String affiliation = getAffiliation(responseApiAcademico.getCodigoUnidadMayorContrato());
        if (affiliation == null) {
            throw new Exception("affiliation does not exist for 'codigoUnidadMayorContrato' :" + responseApiAcademico.getNombreUnidadMayorContrato());
        }

        JSONObject json = new JSONObject();
        json.put("firstName", responseApiAcademico.getNombres());
        json.put("lastName", responseApiAcademico.getPrimerApellido());
        json.put("userName", responseLdapDto.getUser());
        json.put("affiliation", affiliation);
        json.put("position", responseApiAcademico.getPlanta());
        json.put("email", responseApiAcademico.getEmail());


        HttpPost request = new HttpPost("http://localhost:8080/api/builtin-users?key=builtInS3kretKey=123");
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);


            if (statusCode == 200 && entity != null) {
                UserResponseDto userResponseDto = gson.fromJson(retSrc, UserResponseDto.class);
                return userResponseDto;
            }
            if (statusCode == 400) {
                throw new Exception("Username " + responseLdapDto.getUser() + " already exists");
            }
            throw new Exception("Error Create User");
        }

    }

    public boolean assignRol(String nombre, String idtf) throws Exception {
        JSONObject json = new JSONObject();
        json.put("assignee", nombre);
        json.put("role", ROL_DS_CONTRIBUTOR);

        Dataverse dataverse = findDataverse(idtf);

        HttpPost request = new HttpPost(URI_PATH + "/api/dataverses/" + dataverse.getId() + "/assignments");
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        request.addHeader(X_DATAVERSE_KEY, API_TOKEN);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);
            if (statusCode == 200 && entity != null) {
                logger.info("User Active, Assignee Ok :" + dataverse.getAffiliation());
                return true;
            }
            throw new Exception("Rol Not Assign for user :" + nombre);
        }
    }

    public boolean isUserRegister(String identifier) throws IOException {
        //TODO: revisar duracion token
        HttpGet request = new HttpGet("http://localhost:8080/api/admin/authenticatedUsers/" + identifier);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return true;
            }
        }
        return false;
    }

    private String calculateSHA1(String password) {
        return org.apache.commons.codec.digest.DigestUtils.sha1Hex(password);
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }


    private String getAffiliation(int codigoUnidadMayorContrato) {

        var affiliationEnum = Arrays.stream(EnumFacultadUsachUtil.values())
                .filter(p -> p.getCodigoFactultad()
                        .equals(codigoUnidadMayorContrato))
                .findFirst();
        if (affiliationEnum.isPresent()) {
            return affiliationEnum.get().getCodigoAffiliation();
        } else {
            return null;
        }
    }

}
