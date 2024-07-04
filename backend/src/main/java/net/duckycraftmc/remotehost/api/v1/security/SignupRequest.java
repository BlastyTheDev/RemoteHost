package net.duckycraftmc.remotehost.api.v1.security;

import lombok.Data;

@Data
public class SignupRequest {

    private final String username;
    private final String password;
    private final String discord;

}
