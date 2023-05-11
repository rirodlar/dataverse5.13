package edu.harvard.iq.dataverse.authorization.providers.builtin;

import edu.harvard.iq.dataverse.RestUsachServiceBean;
import edu.harvard.iq.dataverse.api.dto.ResponseUserDto;
import edu.harvard.iq.dataverse.authorization.AuthenticatedUserDisplayInfo;
import edu.harvard.iq.dataverse.authorization.AuthenticationProviderDisplayInfo;
import edu.harvard.iq.dataverse.authorization.AuthenticationRequest;
import edu.harvard.iq.dataverse.authorization.AuthenticationResponse;
import edu.harvard.iq.dataverse.authorization.AuthenticationServiceBean;
import edu.harvard.iq.dataverse.authorization.CredentialsAuthenticationProvider;
import java.util.Arrays;
import java.util.List;

import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.util.BundleUtil;
import edu.harvard.iq.dataverse.validation.PasswordValidatorServiceBean;

/**
 * An authentication provider built into the application. Uses JPA and the 
 * local database to store the users.
 * 
 * @author michael
 */
public class BuiltinAuthenticationProvider implements CredentialsAuthenticationProvider {
    
    public static final String PROVIDER_ID = "builtin";
    /**
     * TODO: Think more about if it really makes sense to have the key for a
     * credential be a Bundle key. What if we want to reorganize our Bundle
     * files and rename some Bundle keys? Would login be broken until we update
     * the strings below?
     */
    public static final String KEY_USERNAME_OR_EMAIL = "login.builtin.credential.usernameOrEmail";
    public static final String KEY_PASSWORD = "login.builtin.credential.password";
    private static List<Credential> CREDENTIALS_LIST;
      
    final BuiltinUserServiceBean bean;
    final AuthenticationServiceBean authBean;
    private PasswordValidatorServiceBean passwordValidatorService;

    final RestUsachServiceBean restUsachServiceBean;

    public BuiltinAuthenticationProvider( BuiltinUserServiceBean aBean, PasswordValidatorServiceBean passwordValidatorService, AuthenticationServiceBean auBean, RestUsachServiceBean restUsachServiceBean) {
        this.bean = aBean;
        this.authBean = auBean;
        this.restUsachServiceBean = restUsachServiceBean;
        this.passwordValidatorService = passwordValidatorService;
        CREDENTIALS_LIST = Arrays.asList(new Credential(KEY_USERNAME_OR_EMAIL), new Credential(KEY_PASSWORD, true));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public AuthenticationProviderDisplayInfo getInfo() {
        return new AuthenticationProviderDisplayInfo(getId(), BundleUtil.getStringFromBundle("auth.providers.title.builtin"), "Internal user repository");
    }

    @Override
    public boolean isPasswordUpdateAllowed() {
        return true;
    }

    @Override
    public boolean isUserInfoUpdateAllowed() {
        return true;
    }

    @Override
    public boolean isUserDeletionAllowed() {
        return true;
    }
    
    @Override
    public void deleteUser(String userIdInProvider) {
        bean.removeUser(userIdInProvider);
    }
    
    @Override
    public void updatePassword(String userIdInProvider, String newPassword) {
        BuiltinUser biUser = bean.findByUserName( userIdInProvider  );
        biUser.updateEncryptedPassword(PasswordEncryption.get().encrypt(newPassword),
                                       PasswordEncryption.getLatestVersionNumber());
        bean.save(biUser);
    }
    
    /**
     * Validates that the passed password is indeed the password of the user.
     * @param userIdInProvider
     * @param password
     * @return {@code true} if the password matches the user's password; {@code false} otherwise.
     */
    @Override
    public Boolean verifyPassword( String userIdInProvider, String password ) {
        BuiltinUser biUser = bean.findByUserName( userIdInProvider  );
        if ( biUser == null ) return null;
        return PasswordEncryption.getVersion(biUser.getPasswordEncryptionVersion())
                                 .check(password, biUser.getEncryptedPassword());
    }
    

    @Override
    public AuthenticationResponse authenticate( AuthenticationRequest authReq ) {

        ResponseUserDto responseUserDto = new ResponseUserDto();
        String user = authReq.getCredential(KEY_USERNAME_OR_EMAIL);
        String password = authReq.getCredential(KEY_PASSWORD);
            try {
                 responseUserDto =  restUsachServiceBean.findUser(user, password);
            } catch (Exception e) {
                return AuthenticationResponse.makeError("Error Validate", e);
            }

        AuthenticatedUserDisplayInfo authenticatedUserDisplayInfo = new AuthenticatedUserDisplayInfo(
                responseUserDto.getNombres(),
                responseUserDto.getPrimerApellido(),
                responseUserDto.getEmail(),
                responseUserDto.getAffiliation(),
                responseUserDto.getPlanta());
        return AuthenticationResponse.makeSuccess(user, authenticatedUserDisplayInfo);
   }

    @Override
    public List<Credential> getRequiredCredentials() {
        return CREDENTIALS_LIST;
    }

    @Override
    public boolean isOAuthProvider() {
        return false;
    }

    @Override
    public boolean isDisplayIdentifier() {
        return false;
    }

}
