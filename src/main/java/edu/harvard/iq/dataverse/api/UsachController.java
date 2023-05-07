/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.RestUsachServiceBean;
import edu.harvard.iq.dataverse.UserServiceBean;

import java.io.IOException;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author rarodriguezl
 */
@Path("usach")
public class UsachController extends AbstractApiBean {

    private static final Logger logger = Logger.getLogger(UsachController.class.getName());

    @EJB
    RestUsachServiceBean restUsachServiceBean;

    @EJB
    UserServiceBean userServiceBean;

    @POST
    @Path("/activate")
    public Response activate(JsonObject jsonObject) throws IOException {
        try {

            String user = jsonObject.getString("user");
            String password = jsonObject.getString("password");

           // return restUsachXServiceBean.activate(user, password);
            return null;

        } catch (Exception e) {
            logger.warning(e.getMessage());
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


//        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
//            return error(Response.Status.BAD_REQUEST, "invalid request");
//        }
//
//        try {
//            //validate Ldap
//            ResponseLdapDto responseLdapDto = restUsachServiceBean.connectionLdap(user, password);
//            if (!responseLdapDto.isSuccess()) {
//                return error(Response.Status.BAD_REQUEST, responseLdapDto.getMessage());
//            }
//            //Api Academico
//            ResponseApiAcademico responseApiAcademico = restUsachServiceBean.connectionApiAcademic(responseLdapDto.getRut());
//            //rev si usuario existe en BD
//            if (!restUsachServiceBean.isUserRegister(user)) {
//                //register user
//               // String affiliation = responseApiAcademico.getA
//                UserResponseDto userResponseDto = restUsachServiceBean.createUser(responseApiAcademico,responseLdapDto);
//                restUsachServiceBean.assignRol(userResponseDto.getIdentifier(), "FING");
//                return ok("OK para insertar usuario :" + responseApiAcademico.getRun());
//            } else {
//                return error(Response.Status.BAD_REQUEST, "username :" + user + "already exists");
//            }
//
//        } catch (Exception e) {
//            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
//        }










//    @POST
//    @Path("/ldap")
//    public Response ldap(JsonObject jsonObject) throws IOException {
//        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//        try {
//            String password = jsonObject.getString("password");
//            String hashedVal = calculateSHA1(password);
//            JSONObject json = new JSONObject();
//            json.put("user", jsonObject.getString("user"));
//            json.put("password", hashedVal);
//            HttpPost request = new HttpPost(URL_LDAP_SEGIC);
//            StringEntity params = new StringEntity(json.toString());
//            request.addHeader("content-type", "application/json");
//            request.addHeader("Authorization", getBasicAuthenticationHeader(URL_API_ACADEMICO, PASSWORD_LDAP));
//            request.setEntity(params);
//            HttpResponse response = httpClient.execute(request);
//            int statusCode = response.getStatusLine().getStatusCode();
//            HttpEntity entity = response.getEntity();
//            String retSrc = EntityUtils.toString(entity);
//            ResponseLdapDto responseLdapDto = gson.fromJson(retSrc, ResponseLdapDto.class);
//            if (statusCode == 200 && entity != null) {
//                if (responseLdapDto.isSuccess()) {
//                    return ok(responseLdapDto.getData().getRut());
//                } else {
//                    return error(Response.Status.BAD_REQUEST, responseLdapDto.getMessage());
//                }
//            }
//            return error(Response.Status.BAD_REQUEST, responseLdapDto.getMessage());
//        }catch (Exception e){
//            logger.severe(e.getMessage());
//            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
//        }finally {
//            httpClient.close();
//        }
//    }

//    @Path("/academico")
//    @GET
//    public Response findAcademico() throws IOException {
//        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//        try {
//            String rut ="111";
//            HttpGet request = new HttpGet(URL_API_ACADEMICO_MOCK + rut);
//            request.addHeader("content-type", "application/json");
//            request.addHeader("Authorization", getBasicAuthenticationHeader(USER_API_ACADEMICO, PASSWORD_API_ACADEMICO));
//
//            HttpResponse response = httpClient.execute(request);
//            int statusCode = response.getStatusLine().getStatusCode();
//            HttpEntity entity = response.getEntity();
//            String retSrc = EntityUtils.toString(entity);
//
//            if(statusCode == 403) return error(Response.Status.FORBIDDEN, "Forbidden");
//
//            JSONObject result = new JSONObject(retSrc);
//            if (statusCode == 200 && entity != null) {
//                return ok(result.getString("codigoUnidadMayorContrato"));
//            }else{
//                return error(Response.Status.BAD_REQUEST, result.getString("message"));
//            }
//
//        }catch (Exception e){
//            logger.severe(e.getMessage());
//            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
//        }finally {
//            httpClient.close();
//        }
//
//    }


}




