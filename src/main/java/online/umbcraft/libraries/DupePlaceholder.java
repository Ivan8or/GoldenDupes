package online.umbcraft.libraries;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class DupePlaceholder extends PlaceholderExpansion {

    final private GoldenDupes plugin;

    public DupePlaceholder(GoldenDupes plugin) {
        this.plugin = plugin;
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
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer p, String s) {
        String placeholder = "INVALID_PLACEHOLDER";
        switch (s) {
            case "anvil":
                placeholder =
                        ((plugin.getAnvilDupe() != null) &&
                                (plugin.getAnvilDupe().getScheduler().isEnabled())
                                ? "enabled" : "disabled");
                break;
            case "autocraft":
                placeholder =
                        ((plugin.getAutocraftDupe() != null) &&
                                (plugin.getAutocraftDupe().getScheduler().isEnabled())
                                ? "enabled" : "disabled");
                break;
            case "donkey":
                placeholder =
                        ((plugin.getDonkeyDupe() != null) &&
                                (plugin.getDonkeyDupe().getScheduler().isEnabled())
                                ? "enabled" : "disabled");
                break;
            case "portal":
                placeholder =
                        ((plugin.getNetherPortalDupe() != null) &&
                                (plugin.getNetherPortalDupe().getScheduler().isEnabled())
                                ? "enabled" : "disabled");
                break;
        }
        return placeholder;
    }


}
