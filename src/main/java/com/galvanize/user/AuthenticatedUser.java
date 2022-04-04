package com.galvanize.user;


import com.fasterxml.jackson.annotation.JsonInclude;

public class AuthenticatedUser {
    private boolean authenticated;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AuthenticatedUser(boolean authenticated, User user) {
        this.authenticated = authenticated;
        this.user = user;
    }
}
