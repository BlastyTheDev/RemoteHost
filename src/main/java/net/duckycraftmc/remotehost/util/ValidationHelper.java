package net.duckycraftmc.remotehost.util;

import net.duckycraftmc.remotehost.api.v1.minecraft.MinecraftServer;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;

import java.util.Objects;

public class ValidationHelper {

    public static boolean isUserOwnerOrCoOwner(User user, MinecraftServer server, UserRepository userRepository) {
        return Objects.equals(server.getOwnerId(), user.getId()) || server.getCoOwners(userRepository).contains(user);
    }

}
