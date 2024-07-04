package net.duckycraftmc.remotehost.api.v1.security;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;

}
