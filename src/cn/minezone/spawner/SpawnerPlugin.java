package cn.minezone.spawner;

import cn.minezone.spawner.builder.EntityBuilder;
import cn.minezone.spawner.commands.CommandHandler;
import cn.minezone.spawner.craftspawner.CraftSpawner;
import cn.minezone.spawner.craftspawner.SpawnerApi;
import cn.minezone.spawner.input.PlayerInputUtil;
import cn.minezone.spawner.listener.*;
import cn.minezone.spawner.runnable.MobRefreshRunnable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author mcard
 */
public class SpawnerPlugin extends JavaPlugin {

    public static final String PLUGIN_NAME = "EpicSpawner";

    @Override
    public void onEnable() {

        //保存默认配置
        saveResource("block.yml", false);
        saveResource("spawner.yml", false);
        saveDefaultConfig();

        //注册监听器
        Bukkit.getPluginManager().registerEvents(new MobSpawnEvent(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnerPlaceEvent(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnerBreakEvent(getConfig()), this);
        Bukkit.getPluginManager().registerEvents(new EditorClickEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInputHelper(), this);

        Bukkit.getPluginManager().registerEvents(new PlayerInputUtil(), this);

        //注册可存储配置的类
        ConfigurationSerialization.registerClass(CraftSpawner.class);
        ConfigurationSerialization.registerClass(EntityBuilder.class);

        //初始化API
        SpawnerApi.initialize();

        //初始化命令
        Bukkit.getPluginCommand("epicspawner").setExecutor(new CommandHandler(this));
        Bukkit.getPluginCommand("epicspawner").setTabCompleter(new CommandHandler(this));

        //启动调度器
        new MobRefreshRunnable(this).runTaskTimer(this, 0, 1);
    }
}
