package net.duckycraftmc.remotehost.api.v1.minecraft;

import lombok.Data;

@Data
public class CreateRequest {

    private String name;
    private Integer ram;
    private String type;
    private String version;
    private String build;

}
