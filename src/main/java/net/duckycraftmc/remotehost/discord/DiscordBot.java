package net.duckycraftmc.remotehost.discord;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Getter
@Component
public class DiscordBot {

    private final JDA jda;
    private final Guild guild;
    private List<Member> members;

    public DiscordBot() throws InterruptedException {
        jda = JDABuilder.createDefault(Dotenv.load().get("DISCORD_BOT_TOKEN"))
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .setStatus(OnlineStatus.INVISIBLE)
                .build().awaitReady();
        guild = jda.getGuildById(Dotenv.load().get("DISCORD_GUILD_ID"));
        assert guild != null;
        guild.loadMembers().onSuccess(memberList -> members = memberList);
        while (members == null)
            Thread.sleep(1000);
    }

}
