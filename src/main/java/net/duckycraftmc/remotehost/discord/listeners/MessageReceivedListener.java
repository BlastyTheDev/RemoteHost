package net.duckycraftmc.remotehost.discord.listeners;

import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import net.duckycraftmc.remotehost.discord.DiscordBot;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MessageReceivedListener extends ListenerAdapter {

    private final DiscordBot bot;
    private final UserRepository userRepository;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getChannelType() != ChannelType.PRIVATE)
            return;
        if (e.getAuthor().isBot())
            return;
        User user = userRepository.findByDiscord(e.getAuthor().getName()).orElse(null);
        if (user == null)
            return;
        System.out.println("User found: " + user.getUsername());
        if (user.getDiscordVerified())
            return;
        System.out.println("User not verified");
        if (bot.getVerifyCodeMap().get(e.getAuthor().getName()) == null)
            return;
        System.out.println("Verification code found");
        if (e.getMessage().getContentRaw().equals(String.valueOf(bot.getVerifyCodeMap().get(e.getAuthor().getName())))) {
            user.setDiscordVerified(true);
            userRepository.save(user);
            e.getChannel().sendMessage("Your account `" + user.getUsername() + "` has been verified!").queue();
        }
    }

}
