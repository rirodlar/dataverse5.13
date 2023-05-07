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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;


@Stateless
@Named
public class RestUsachServiceBean extends AbstractApiBean implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(RestUsachServiceBean.class.getCanonicalName());
    public static final String X_DATAVERSE_KEY = "X-Dataverse-key";
    public static final String APPLICATION_JSON = "application/json";
    public static final String KEY_CREATE_USER = "builtInS3kretKey=123";
    public static final String AUTHORIZATION = "Authorization";
    public static final String URI_PATH = "http://localhost:8080";
    public static final String API_TOKEN = "499a5e23-9c66-4f28-b69a-6c75ef5d5a47";
    public static final String ROL_DS_CONTRIBUTOR = "dsContributor";
    public static final String URL_LDAP_SEGIC = "https://cuentas.segic.cl/api/cuenta/check";
    public static final String URL_LDAP_SEGIC_MOCK = "https://run.mocky.io/v3/0b9582f8-6240-44fd-9f5f-dcba6f702d04/";
    public static final String USER_LDAP = "apiPSP";
    public static final String PASSWORD_LDAP = "eMj9gHrH1J";
    public static final String USER_API_ACADEMICO = "docente.usach.cl";
    public static final String PASSWORD_API_ACADEMICO = "8Q57m9GtznM72NXrZgmP3sbt6xGYWJKcnwPagNwK";
    public static final String URL_API_ACADEMICO = "https://api.dti.usach.cl/api/docente/";
    public static final String URL_API_ACADEMICO_MOCK = "https://run.mocky.io/v3/c4873f8e-6d90-4f83-95c7-dfb536982024/";
    public static final String ACADEMICOS = "ACADEMICOS";

    private Gson gson = new Gson();


    public ResponseApiAcademico validateUser(String user, String password) throws Exception {
        //validate Ldap
        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            throw new Exception("invalid request");
        }


        ResponseLdapDto responseLdapDto = this.connectionLdap(user, password);
        if (!responseLdapDto.isSuccess()) {
            throw new Exception(responseLdapDto.getMessage());
        }

        //ver si es admin
        UserResponseDto userResponseDto = isUserRegister(user);
        if(userResponseDto != null && userResponseDto.getData().isSuperuser()){
            ResponseApiAcademico responseApiAcademico = mapperUser(userResponseDto.getData());
            return responseApiAcademico;
        }

        ResponseApiAcademico responseApiAcademico = this.connectionApiAcademic(responseLdapDto.getRut());
        if (!responseApiAcademico.getPlanta().equals(ACADEMICOS)) {
            logger.warning(responseApiAcademico.toString());
            throw new Exception("The Planta should be 'ACADEMICOS'");
        }

        String affiliation = this.getAffiliation(responseApiAcademico.getCodigoUnidadMayorContrato());
        responseApiAcademico.setAffiliation(affiliation);
//        if (this.isUserRegister(user)) {
//            return responseApiAcademico;
//        }

        //dataverse
        try {
            //user existe
            UserResponseDto userResponseDto1 = validateUser(user);
            //si no tiene tiene rol
            if(userResponseDto1.getData().getTraces().getRoleAssignments() == null){
                this.assignRol(userResponseDto1.getData().getUser().getIdentifier(), affiliation);
            }
            return responseApiAcademico;

        }catch (UsernameNotFoundException e){
            //User Notfound
            UserResponseDto userResponseDto1 = this.createUser(responseApiAcademico, user, affiliation);
            this.assignRol(userResponseDto1.getIdentifier(), affiliation);
            return responseApiAcademico;
        }catch (Exception ex){
            throw new Exception(ex);
        }
//        if(userResponseDto1.getStatus().equals("OK")){
//           if(userResponseDto1.getData().getTraces().getRoleAssignments() != null){
//                return responseApiAcademico;
//           }else{
//             //assingRol
//               this.assignRol(userResponseDto1.getIdentifier(), affiliation);
//           }
//        }else{
//            UserResponseDto userResponseDto = this.createUser(responseApiAcademico, user, affiliation);
//            this.assignRol(userResponseDto.getIdentifier(), affiliation);
//            return responseApiAcademico;
//        }

        //register user
