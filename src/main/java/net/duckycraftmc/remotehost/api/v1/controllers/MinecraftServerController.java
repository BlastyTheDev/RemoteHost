package net.duckycraftmc.remotehost.api.v1.controllers;

import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/minecraft")
@RequiredArgsConstructor
public class MinecraftServerController {

    private final UserRepository userRepository;

}
