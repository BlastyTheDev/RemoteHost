package net.duckycraftmc.remotehost.api.v1.minecraft;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "minecraft_servers")
public class MinecraftServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column(name = "ram", nullable = false)
    private Integer ram; // in MiB
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "collaborators", nullable = false)
    private String collaborators; // format: "id1,id2,id3"

}
