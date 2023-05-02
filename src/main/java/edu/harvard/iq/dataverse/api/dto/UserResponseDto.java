package edu.harvard.iq.dataverse.api.dto;

public class UserResponseDto {
    private String status;

    private Data data;

    public static class Data {
        private AuthenticatedUser authenticatedUser;


        public AuthenticatedUser getAuthenticatedUser() {
            return authenticatedUser;
        }

        public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
            this.authenticatedUser = authenticatedUser;
        }
    }

    public static class AuthenticatedUser {
        private String identifier;

        public String getIdentifier() {
            return identifier;
        }
    }

    public String getIdentifier() {
        return this.data.authenticatedUser.identifier;
    }

}