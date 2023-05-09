package edu.harvard.iq.dataverse;


import com.google.gson.Gson;
import edu.harvard.iq.dataverse.api.AbstractApiBean;
import edu.harvard.iq.dataverse.api.dto.ResponseApiAcademico;
import edu.harvard.iq.dataverse.api.dto.ResponseLdapDto;
import edu.harvard.iq.dataverse.api.dto.UserResponseDto;
import edu.harvard.iq.dataverse.util.Constant;
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
import java.util.Optional;
import java.util.logging.Logger;
import static edu.harvard.iq.dataverse.util.Constant.*;


@Stateless
@Named
public class RestUsachServiceBean extends AbstractApiBean implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(RestUsachServiceBean.class.getCanonicalName());

    private Gson gson = new Gson();


    //TODO: evaluar cambiar DTO
    public ResponseApiAcademico findUser(String user, String password) throws Exception {
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

        //ver si es curador
        if(userResponseDto != null) {
            try {
                userResponseDto = findUserWithTraces(user);
                Optional<UserResponseDto.Item> item = userResponseDto.getData().getTraces().getRoleAssignments().getItems().stream().filter(u -> u.getRoleAlias().equals(Constant.rolCurator)).findFirst();
                if (item.isPresent()) {
                    return mapperUser(userResponseDto.getData());
                }
            }catch (Exception e){
                logger.warning("the user "+ user + " is notCurator not Found" + e.getMessage());
            }
        }

        //ver si es academico
        ResponseApiAcademico responseApiAcademico = this.connectionApiAcademic(responseLdapDto.getRut());
        if (!responseApiAcademico.getPlanta().equals(ACADEMICOS)) {
            logger.warning(responseApiAcademico.toString());
            throw new Exception("The Planta should be 'ACADEMICOS'");
        }

        String affiliation = this.getAffiliation(responseApiAcademico.getCodigoUnidadMayorContrato());
        responseApiAcademico.setAffiliation(affiliation);

        //dataverse
        try {
            //user existe
            if(userResponseDto == null){
                 userResponseDto = findUserWithTraces(user);
            }
           // UserResponseDto userResponseDto1 = findUserWithTraces(user);
            //si no tiene tiene rol
            if(userResponseDto != null && userResponseDto.getData().getTraces().getRoleAssignments() == null){
                this.assignRol(userResponseDto.getData().getUser().getIdentifier(), affiliation);
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
        //mock URL  - pendiente
        HttpPost request = new HttpPost(getUrlMockLdap(user));
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

    private String getUrlMockLdap(String urlLdapSegic) {
        String url;
        switch (urlLdapSegic){
            case "dato1": url = "https://run.mocky.io/v3/94f19a8f-d1b4-474c-bc18-f08db917b10f/"; break;
            case "dato2": url = "https://run.mocky.io/v3/bb7fee92-ccd8-45b0-8603-5a712f80a5e7/"; break;
            case "dato3": url = "https://run.mocky.io/v3/2aae32e2-d1df-46f9-bab0-0f200d427dae/"; break;
            case "dato4": url = "https://run.mocky.io/v3/eefbe045-b2e9-4701-9b37-01cbedc1c1f3/"; break;
            case "dato5": url = "https://run.mocky.io/v3/7f8937ec-5334-405a-824a-0612a8679b1a/"; break;
            case "dato6": url = "https://run.mocky.io/v3/5e322272-7056-4d5d-962d-8eb28a474785/"; break;
            case "dato7": url = "https://run.mocky.io/v3/610dc91f-e3e4-4cac-82ba-0be652cbb3e4/"; break;
            case "admin.dataverse": url = "https://run.mocky.io/v3/4261450a-2ef4-4f8a-9e50-a3335c632155/"; break;
            default: url = URL_LDAP_SEGIC;
        }
        return url;
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

    private UserResponseDto findUserWithTraces(String identifier) throws Exception{
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
            throw new UsernameNotFoundException("Error findUser :" + identifier);
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
