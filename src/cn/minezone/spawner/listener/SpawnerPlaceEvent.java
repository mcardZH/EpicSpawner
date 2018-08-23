package cn.minezone.spawner.listener;

import cn.minezone.spawner.craftspawner.SpawnerApi;
import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author mcard
 */
public class SpawnerPlaceEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }
        String spawnerName = new NBTItem(event.getItemInHand()).getString("spawnerName");
        //注册方块
        if (spawnerName == null || "".equals(spawnerName)) {
            return;
        }
        SpawnerApi.regSpawnerBlockByEvent(event);
        CreatureSpawner s = (CreatureSpawner) event.getBlock().getState();
        s.setSpawnedType(SpawnerApi.getSpawner(spawnerName).getSpawnerEntityType());
    }

}