//        UserResponseDto userResponseDto = this.createUser(responseApiAcademico, user, affiliation);
//        this.assignRol(userResponseDto.getIdentifier(), affiliation);
//        return responseApiAcademico;


    }

    //TODO: revisar
    private ResponseApiAcademico mapperUser(UserResponseDto.Data data) {

        ResponseApiAcademico responseApiAcademico = new ResponseApiAcademico();
        responseApiAcademico.setNombres(data.getFirstName());
        responseApiAcademico.setPrimerApellido(data.getLastName());
        responseApiAcademico.setEmail(data.getEmail());
        responseApiAcademico.setAffiliation(data.getAffiliation());
        responseApiAcademico.setPlanta(data.getPosition());
        return responseApiAcademico;
    }

    public ResponseLdapDto connectionLdap(String user, String password) throws Exception {

        JSONObject json = new JSONObject();
        json.put("user", user);
        json.put("password", calculateSHA1(password));
        HttpPost request = new HttpPost(URL_LDAP_SEGIC);
        StringEntity params = new StringEntity(json.toString());
        request.addHeader(AUTHORIZATION, getBasicAuthenticationHeader(USER_LDAP, PASSWORD_LDAP));
        request.setEntity(params);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);
            ResponseLdapDto responseLdapDto = gson.fromJson(retSrc, ResponseLdapDto.class);

            if (statusCode == 200 && entity != null) {
                return responseLdapDto;
            } else {
                throw new Exception("User Ldap Not Found");
            }

        } catch (Exception e) {
            throw new Exception("Error LDAP :" + e.getMessage());
        }
    }

    public ResponseApiAcademico connectionApiAcademic(String rut) throws Exception {


        HttpGet request = new HttpGet(getUrlMockApiAcademico(rut) + rut);
        request.addHeader("Authorization", getBasicAuthenticationHeader(USER_API_ACADEMICO, PASSWORD_API_ACADEMICO));
        int statusCode = 0;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);
            JSONObject result = new JSONObject(retSrc);
            if (statusCode == 200 && entity != null) {
                ResponseApiAcademico responseApiAcademico = gson.fromJson(retSrc, ResponseApiAcademico.class);
                return responseApiAcademico;
            }else{
                throw new Exception(result.getString("message"));
            }

        }catch (Exception e){
            throw new Exception("Error Api Academico: "+ statusCode);
        }
    }

    private static String getUrlMockApiAcademico(String rut) {
        String url;
        switch (rut){
            case "10000001":  url = "https://run.mocky.io/v3/c4873f8e-6d90-4f83-95c7-dfb536982024/";break;
            case "10000002":  url = "https://run.mocky.io/v3/4e60029f-9a1e-4f19-abed-0963e02797f1/";break;
            case "10000003":  url = "https://run.mocky.io/v3/2aa7f0b4-3002-4bf5-9062-e26195355749/";break;
            case "10000004":  url = "https://run.mocky.io/v3/6504ef2d-d11d-40fb-920b-be456f3e6ed2/";break;
            case "10000005":  url = "https://run.mocky.io/v3/aaf58ca3-1fd3-4a21-bb7c-b5953aa94976/";break;
            case "10000006":  url = "https://run.mocky.io/v3/a36c5708-d921-4077-80f4-000d39bed264/";break;
            case "10000007":  url = "https://run.mocky.io/v3/d28ba763-b801-44b8-9b4e-4ad7fd5c343f/";break; //500
            case "10000008":  url = "https://run.mocky.io/v3/e0963687-cb0b-4a18-b8aa-35b50dbea235/";break; //404
            case "10000009":  url = "https://run.mocky.io/v3/17f2e686-a24d-488a-8173-739941957ccc/";break; //
            default: url = URL_API_ACADEMICO;
        }
        return url;
    }


    public UserResponseDto createUser(ResponseApiAcademico responseApiAcademico, String user, String affiliation) throws Exception {

        JSONObject json = getJsonCreateUserObject(responseApiAcademico, user, affiliation);
        HttpPost request = new HttpPost(URI_PATH + "/api/builtin-users?key=" + KEY_CREATE_USER);
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("content-type", APPLICATION_JSON);
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

            throw new Exception("ERROR " + retSrc);

        }

    }


    public boolean assignRol(String nombre, String idtf) throws Exception {
        JSONObject json = new JSONObject();
        json.put("assignee", nombre);
        json.put("role", ROL_DS_CONTRIBUTOR);

        Dataverse dataverse = findDataverse(idtf);

        if(dataverse == null){
            throw new Exception("Dataverse Not Found for affiliation :"+ idtf);
        }

        HttpPost request = new HttpPost(URI_PATH + "/api/dataverses/" + dataverse.getId() + "/assignments");
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("content-type", APPLICATION_JSON);
        request.setEntity(params);
        request.addHeader(X_DATAVERSE_KEY, API_TOKEN);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (statusCode == 200 && entity != null) {
                logger.info("User Active, Assignee Ok :" + dataverse.getAffiliation());
                return true;
            }
            throw new Exception("Rol Not Assign for user :" + nombre);
        }
    }

    public UserResponseDto isUserRegister(String identifier) throws IOException {
        HttpGet request = new HttpGet(URI_PATH + "/api/admin/authenticatedUsers/" + identifier);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);
            if (statusCode == 200) {
                UserResponseDto userResponseDto = gson.fromJson(retSrc, UserResponseDto.class);
                return userResponseDto;
            }
        }
        return null;
    }

    private UserResponseDto validateUser(String identifier) throws Exception{
        HttpGet request = new HttpGet(URI_PATH + "/api/users/"+identifier+"/traces/");
        request.addHeader("content-type", APPLICATION_JSON);
        request.addHeader(X_DATAVERSE_KEY, API_TOKEN);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);
            if (statusCode == 200 && entity != null) {
                UserResponseDto userResponseDto = gson.fromJson(retSrc, UserResponseDto.class);
                return userResponseDto;
            }
            throw new UsernameNotFoundException("User Not Found :" + identifier);
        }
    }

    private static JSONObject getJsonCreateUserObject(ResponseApiAcademico responseApiAcademico, String user, String affiliation) {
        JSONObject json = new JSONObject();
        json.put("firstName", responseApiAcademico.getNombres());
        json.put("lastName", responseApiAcademico.getPrimerApellido());
        json.put("userName", user);
        json.put("affiliation", affiliation);
        json.put("position", responseApiAcademico.getPlanta());
        json.put("email", responseApiAcademico.getEmail());
        return json;
    }


    private String calculateSHA1(String password) {
        return org.apache.commons.codec.digest.DigestUtils.sha1Hex(password);
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }


    private String getAffiliation(int codigoUnidadMayorContrato) throws Exception {
        EnumFacultadUsachUtil enumFacultadUsachUtil = Arrays.stream(EnumFacultadUsachUtil.values())
                .filter(p -> p.getCodigoFactultad()
                        .equals(codigoUnidadMayorContrato))
                .findFirst().orElseThrow(() -> new Exception("affiliation does not exist :" + codigoUnidadMayorContrato));
        return enumFacultadUsachUtil.getCodigoAffiliation();

    }

}
