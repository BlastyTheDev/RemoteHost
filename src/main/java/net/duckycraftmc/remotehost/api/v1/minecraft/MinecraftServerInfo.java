package net.duckycraftmc.remotehost.api.v1.minecraft;

import lombok.Data;

@Data
public class MinecraftServerInfo {
    
    private Integer id;
    private String name;
    private String address;
    private Integer port;
    private String status;
    private Integer playersOnline;
    private Integer maxPlayers;
    
}
