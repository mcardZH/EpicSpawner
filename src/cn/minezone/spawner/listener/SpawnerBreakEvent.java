package cn.minezone.spawner.listener;

import cn.minezone.spawner.craftspawner.SpawnerApi;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * @author mcard
 */
public class SpawnerBreakEvent implements Listener {


    private static final char COLOR = '&';
    private FileConfiguration config;

    public SpawnerBreakEvent(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }
        if ("".equals(SpawnerApi.getSpawnerBlockSpawner(e.getBlock().getLocation()))) {
            return;
        }
        String path1 = "Only-owner-can-break";
        String path2 = "Only-owner-can-break-tip";
        String path3 = "Drop-break-spawner";
        if (config.getBoolean(path1)) {
            if (!SpawnerApi.getSpawnerBlockOwner(e.getBlock().getLocation()).equals(e.getPlayer().getUniqueId().toString()) && !e.getPlayer().isOp()) {
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes(COLOR, config.getString(path2)));
                e.setCancelled(true);
                return;
            }
        }
        if (config.getBoolean("Drop-break-spawner")) {
            e.getBlock().getLocation().getWorld().dropItemNaturally(
                    e.getBlock().getLocation(), SpawnerApi.getSpawner(SpawnerApi.getSpawnerBlockSpawner(e.getBlock().getLocation())).getSpawnerItem());
        }
        SpawnerApi.unRegSpawnerBlock(e.getBlock());

    }


}
