package de.Zorro909.ConfigurationLibrary.Messages;

import java.util.HashMap;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class TranslatableMessage {

    private HashMap<Language, String> translatedVersions;

    public TranslatableMessage(HashMap<Language, String> translatedVersions) {
        this.translatedVersions = translatedVersions;
    }

    public HashMap<Language, String> getTranslatedVersions() {
        return translatedVersions;
    }

    public void sendMessage(Player player, Language forcedLanguage) {
        String message = defaultReplaces(player, getText(forcedLanguage));
        player.sendMessage(TextComponent.fromLegacyText(message));
    }

    public void sendMessageWithPrefix(Player player, TranslatableMessage prefix,
            Language forcedLanguage) {
        ComponentBuilder cbuilder = new ComponentBuilder("");
        cbuilder.appendLegacy(prefix.getText(forcedLanguage));
        cbuilder.appendLegacy(defaultReplaces(player, getText(forcedLanguage)));
        player.sendMessage(cbuilder.create());
    }

    private String defaultReplaces(Player player, String text) {
        text = text.replace("$player", player.getDisplayName());
        text = text.replace("$realPlayer", player.getName());
        text = text.replace("$world", player.getWorld().getName());
        return text;
    }

    public void sendMessageWithReplaces(Player player, Language forcedLanguage,
            StringReplace... replaces) {
        String message = defaultReplaces(player, getText(forcedLanguage, replaces));
        player.sendMessage(TextComponent.fromLegacyText(message));
    }

    public void sendMessageWithPrefixWithReplaces(Player player, TranslatableMessage prefix,
            Language forcedLanguage, StringReplace... replaces) {
        ComponentBuilder cbuilder = new ComponentBuilder("");
        cbuilder.appendLegacy(prefix.getText(forcedLanguage));
        cbuilder.appendLegacy(defaultReplaces(player, getText(forcedLanguage, replaces)));
        player.sendMessage(cbuilder.create());
    }

    private String getText(Language forcedLanguage, StringReplace... replaces) {
        String text = null;
        if (translatedVersions.containsKey(forcedLanguage)) {
            text = translatedVersions.get(forcedLanguage);
        } else {
            text = translatedVersions.get(Language.ENGLISH);
        }
        if (text != null) {
            for (int i = 0; i < replaces.length; i++) {
                text = replaces[i].convert(text);
            }
        }
        return text;
    }

}
