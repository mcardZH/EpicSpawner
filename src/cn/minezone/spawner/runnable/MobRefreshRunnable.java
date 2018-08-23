package cn.minezone.spawner.runnable;

import cn.minezone.spawner.builder.EntityBuilder;
import cn.minezone.spawner.craftspawner.Spawner;
import cn.minezone.spawner.craftspawner.SpawnerApi;
import cn.minezone.spawner.event.MobRefreshEvent;
import cn.minezone.spawner.event.SpawnerFreshFinishedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

/**
 * @author mcard
 */
public class MobRefreshRunnable extends BukkitRunnable {
    /**
     * String loc 用于记录配置文件位置
     * Integer 已经多少tick未刷新
     */
    private Map<String, Integer> lastUpdate = new HashMap<>();
    private int count = 0;
    private Plugin plugin;
    private YamlConfiguration config;

    public MobRefreshRunnable(Plugin plugin) {
        this.plugin = plugin;
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "block.yml"));
    }

    @Override
    public void run() {
        //每1秒重新读取一次配置文件
        count++;
        int refreshConfig = 20;
        if (count >= refreshConfig) {
            count = 0;
            config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "block.yml"));
        }
        for (String key : config.getKeys(false)) {
            if (lastUpdate.containsKey(key)) {
                int last = lastUpdate.get(key) + 1;
                Spawner s = SpawnerApi.getSpawner(config.getString(key + ".spawner"));
                if (s == null) {
                    continue;
                }
                if (last >= s.getTick()) {
                    //随机时间最大值
                    int c = s.getMaxTick() - last;
                    if (c > 1) {
                        int low = new Random().nextInt(c - 1);
                        if (low + 1 != 1 && low != 2) {
                            continue;
                        }
                    }

                    //要更新了
                    String[] temp = key.split(" ");
                    Location loc = new Location(Bukkit.getWorld(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3]));
                    if (loc.getBlock().getType() != Material.MOB_SPAWNER) {
                        SpawnerApi.unRegSpawnerBlock(loc.getBlock());
                        continue;
                    }
                    refresh(loc, s);
                    lastUpdate.remove(key);
                    lastUpdate.put(key, 0);
                } else {
                    lastUpdate.remove(key);
                    lastUpdate.put(key, last);
                }
            } else {
                lastUpdate.put(key, 0);
            }
        }
    }

    private void refresh(Location loc, Spawner s) {
        List<Entity> near = new ArrayList<>();
        int ra = s.getActivateRadius();
        for (Entity entity : loc.getWorld().getEntities()) {
            Location l = entity.getLocation();
            if (Math.abs(l.getX() - loc.getX()) <= ra && Math.abs(l.getY() - loc.getY()) <= ra && Math.abs(l.getZ() - loc.getZ()) <= ra) {
                near.add(entity);
            }
        }

        int mobNum = near.size();
        boolean player = false;
//        for (Entity entity : c) {
//            NBTEntity e = new NBTEntity(entity);
//            if (locationToString(loc).replace(" ", "").equals(e.getString("Name"))) {
//                mobNum++;
//            }
//        }
        //原来使用getNearByEntities，但是1710没有这个方法
        for (Entity entity : near) {
            if (entity instanceof Player) {
                player = true;
                mobNum--;
            }
        }
        if (!player) {
            return;
        }
        MobRefreshEvent event;
        for (EntityBuilder spawnEntity : s.getSpawnEntities()) {
            spawnEntity.setLocation(loc.clone().add(
                    new Random().nextInt(s.getActivateRadius() * 2) - s.getActivateRadius(),
                    new Random().nextInt(s.getActivateRadius() * 2) - s.getActivateRadius(),
                    new Random().nextInt(s.getActivateRadius() * 2) - s.getActivateRadius()));
        }
        if (mobNum >= s.getMaxMobAlive()) {
            //已经存在足够多的生物，默认此次刷新被取消。
            event = new MobRefreshEvent(loc, s);
            event.setCancelled(true);
        } else {
            event = new MobRefreshEvent(loc, s);
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        List<Entity> entities = new ArrayList<>();
        for (EntityBuilder spawnEntity : event.getSpawner().getSpawnEntities()) {
            entities.add(spawnEntity.spawn());
        }
        for (ItemStack itemStack : event.getSpawner().getSpawnItem()) {
            //随机位置
            Location location = loc.clone().add(
                    new Random().nextInt(s.getActivateRadius() * 2) - s.getActivateRadius(),
                    new Random().nextInt(s.getActivateRadius() * 2) - s.getActivateRadius(),
                    new Random().nextInt(s.getActivateRadius() * 2) - s.getActivateRadius());
            entities.add(loc.getWorld().dropItemNaturally(location, itemStack));
        }
        for (String cmd : event.getSpawner().getSpawnCommand()) {
            String r = cmd.replace("%x", loc.getBlockX() + "");
            r = r.replace("%y", loc.getBlockY() + "");
            r = r.replace("%z", loc.getBlockZ() + "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), r);
        }
        Bukkit.getPluginManager().callEvent(new SpawnerFreshFinishedEvent(loc.getBlock(), s, entities));
    }

    private static String locationToString(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
}
