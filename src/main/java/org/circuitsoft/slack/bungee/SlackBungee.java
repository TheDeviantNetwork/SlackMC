package org.circuitsoft.slack.bungee;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.circuitsoft.slack.api.SlackMessage;
import org.circuitsoft.slack.api.SlackPoster;
import org.circuitsoft.slack.api.web.SlackWebServer;

public class SlackBungee extends Plugin implements Listener {

    private List<String> blacklist;
    private Configuration config;
    private SlackPoster slackPoster;
    private SlackWebServer slackWebServer;

    @Override
    public void onEnable() {
        getLogger().info("Slack has been enabled!");
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new SlackBungeeCommand(this));
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "config.yml does not exist: ", ex);
        }
        updateConfig(this.getDescription().getVersion());
        //todo: add more webhooks to SlackPoster
        String webhookUrl = config.getString("channels.default.incoming-webhook");
        blacklist = config.getStringList("blacklist");
        if (webhookUrl == null || webhookUrl.trim().isEmpty() || webhookUrl.equals("https://hooks.slack.com/services/")) {
            getLogger().severe("You have not set your webhook URL in the config!");
            return;
        }
        this.slackPoster = new SlackPoster(webhookUrl);
        getProxy().getScheduler().runAsync(this, slackPoster);

    }

    public void reloadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "config.yml does not exist: ", ex);
        }
        slackPoster.setWebhookUrl(config.getString("webhook"));
        blacklist = config.getStringList("blacklist");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        ProxiedPlayer p = (ProxiedPlayer) event.getSender();
        if (event.isCommand()) {
            if (!config.getBoolean("send-commands")) {
                return;
            }
            if (hasPermission(p, "slack.hide.command") && isAllowed(event.getMessage())) {
                send(event.getMessage(), p, p.getServer().getInfo().getName(), false);
            }
        } else if (hasPermission(p, "slack.hide.chat")) {
            send(event.getMessage(), p, p.getServer().getInfo().getName(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(ServerConnectedEvent event) {
        if (hasPermission(event.getPlayer(), "slack.hide.login")) {
            send("_joined_", event.getPlayer(), event.getServer().getInfo().getName(), true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(ServerDisconnectEvent event) {
        if (hasPermission(event.getPlayer(), "slack.hide.logout")) {
            send("_quit_", event.getPlayer(), event.getTarget().getName(), true);
        }
    }

    private void send(String message, ProxiedPlayer player, String serverName, boolean useMarkdown) {
        slackPoster.addMessage(new SlackMessage(message, player.getName(), serverName, useMarkdown));
    }

    private boolean isAllowed(String command) {
        if (config.getBoolean("use-blacklist")) {
            return !blacklist.contains(command);
        } else {
            return true;
        }
    }

    private void updateConfig(String version) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                Files.copy(getResourceAsStream("config.yml"), file.toPath());
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Default config not saved: ", ex);
            }
        }
        String configV = config.getString("v");
        if (configV == null) {
            if (version != null) {
                config.set("version", version);
            }
        } else {
            if (!configV.equals(version)) {
                config.set("version", version);
            }
        }
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to save config: ", ex);
        }
    }

    private boolean hasPermission(ProxiedPlayer player, String permission) {
        if (config.getBoolean("use-perms")) {
            return !player.hasPermission(permission);
        } else {
            return true;
        }
    }

    public SlackPoster getSlackPoster() {
        return slackPoster;
    }
}
