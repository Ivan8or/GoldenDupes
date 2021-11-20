package online.umbcraft.libraries;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import online.umbcraft.libraries.config.ConfigPath;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class DupePlaceholder extends PlaceholderExpansion {

    final private GoldenDupes plugin;
    final private String enabled;
    final private String disabled;

    public DupePlaceholder(GoldenDupes plugin) {
        this.plugin = plugin;
        enabled = plugin.getConfig().getString(ConfigPath.PLACEHOLDER_ENABLED_TEXT.path());
        disabled = plugin.getConfig().getString(ConfigPath.PLACEHOLDER_DISABLED_TEXT.path());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "goldendupes";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ivan8or";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer p, String s) {
        String placeholder = "INVALID_PLACEHOLDER";
        switch (s) {
            case "anvil_status":
                placeholder =
                        ((plugin.getAnvilDupe() != null) &&
                                (plugin.getAnvilDupe().getScheduler().isEnabled())
                                ? enabled : disabled);
                break;
            case "autocraft_status":
                placeholder =
                        ((plugin.getAutocraftDupe() != null) &&
                                (plugin.getAutocraftDupe().getScheduler().isEnabled())
                                ? enabled : disabled);
                break;
            case "donkey_status":
                placeholder =
                        ((plugin.getDonkeyDupe() != null) &&
                                (plugin.getDonkeyDupe().getScheduler().isEnabled())
                                ? enabled : disabled);
                break;
            case "portal_status":
                placeholder =
                        ((plugin.getNetherPortalDupe() != null) &&
                                (plugin.getNetherPortalDupe().getScheduler().isEnabled())
                                ? enabled : disabled);
                break;
        }
        return placeholder;
    }


}
