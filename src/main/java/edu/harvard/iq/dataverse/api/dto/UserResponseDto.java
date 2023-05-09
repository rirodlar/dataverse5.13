package edu.harvard.iq.dataverse.api.dto;

import java.util.ArrayList;
import java.util.List;

public class UserResponseDto {
    private String status;

    private Data data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private String firstName;
        private String lastName;

        private String email;

        private String affiliation;

        private String position;

        private boolean superuser;

        private User user;

        private Trace traces;
        private AuthenticatedUser authenticatedUser;

        public boolean isSuperuser() {
            return superuser;
        }

        public void setSuperuser(boolean superuser) {
            this.superuser = superuser;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAffiliation() {
            return affiliation;
        }

        public void setAffiliation(String affiliation) {
            this.affiliation = affiliation;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public AuthenticatedUser getAuthenticatedUser() {
            return authenticatedUser;
        }

        public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
            this.authenticatedUser = authenticatedUser;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public Trace getTraces() {
            return traces;
        }

        public void setTraces(Trace traces) {
            this.traces = traces;
        }
    }

    public static class AuthenticatedUser {
        private String identifier;

        public String getIdentifier() {
            return identifier;
        }
    }

    public static class User{
        private String identifier;
        private String name;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Trace{
        private RoleAssignment roleAssignments;

        public RoleAssignment getRoleAssignments() {
            return roleAssignments;
        }

        public void setRoleAssignments(RoleAssignment roleAssignments) {
            this.roleAssignments = roleAssignments;
        }
    }

    public String getIdentifier() {
        return this.data.authenticatedUser.identifier;
    }

    public static class RoleAssignment{
        private int count;
        private List<Item> items = new ArrayList<>();

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public static class Item{
        private int id;
        private String definitionPointName;
        private int definitionPointId;
        private String dsContributor;
        private String roleName;
        private String roleAlias;

        public String getRoleAlias() {
            return roleAlias;
        }

        public void setRoleAlias(String roleAlias) {
            this.roleAlias = roleAlias;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDefinitionPointName() {
            return definitionPointName;
        }

        public void setDefinitionPointName(String definitionPointName) {
            this.definitionPointName = definitionPointName;
        }

        public int getDefinitionPointId() {
            return definitionPointId;
        }

        public void setDefinitionPointId(int definitionPointId) {
            this.definitionPointId = definitionPointId;
        }

        public String getDsContributor() {
            return dsContributor;
        }

        public void setDsContributor(String dsContributor) {
            this.dsContributor = dsContributor;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }



}