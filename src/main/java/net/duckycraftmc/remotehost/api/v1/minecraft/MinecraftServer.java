package net.duckycraftmc.remotehost.api.v1.minecraft;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "server_type", nullable = false)
    private MinecraftServerType serverType;
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;
    @Column(name = "collaborators", nullable = false)
    private String collaborators; // format: "id1,id2,id3"

    public List<User> getCollaborators(UserRepository userRepository) {
        String[] collaboratorIds = collaborators.split(",");
        List<User> users = new ArrayList<>();
        for (String collaboratorId : collaboratorIds)
            users.add(userRepository.findById(Integer.parseInt(collaboratorId)).orElse(null));
        return users;
    }

    public void addCollaborator(User collaborator) {
        collaborators += "," + collaborator.getId();
    }

}
