package cn.minezone.spawner.commands;

import cn.minezone.spawner.SpawnerPlugin;
import cn.minezone.spawner.builder.ItemBuilder;
import cn.minezone.spawner.craftspawner.Spawner;
import cn.minezone.spawner.craftspawner.SpawnerApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author mcard
 */
public class CommandHandler implements TabExecutor {

    private static final char ALT_COLOR_CHAR = '&';
    private FileConfiguration config;

    public CommandHandler(Plugin plugin) {
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args == null || args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            help(sender, args);
            return true;
        }
        try {
            switch (args[0]) {
                case "create": {
                    create(sender, args);
                    break;
                }
                case "get": {
                    get(sender, args);
                    break;
                }
                case "delete": {
                    delete(sender, args);
                    break;
                }
                case "edit": {
                    edit(sender, args);
                    break;
                }
                case "list": {
                    list(sender, args);
                    break;
                }
                case "reload": {
                    reload(sender, args);
                    break;
                }
                default: {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR,
                            config.getString("languages.arguments-error", "languages.arguments-error")));
                }
            }
        } catch (Exception e) {
            if (sender.isOp()) {
                //捕获调试信息
                StackTraceElement[] messages = e.getStackTrace();
                int length = messages.length;
                //简化调试信息，内部错误就忽略掉
                List<String> blackList = Arrays.asList("mojang", "bukkit", "spigot", "minecraft");
                boolean skip;
                sender.sendMessage(ChatColor.GREEN + "错误原因：" + ChatColor.RED + e.getClass().getName() + " " + e.getMessage());
                sender.sendMessage(ChatColor.GREEN + "错误详细日志:");
                for (StackTraceElement message : messages) {
                    skip = false;
                    for (String bl : blackList) {
                        if (message.getClassName().contains(bl)) {
                            skip = true;
                            break;
                        }
                    }
                    if (!skip) {
                        sender.sendMessage(ChatColor.RED + message.toString());
                    }

                }
            } else {
                sender.sendMessage(ChatColor.RED + "插件运行时发生错误，请联系管理员！");
            }
        }


        return true;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args == null || args.length == 0 || args.length == 1) {
            if (sender.isOp()) {
                return Arrays.asList("help", "create", "get", "delete", "edit", "reload");
            }
            return null;
        }
        if ("edit".equalsIgnoreCase(args[0]) || "delete".equalsIgnoreCase(args[0])) {
            if (sender.isOp()) {
                List<String> names = new ArrayList<>();
                for (Spawner spawner : SpawnerApi.getRegisteredSpawner()) {
                    names.add(spawner.getName());
                }
                return names;
            }
        }
        return null;
    }

    private void list(CommandSender s, String[] args) {
        if (!s.hasPermission("epic.spawner.list")) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.no-permission")));
            return;
        }
        for (Spawner spawner : SpawnerApi.getRegisteredSpawner()) {
            s.sendMessage(spawner.getName() + "(" + spawner.getDisplayName() + "§r)");
        }
    }

    private void help(CommandSender s, String[] args) {
        for (String help : config.getStringList("languages.help")) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, help));
        }
    }

    private void create(CommandSender s, String[] args) {
        if (!s.hasPermission("epic.spawner.create")) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.no-permission")));
            return;
        }
        if (args.length != 2) {
            lengthError(s);
            return;
        }
        if (SpawnerApi.isSpawnerExist(args[1])) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.spawner-exist")));
            return;
        }
        SpawnerApi.createSpawner(args[1]);
        Bukkit.dispatchCommand(s, "es edit " + args[1]);
        s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.create-success")));
    }

    private void get(CommandSender s, String[] args) {
        if (!s.hasPermission("epic.spawner.get")) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.no-permission")));
            return;
        }
        if (args.length != 2 && args.length != 3) {
            lengthError(s);
            return;
        }
        if (!SpawnerApi.isSpawnerExist(args[1])) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.spawner-isnt-exist")));
            return;
        }
        if (args.length == 2) {
            if (!(s instanceof Player)) {
                s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.sender-isnt-player")));
                return;
            }
            Player p = (Player) s;
            p.getInventory().addItem(SpawnerApi.getSpawner(args[1]).getSpawnerItem());
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.give-success")));
        } else {
            Player p = Bukkit.getPlayerExact(args[2]);
            if (p == null || !p.isOnline()) {
                s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.player-isnt-exist")));
                return;
            }
            p.getInventory().addItem(SpawnerApi.getSpawner(args[1]).getSpawnerItem());
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.give-success")));
        }
    }

    private void delete(CommandSender s, String[] args) throws IOException {
        if (!s.hasPermission("epic.spawner.delete")) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.no-permission")));
            return;
        }
        if (args.length != 2) {
            lengthError(s);
            return;
        }
        if (!SpawnerApi.isSpawnerExist(args[1])) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.spawner-isnt-exist")));
            return;
        }
        SpawnerApi.deleteSpawner(args[1]);
        s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.delete-success")));
    }

    private void reload(CommandSender s, String[] args) {
        if (!s.hasPermission("epic.spawner.reload")) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.no-permission")));
            return;
        }
        if (args.length != 1) {
            lengthError(s);
            return;
        }
        SpawnerApi.reloadAllConfig();
        Bukkit.getPluginManager().getPlugin(SpawnerPlugin.PLUGIN_NAME).reloadConfig();
        s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.reload-success")));
    }

    private void edit(CommandSender s, String[] args) {
        if (!s.hasPermission("epic.spawner.edit")) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.no-permission")));
            return;
        }
        if (args.length != 2) {
            lengthError(s);
            return;
        }
        if (!SpawnerApi.isSpawnerExist(args[1])) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.spawner-isnt-exist")));
            return;
        }
        if (!(s instanceof Player)) {
            s.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.sender-isnt-player")));
            return;
        }
        Player p = (Player) s;
        Inventory inv = Bukkit.createInventory(p, 27, ChatColor.GREEN + "" + ChatColor.BOLD + args[1]);
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(" ");
        item.setItemMeta(im);
        for (Integer integer : Arrays.asList(0, 1, 2, 4, 6, 7, 8, 9, 13, 17, 18, 19, 20, 21, 23, 24, 25, 26)) {
            inv.setItem(integer, item);
        }
        Spawner spawner = SpawnerApi.getSpawner(args[1]);
        item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta sm = (SkullMeta) item.getItemMeta();
        sm.setOwner(p.getName());
        sm.setDisplayName("§a设置玩家激活半径");
        sm.setLore(Collections.singletonList("§a当前半径：§r" + spawner.getActivateRadius()));
        item.setItemMeta(sm);
        inv.setItem(3, item);
        item = new ItemStack(Material.ENCHANTMENT_TABLE);
        im = item.getItemMeta();
        im.setDisplayName("§a设置实体最大数量");
        im.setLore(Collections.singletonList("§a当前数量：§r" + spawner.getMaxMobAlive()));
        item.setItemMeta(im);
        inv.setItem(5, item);
        inv.setItem(10, new ItemBuilder(Material.NAME_TAG).setItemDisplayName("§a设置物品名称").addItemLore("§3当前名称：§r" + spawner.getDisplayName()).create());
        inv.setItem(11, new ItemBuilder(Material.BLAZE_ROD).setItemDisplayName("§a设置物品Lore")
                .addItemLore("§c当前Lore").addItemLore("").addItemLore(spawner.getLore().toArray(new String[0])).create());
        inv.setItem(12, new ItemBuilder(Material.MOB_SPAWNER).setItemDisplayName("§a设置刷怪笼生物").addItemLore("§c就是刷怪笼里面转的生物").addItemLore("§a当前类型： §l§b" + spawner.getSpawnerEntityType().name()).create());

        inv.setItem(14, new ItemBuilder(Material.DIAMOND).setItemDisplayName("§a设置刷新物品").addItemLore("§b当前有§a" + spawner.getSpawnItem().size() + "§b种物品").create());
        item = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
        im = item.getItemMeta();
        im.setDisplayName("§a设置生物刷新");
        im.setLore(Collections.singletonList("§b当前有§a" + spawner.getSpawnEntities().size() + "§b种生物"));
        item.setItemMeta(im);
        inv.setItem(15, item);
        inv.setItem(16, new ItemBuilder(Material.SIGN).setItemDisplayName("§a设置执行指令").addItemLore("§b当前有§a" + spawner.getSpawnCommand().size() + "§b条指令").create());
        inv.setItem(22, new ItemBuilder(Material.WATCH).setItemDisplayName("§a设置刷新间隔")
                .addItemLore("§a当前间隔最小时间：§b" + spawner.getTick()).addItemLore("§a当前间隔最大时间：§b" + spawner.getMaxTick())
                .addItemLore("§b单位：tick，20ticks = 1s").create());
        p.closeInventory();
        p.openInventory(inv);
    }

    private void lengthError(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR,
                config.getString("languages.arguments-length", "languages.arguments-length")));
    }
}
