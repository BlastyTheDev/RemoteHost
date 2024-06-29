package net.duckycraftmc.remotehost.discord;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import net.duckycraftmc.remotehost.discord.listeners.MessageReceivedListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/discord")
public class DiscordBot {

    private final JDA jda;
    private final Guild guild;
    private List<Member> members;

    @Getter
    private final HashMap<String, Integer> verifyCodeMap = new HashMap<>();

    public DiscordBot(UserRepository userRepository) throws InterruptedException {
        jda = JDABuilder.createDefault(Dotenv.load().get("DISCORD_BOT_TOKEN"))
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .addEventListeners(new MessageReceivedListener(this, userRepository))
                .setStatus(OnlineStatus.INVISIBLE)
                .build().awaitReady();
        guild = jda.getGuildById(Dotenv.load().get("DISCORD_GUILD_ID"));
        assert guild != null;
        guild.loadMembers().onSuccess(memberList -> members = memberList);
        while (members == null)
            Thread.sleep(1000);
    }

    @GetMapping("/member")
    public String getMember(@RequestParam(name = "u") String discordUsername, HttpServletResponse response) {
        Member member = members.stream().filter(m -> m.getUser().getName().equals(discordUsername)).findFirst().orElse(null);
        if (member == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Member not found";
        }
        return member.getUser().getName();
    }

    @GetMapping("/verify")
    public Integer getVerificationCode(HttpServletResponse response) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getDiscordVerified()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return -1;
        }
        Member member = members.stream().filter(m -> m.getUser().getName().equals(user.getDiscord())).findFirst().orElse(null);
        if (member == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return -1;
        }
        int code = new Random().nextInt(100000, 999999);
        verifyCodeMap.put(member.getUser().getName(), code);
        return code;
    }

}
