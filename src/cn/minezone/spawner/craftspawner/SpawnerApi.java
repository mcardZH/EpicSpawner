package cn.minezone.spawner.craftspawner;

import cn.minezone.spawner.SpawnerPlugin;
import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author mcard
 */
public final class SpawnerApi {

    private static YamlConfiguration spawnerConfig;
    private static YamlConfiguration blockConfig;
    private static File file;
    private static File blockFile;
    private static Plugin plugin;

    /**
     * 无需调用，插件启动时调用一次
     */
    public static void initialize() {
        if (plugin == null) {
            plugin = Bukkit.getPluginManager().getPlugin(SpawnerPlugin.PLUGIN_NAME);
        }
        if (file == null) {
            file = new File(plugin.getDataFolder(), "spawner.yml");
        }
        if (blockFile == null) {
            blockFile = new File(plugin.getDataFolder(), "block.yml");
        }
        if (spawnerConfig == null) {
            spawnerConfig = YamlConfiguration.loadConfiguration(file);
        }
        if (blockConfig == null) {
            blockConfig = YamlConfiguration.loadConfiguration(blockFile);
        }
    }

    /**
     * 建立一个新的刷怪笼
     *
     * @param name 名称（唯一）
     * @return 名称存在返回null，否则返回刷挂笼对象并在配置中注册。
     */
    public static Spawner createSpawner(String name) {
        if (isSpawnerExist(name)) {
            return null;
        }
        Spawner spawner = new CraftSpawner(name);
        try {
            saveSpawner(spawner);
        } catch (IOException e) {
            return null;
        }
        return spawner;
    }

    /**
     * 删除一个刷挂笼
     *
     * @param name 名称
     */
    public static void deleteSpawner(String name) throws IOException {
        spawnerConfig.set(name, null);
        spawnerConfig.save(file);
    }

    /**
     * 获取全部已经注册的刷怪笼
     *
     * @return 刷怪笼集合
     */
    public static Set<Spawner> getRegisteredSpawner() {
        Set<Spawner> spawner = new HashSet<>();
        for (String key : spawnerConfig.getKeys(false)) {
            spawner.add((Spawner) spawnerConfig.get(key));
        }
        return spawner;
    }

    /**
     * 保存刷怪笼配置（修改后务必调用）
     *
     * @param spawner 刷怪笼
     * @throws IOException 保存出错
     */
    public static void saveSpawner(Spawner spawner) throws IOException {
        spawnerConfig.set(spawner.getName(), spawner);
        if (file == null) {
            initialize();
            System.out.print("fill null？这是不应该发生的");
        }
        if (spawnerConfig == null) {
            initialize();
            System.out.print("config null？这是不应该发生的");
        }
        spawnerConfig.save(file);
    }

    /**
     * 获取一个刷怪笼对象
     *
     * @param name 名称
     * @return 存在返回该对象，不存在返回null
     */
    public static Spawner getSpawner(String name) {
        if (name == null) {
            return null;
        }
        try {
            return (Spawner) spawnerConfig.get(name, null);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 判断一个自定义刷怪笼是否存在
     *
     * @param name 刷怪笼名称
     * @return true存在，false不存在
     */
    public static boolean isSpawnerExist(String name) {
        return spawnerConfig.contains(name);
    }

    /**
     * 在配置文件中注册一个刷怪笼方块
     *
     * @param block   方块
     * @param owner   放置者
     * @param spawner 对应的刷怪笼名称
     * @return 成功返回true，失败返回false（主要出现在提供的block非刷怪笼）
     */
    public static boolean regSpawnerBlock(OfflinePlayer owner, Block block, String spawner) {
        if (!isSpawnerExist(spawner)) {
            return false;
        }
        if (block.getType() != Material.MOB_SPAWNER) {
            return false;
        }
        blockConfig.set(locationToString(block.getLocation()) + ".owner", owner.getUniqueId().toString());
        blockConfig.set(locationToString(block.getLocation()) + ".spawner", spawner);
        try {
            blockConfig.save(blockFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 通过事件来注册刷怪笼
     *
     * @param event 事件
     * @return 成功返回true，失败返回false（主要出现在放置错误的方块）
     */
    public static boolean regSpawnerBlockByEvent(BlockPlaceEvent event) {
        if (Bukkit.getPluginManager().getPlugin(SpawnerPlugin.PLUGIN_NAME).getConfig().getBoolean("lore-mode", true)) {
            String s = ChatColor.stripColor(event.getItemInHand().getItemMeta().getLore().get(event.getItemInHand().getItemMeta().getLore().size() - 1));
            return regSpawnerBlock(event.getPlayer(), event.getBlock(), s);
        } else {
            return regSpawnerBlock(event.getPlayer(), event.getBlock(), new NBTItem(event.getItemInHand()).getString("spawnerName"));
        }

    }

    /**
     * 反注册刷怪笼
     *
     * @param block 刷怪笼方块
     */
    public static void unRegSpawnerBlock(Block block) {
        blockConfig.set(locationToString(block.getLocation()), null);
        try {
            blockConfig.save(blockFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重载刷怪笼列表、刷怪笼方块列表
     */
    public static void reloadAllConfig() {
        blockConfig = null;
        blockFile = null;
        file = null;
        spawnerConfig = null;
        initialize();
    }

    /**
     * 获取刷怪笼方块的所有者
     *
     * @param loc 方块坐标
     * @return 有这个对象则返回Owner的UUID，否则返回空文本
     */
    public static String getSpawnerBlockOwner(Location loc) {
        return blockConfig.getString(locationToString(loc) + ".owner", "");
    }

    /**
     * 获取刷怪笼方块的刷怪笼对象
     *
     * @param loc 方块坐标
     * @return 有这个对象则返回对应的名称，否则返回空文本
     */
    public static String getSpawnerBlockSpawner(Location loc) {
        return blockConfig.getString(locationToString(loc) + ".spawner", "");
    }

    /**
     * 设置刷怪笼的所有者
     *
     * @param loc    位置
     * @param player 新所有者
     * @return 设置结果
     */
    public static boolean setSpawnerBlockOwner(Location loc, OfflinePlayer player) {
        return regSpawnerBlock(player, loc.getBlock(), getSpawnerBlockSpawner(loc));
    }

    /**
     * 设置刷怪笼的新对象
     *
     * @param loc     位置
     * @param spawner 新对象
     * @return 设置结果
     */
    public static boolean setSpawnerBlockSpawner(Location loc, Spawner spawner) {
        return regSpawnerBlock(Bukkit.getOfflinePlayer(UUID.fromString(getSpawnerBlockOwner(loc))), loc.getBlock(), spawner.getName());
    }

    private static String locationToString(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
}
