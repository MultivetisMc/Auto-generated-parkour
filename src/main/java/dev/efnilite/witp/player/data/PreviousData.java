package dev.efnilite.witp.player.data;

import dev.efnilite.witp.WITP;
import dev.efnilite.witp.util.Logging;
import dev.efnilite.witp.util.Util;
import dev.efnilite.witp.util.Version;
import dev.efnilite.witp.util.config.Option;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class PreviousData {

    private double health;
    private double maxHealth;
    private InventoryData inventoryData;
    private final int hunger;
    private final Player player;
    private final GameMode gamemode;
    private final Location location;

    public PreviousData(@NotNull Player player) {
        this.player = player;
        this.gamemode = player.getGameMode();
        this.location = player.getLocation();
        this.hunger = player.getFoodLevel();

        if (Option.SAVE_STATS.get()) {
            this.health = player.getHealth();
            this.maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        }
        if (Option.HEALTH_HANDLING.get()) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        }
        if (Option.INVENTORY_HANDLING.get()) {
            this.inventoryData = new InventoryData(player);
            this.inventoryData.saveInventory();
            if (Option.INVENTORY_SAVING.get()) {
                this.inventoryData.saveFile();
            }
        }
        if (Version.isHigherOrEqual(Version.V1_13)) {
            for (PotionEffectType value : PotionEffectType.values()) {
                player.removePotionEffect(value);
            }
        }
    }

    public void apply(boolean teleportBack) {
        try {
            if (teleportBack) {
                if (Option.GO_BACK.get()) {
                    Location to = Util.parseLocation(WITP.getConfiguration().getString("config", "bungeecord.go-back"));
                    player.teleport(to);
                } else {
                    player.teleport(location);
                }
            }

            player.setFoodLevel(hunger);
            player.setGameMode(gamemode);

            // -= Attributes =-
            if (Option.SAVE_STATS.get() && Option.HEALTH_HANDLING.get()) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
                player.setHealth(health);
            }
        } catch (Throwable ex) {// not optimal but there isn't another way
            Logging.stack("Error while recovering stats of " + player.getName(),
                    "Please report this error to the developer! Inventory will still be restored.", ex);
        }
        if (inventoryData != null) {
            inventoryData.apply(false);
        }
    }
}
