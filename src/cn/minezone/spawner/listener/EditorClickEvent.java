package cn.minezone.spawner.listener;

import cn.minezone.spawner.builder.EntityBuilder;
import cn.minezone.spawner.builder.ItemBuilder;
import cn.minezone.spawner.craftspawner.Spawner;
import cn.minezone.spawner.craftspawner.SpawnerApi;
import cn.minezone.spawner.input.PlayerInputCallBack;
import cn.minezone.spawner.input.PlayerInputUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
public class EditorClickEvent implements Listener, PlayerInputCallBack {

    private static final char ALT_COLOR_CHAR = '&';
    private FileConfiguration config;
    private Plugin plugin;

    public EditorClickEvent(Plugin plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        String title = e.getInventory().getTitle();
        String[] titles;
        if (title.contains(" - ")) {
            titles = title.split(" - ");
        } else {
            titles = (title + " - ").split(" - ");
        }
        Spawner s = SpawnerApi.getSpawner(ChatColor.stripColor(titles[0]));
        if (titles.length == 2 && "Item".equalsIgnoreCase(titles[1])) {
            //关闭并保存所有物品
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < 45; i++) {
                int raw;
                if (i >= 10 && i <= 16) {
                    raw = i - 10;
                } else if (i >= 19 && i <= 25) {
                    raw = i - 12;
                } else if (i >= 28 && i <= 34) {
                    raw = i - 14;
                } else if (i >= 37 && i <= 43) {
                    raw = i - 16;
                } else {
                    raw = -1;
                }
                if (raw == -1) {
                    continue;
                }
                if (e.getInventory().getItem(i) != null) {
                    items.add(e.getInventory().getItem(i).clone());
                }
            }
            s.setSpawnItems(items);
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().getTitle().startsWith(ChatColor.GREEN + "" + ChatColor.BOLD)) {
            return;
        }
        String title = e.getInventory().getTitle();
        String[] titles;
        if (title.contains(" - ")) {
            titles = title.split(" - ");
        } else {
            titles = (title + " - ").split(" - ");
        }

        if (!SpawnerApi.isSpawnerExist(ChatColor.stripColor(titles[0]))) {
            return;
        }
        if (!e.getWhoClicked().equals(e.getInventory().getHolder())) {
            return;
        }
        if (e.getInventory() != e.getClickedInventory()) {
            //点自己背包不管
            return;
        }
        if (titles.length == 1) {
            e.setCancelled(true);
            main(e);
            return;
        }
        if ("Lore".equals(titles[1])) {
            e.setCancelled(true);
            lore(e, ChatColor.stripColor(titles[0]));
            return;
        }
        if ("Item".equals(titles[1])) {
            item(e, ChatColor.stripColor(titles[0]));
            return;
        }
        if ("Entities".equals(titles[1])) {
            e.setCancelled(true);
            entities(e, ChatColor.stripColor(titles[0]));
        }
        if ("Commands".equals(titles[1])) {
            e.setCancelled(true);
            commands(e, ChatColor.stripColor(titles[0]));
        }


    }

    private void commands(InventoryClickEvent e, String spawner) {
        if (e.getSlot() == 0) {
            //返回
            Bukkit.dispatchCommand((Player) e.getWhoClicked(), "epicspawner edit " + spawner);
            return;
        }
        Spawner s = SpawnerApi.getSpawner(spawner);
        if (e.getSlot() == 46) {
            //清空全部
            s.setLore(new ArrayList<>());
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.do-success", "languages.do-success")));
            Bukkit.dispatchCommand((Player)e.getWhoClicked(), "epicspawner edit " + spawner);
            return;
        }
        if (e.getSlot() == 52) {
            //增加一条
            ((Player) e.getWhoClicked()).sendMessage("§a请输入指令，无需带/");
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "addCommand", e.getInventory(), spawner, e);
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
            return;
        }
        if (e.getInventory().getItem(e.getSlot()) == null) {
            return;
        }
        int raw;
        if (e.getSlot() >= 10 && e.getSlot() <= 16) {
            raw = e.getSlot() - 10;
        } else if (e.getSlot() >= 19 && e.getSlot() <= 25) {
            raw = e.getSlot() - 12;
        } else if (e.getSlot() >= 28 && e.getSlot() <= 34) {
            raw = e.getSlot() - 14;
        } else if (e.getSlot() >= 37 && e.getSlot() <= 43) {
            raw = e.getSlot() - 16;
        } else {
            raw = -1;
        }

        if (raw == -1) {
            return;
        }
        List<String> cmd = s.getSpawnCommand();
        cmd.remove(raw);
        s.setSpawnCommand(cmd);
        try {
            SpawnerApi.saveSpawner(s);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle());
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(" ");
        item.setItemMeta(im);
        for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
            inv.setItem(integer, item);
        }
        inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
        inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
        inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l新增命令").addItemLore("§a注意：命令将由控制台执行").create());

        int count = 10;

        for (int i = 0; i < s.getSpawnCommand().size() && i < 28; i++) {
            inv.setItem(count, new ItemBuilder(Material.NAME_TAG).setItemDisplayName(s.getSpawnCommand().get(i)).addItemLore("§a点击移除此行").create());
            count++;
            if ((count + 1) % 9 == 0) {
                count += 2;
            }
        }

        if (s.getSpawnCommand().size() > 28) {
            inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§cLore过多，部分无法显示！").addItemLore("§c共" + (s.getSpawnCommand().size() - 28) + "条无法显示").create());
        }

        e.getWhoClicked().openInventory(inv);
    }

    private void entities(InventoryClickEvent e, String spawner) {
        Spawner s = SpawnerApi.getSpawner(spawner);
        if (e.getSlot() == 46) {
            //清空全部
            s.setSpawnEntitiesByEntityBuilder(new ArrayList<>());
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.do-success", "languages.do-success")));
            Bukkit.dispatchCommand((Player)e.getWhoClicked(), "epicspawner edit " + spawner);
            return;
        }
        if (e.getSlot() == 0) {
            //返回
            Bukkit.dispatchCommand((Player)e.getWhoClicked(), "epicspawner edit " + spawner);
            return;
        }
        if (e.getSlot() == 52) {
            StringBuilder now = new StringBuilder("§a目前支援的类型有：");
            for (EntityType value : EntityType.values()) {
                if (value.isSpawnable()) {
                    now.append(value.name()).append(", ");
                }
            }
            ((Player) e.getWhoClicked()).sendMessage(now.toString());
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.please-input-text", "languages.please-input-text")));
            ((Player) e.getWhoClicked()).sendMessage("§e输入方式：[生物类型(上面这些)] [生物名称(生成时的名称)] \n后者可以省略，两者中间有一个空格");
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "addEntity", e.getInventory(), spawner, e);
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
            return;
        }
        int raw;
        if (e.getSlot() >= 10 && e.getSlot() <= 16) {
            raw = e.getSlot() - 10;
        } else if (e.getSlot() >= 19 && e.getSlot() <= 25) {
            raw = e.getSlot() - 12;
        } else if (e.getSlot() >= 28 && e.getSlot() <= 34) {
            raw = e.getSlot() - 14;
        } else if (e.getSlot() >= 37 && e.getSlot() <= 43) {
            raw = e.getSlot() - 16;
        } else {
            raw = -1;
        }

        if (raw == -1) {
            return;
        }
        List<EntityBuilder> builders = s.getSpawnEntities();
        builders.remove(raw);
        s.setSpawnEntitiesByEntityBuilder(builders);
        try {
            SpawnerApi.saveSpawner(s);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle());
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(" ");
        item.setItemMeta(im);
        for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
            inv.setItem(integer, item);
        }
        inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
        inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
        inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l增加生物").create());

        int count = 10;

        for (int i = 0; i < s.getSpawnEntities().size() && i < 28; i++) {
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta skull = (SkullMeta) item.getItemMeta();
            skull.setDisplayName("".equals(s.getSpawnEntities().get(i).getName()) ? " " : s.getSpawnEntities().get(i).getName());
            if ("".equals(s.getSpawnEntities().get(i).getName())) {
                skull.setLore(Arrays.asList("§a§l类型：" + s.getSpawnEntities().get(i).getType().name(), "§c未设置名称"));
            } else {
                skull.setLore(Collections.singletonList("§a§l类型：" + s.getSpawnEntities().get(i).getType().name()));
            }
            skull.setOwner(captureName(s.getSpawnEntities().get(i).getType().name().toLowerCase()));
            item.setItemMeta(skull);
            inv.setItem(count, item);
            count++;
            if ((count + 1) % 9 == 0) {
                count += 2;
            }
        }
        if (s.getSpawnEntities().size() > 28) {
            inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c生物过多，部分无法显示！").addItemLore("§c共" + (s.getSpawnEntities().size() - 28) + "条无法显示").create());
        }
        e.getWhoClicked().openInventory(inv);
    }

    private void item(InventoryClickEvent e, String spawner) {
        if (e.getSlot() <= 9 || e.getSlot() == 17 || e.getSlot() == 18 || e.getSlot() == 26 || e.getSlot() == 27 || e.getSlot() == 35 || e.getSlot() == 36 || e.getSlot() >= 44) {
            e.setCancelled(true);
        }
        if (e.getSlot() == 46) {
            //清空全部
            Spawner s = SpawnerApi.getSpawner(spawner);
            s.setSpawnItems(new ArrayList<>());
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            for (int i = 0; i < e.getInventory().getSize(); i++) {
                e.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.do-success", "languages.do-success")));
            Bukkit.getScheduler().runTask(plugin, () -> {
                e.getWhoClicked().closeInventory();
                Bukkit.dispatchCommand((Player)e.getWhoClicked(), "epicspawner edit " + spawner);
            });
            return;
        }
        if (e.getSlot() == 0) {
            //返回
            String title = e.getInventory().getTitle();
            String[] titles;
            if (title.contains(" - ")) {
                titles = title.split(" - ");
            } else {
                titles = (title + " - ").split(" - ");
            }
            Spawner s = SpawnerApi.getSpawner(ChatColor.stripColor(titles[0]));
            if (titles.length == 2 && "Item".equalsIgnoreCase(titles[1])) {
                //关闭并保存所有物品
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < 45; i++) {
                    int raw;
                    if (i >= 10 && i <= 16) {
                        raw = i - 10;
                    } else if (i >= 19 && i <= 25) {
                        raw = i - 12;
                    } else if (i >= 28 && i <= 34) {
                        raw = i - 14;
                    } else if (i >= 37 && i <= 43) {
                        raw = i - 16;
                    } else {
                        raw = -1;
                    }
                    if (raw == -1) {
                        continue;
                    }
                    if (e.getInventory().getItem(i) != null) {
                        items.add(e.getInventory().getItem(i).clone());
                    }
                }
                s.setSpawnItems(items);
                try {
                    SpawnerApi.saveSpawner(s);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            Bukkit.dispatchCommand((Player)e.getWhoClicked(), "epicspawner edit " + spawner);
            return;
        }
        if (e.getSlot() == 52) {
            //保存
            Spawner s = SpawnerApi.getSpawner(ChatColor.stripColor(spawner));
            List<ItemStack> items = new ArrayList<>();
            //关闭并保存所有物品
            for (int i = 0; i < 45; i++) {
                int raw;
                if (i >= 10 && i <= 16) {
                    raw = i - 10;
                } else if (i >= 19 && i <= 25) {
                    raw = i - 12;
                } else if (i >= 28 && i <= 34) {
                    raw = i - 14;
                } else if (i >= 37 && i <= 43) {
                    raw = i - 16;
                } else {
                    raw = -1;
                }
                if (raw == -1) {
                    continue;
                }
                if (e.getInventory().getItem(i) != null) {
                    items.add(e.getInventory().getItem(i).clone());
                }

            }
            s.setSpawnItems(items);
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    private void lore(InventoryClickEvent e, String spawner) {
        if (e.getSlot() == 0) {
            //返回
            Bukkit.dispatchCommand((Player)e.getWhoClicked(), "epicspawner edit " + spawner);
            return;
        }
        Spawner s = SpawnerApi.getSpawner(spawner);
        if (e.getSlot() == 46) {
            //清空全部
            s.setLore(new ArrayList<>());
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.do-success", "languages.do-success")));
            Bukkit.dispatchCommand((Player)e.getWhoClicked(), "epicspawner edit " + spawner);
            return;
        }
        if (e.getSlot() == 52) {
            //增加一条
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.please-input-text", "languages.please-input-text")));
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "addLore", e.getInventory(), spawner, e);
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
            return;
        }
        if (e.getInventory().getItem(e.getSlot()) == null) {
            return;
        }
        int raw;
        if (e.getSlot() >= 10 && e.getSlot() <= 16) {
            raw = e.getSlot() - 10;
        } else if (e.getSlot() >= 19 && e.getSlot() <= 25) {
            raw = e.getSlot() - 12;
        } else if (e.getSlot() >= 28 && e.getSlot() <= 34) {
            raw = e.getSlot() - 14;
        } else if (e.getSlot() >= 37 && e.getSlot() <= 43) {
            raw = e.getSlot() - 16;
        } else {
            raw = -1;
        }

        if (raw == -1) {
            return;
        }
        List<String> lore = s.getLore();
        lore.remove(raw);
        s.setLore(lore);
        try {
            SpawnerApi.saveSpawner(s);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle());
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(" ");
        item.setItemMeta(im);
        for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
            inv.setItem(integer, item);
        }
        inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
        inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
        inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l新增Lore").create());

        int count = 10;

        for (int i = 0; i < s.getLore().size() && i < 28; i++) {
            inv.setItem(count, new ItemBuilder(Material.NAME_TAG).setItemDisplayName(s.getLore().get(i)).addItemLore("§a点击移除此行").create());
            count++;
            if ((count + 1) % 9 == 0) {
                count += 2;
            }
        }

        if (s.getLore().size() > 28) {
            inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§cLore过多，部分无法显示！").addItemLore("§c共" + (s.getLore().size() - 28) + "条无法显示").create());
        }

        e.getWhoClicked().openInventory(inv);
    }

    private void main(InventoryClickEvent e) {
        if (e.getSlot() == 10) {
            //设置名称
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.please-input-text", "languages.please-input-text")));
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "setTitle",
                    e.getInventory(), ChatColor.stripColor(e.getInventory().getTitle()));
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
            return;
        }
        Spawner s = SpawnerApi.getSpawner(ChatColor.stripColor(e.getInventory().getTitle()));
        if (e.getSlot() == 11) {
            //设置Lore
            Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle() + " - Lore");
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(" ");
            item.setItemMeta(im);
            for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
                inv.setItem(integer, item);
            }
            inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
            inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
            inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l新增Lore").create());

            int count = 10;

            for (int i = 0; i < s.getLore().size() && i < 28; i++) {
                inv.setItem(count, new ItemBuilder(Material.NAME_TAG).setItemDisplayName(s.getLore().get(i)).addItemLore("§a点击移除此行").create());
                count++;
                if ((count + 1) % 9 == 0) {
                    count += 2;
                }
            }

            if (s.getLore().size() > 28) {
                inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§cLore过多，部分无法显示！").addItemLore("§c共" + (s.getLore().size() - 28) + "条无法显示").create());
            }

            e.getWhoClicked().openInventory(inv);
            return;
        }
        if (e.getSlot() == 12) {
            //设置类型
            StringBuilder now = new StringBuilder("§a目前支援的类型有：");
            for (EntityType value : EntityType.values()) {
                if (value.isSpawnable()) {
                    now.append(value.name()).append(", ");
                }
            }
            ((Player) e.getWhoClicked()).sendMessage(now.toString());
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.please-input-text", "languages.please-input-text")));
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "setType", e.getInventory(), s);
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
            return;
        }

        if (e.getSlot() == 14) {
            //设置刷新物品
            Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle() + " - Item");
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(" ");
            item.setItemMeta(im);
            for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
                inv.setItem(integer, item);
            }
            inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
            inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
            inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l直接把物品放进去就好").addItemLore("§a§l点我保存").create());

            int count = 10;

            for (int i = 0; i < s.getSpawnItem().size() && i < 28; i++) {
                inv.setItem(count, s.getSpawnItem().get(i));
                count++;
                if ((count + 1) % 9 == 0) {
                    count += 2;
                }
            }
            e.getWhoClicked().openInventory(inv);
            return;
        }

        if (e.getSlot() == 15) {
            //设置生物
            Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle() + " - Entities");
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(" ");
            item.setItemMeta(im);
            for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
                inv.setItem(integer, item);
            }
            inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
            inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
            inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l增加生物").create());

            int count = 10;

            for (int i = 0; i < s.getSpawnEntities().size() && i < 28; i++) {
                item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                skull.setDisplayName("".equals(s.getSpawnEntities().get(i).getName()) ? " " : s.getSpawnEntities().get(i).getName());
                if ("".equals(s.getSpawnEntities().get(i).getName())) {
                    skull.setLore(Arrays.asList("§a§l类型：" + s.getSpawnEntities().get(i).getType().name(), "§c未设置名称"));
                } else {
                    skull.setLore(Collections.singletonList("§a§l类型：" + s.getSpawnEntities().get(i).getType().name()));
                }
                skull.setOwner(captureName(s.getSpawnEntities().get(i).getType().name().toLowerCase()));
                item.setItemMeta(skull);
                inv.setItem(count, item);
                count++;
                if ((count + 1) % 9 == 0) {
                    count += 2;
                }
            }
            if (s.getSpawnEntities().size() > 28) {
                inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c生物过多，部分无法显示！").addItemLore("§c共" + (s.getSpawnEntities().size() - 28) + "条无法显示").create());
            }
            e.getWhoClicked().openInventory(inv);
            return;
        }

        if (e.getSlot() == 16) {
            //设置命令
            Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle() + " - Commands");
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(" ");
            item.setItemMeta(im);
            for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
                inv.setItem(integer, item);
            }
            inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
            inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
            inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l新增命令").addItemLore("§a注意：命令将由控制台执行").create());

            int count = 10;

            for (int i = 0; i < s.getSpawnCommand().size() && i < 28; i++) {
                inv.setItem(count, new ItemBuilder(Material.NAME_TAG).setItemDisplayName(s.getSpawnCommand().get(i)).addItemLore("§a点击移除此行").create());
                count++;
                if ((count + 1) % 9 == 0) {
                    count += 2;
                }
            }

            if (s.getLore().size() > 28) {
                inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c命令过多，部分无法显示！").addItemLore("§c共" + (s.getLore().size() - 28) + "条无法显示").create());
            }

            e.getWhoClicked().openInventory(inv);
            return;
        }

        if (e.getSlot() == 22) {
            //设置刷新时间
            ((Player) e.getWhoClicked()).sendMessage("§c设置刷新间隔时间，格式：\n§a<最短间隔时间> <最长间隔时间>\n§a注意二者中间的空格，单位tick");
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "setTime", s);
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
            return;
        }

        if (e.getSlot() == 3) {
            //设置激活半径
            ((Player) e.getWhoClicked()).sendMessage("§c设置激活半径，单位格");
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "setPlayer", s);
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
            return;
        }

        if (e.getSlot() == 5) {
            ((Player) e.getWhoClicked()).sendMessage("§c设置激活半径内最大的实体数量");
            PlayerInputUtil.getPlayerChat((Player) e.getWhoClicked(), this, "setNum", s);
            Bukkit.getScheduler().runTask(plugin, () -> e.getWhoClicked().closeInventory());
        }
    }

    private static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    @Override
    public void callBack(Player input, String msg, Object... objects) {
        if (msg == null) {
            return;
        }
        if ("setTitle".equalsIgnoreCase(String.valueOf(objects[0]))) {
            Spawner s = SpawnerApi.getSpawner(String.valueOf(objects[2]));
            s.setDisplayName(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, msg));
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bukkit.dispatchCommand(input, "epicspawner edit " + String.valueOf(objects[2]));
        }

        if ("addLore".equalsIgnoreCase(String.valueOf(objects[0]))) {
            InventoryClickEvent e = (InventoryClickEvent) objects[3];
            Spawner s = SpawnerApi.getSpawner(String.valueOf(objects[2]));
            List<String> lore = s.getLore();
            lore.add(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, msg));
            s.setLore(lore);
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle());
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(" ");
            item.setItemMeta(im);
            for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
                inv.setItem(integer, item);
            }
            inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
            inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
            inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l新增Lore").create());

            int count = 10;

            for (int i = 0; i < s.getLore().size() && i < 28; i++) {
                inv.setItem(count, new ItemBuilder(Material.NAME_TAG).setItemDisplayName(s.getLore().get(i)).addItemLore("§a点击移除此行").create());
                count++;
                if ((count + 1) % 9 == 0) {
                    count += 2;
                }
            }

            if (s.getLore().size() > 28) {
                inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§cLore过多，部分无法显示！").addItemLore("§c共" + (s.getLore().size() - 28) + "条无法显示").create());
            }

            e.getWhoClicked().openInventory(inv);
            return;
        }

        if ("setType".equalsIgnoreCase(String.valueOf(objects[0]))) {
            //设置类型
            Spawner s = (Spawner) objects[2];
            try {
                EntityType.valueOf(msg.toUpperCase());
            } catch (Exception e) {
                input.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.arguments-value", "languages.arguments-value")));
                Bukkit.dispatchCommand(input, "epicspawner edit " + s.getName());
                return;
            }
            s.setSpawnerEntityType(EntityType.valueOf(msg.toUpperCase()));
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bukkit.dispatchCommand(input, "epicspawner edit " + s.getName());
            return;
        }

        if ("addEntity".equalsIgnoreCase(String.valueOf(objects[0]))) {
            InventoryClickEvent e = (InventoryClickEvent) objects[3];
            Spawner s = SpawnerApi.getSpawner(String.valueOf(objects[2]));

            String[] vs;

            if (!msg.contains(" ")) {
                vs = (msg + " ").split(" ", 2);
            } else {
                vs = msg.split(" ", 2);
            }

            try {
                EntityType.valueOf(vs[0].toUpperCase());
            } catch (Exception ex) {
                input.sendMessage(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, config.getString("languages.arguments-value", "languages.arguments-value")));
                Bukkit.dispatchCommand(input, "epicspawner edit " + s.getName());
                return;
            }

            if (vs.length == 2) {
                s.addSpawnEntitiesByEntityBuilder(new EntityBuilder().setEntityType(EntityType.valueOf(vs[0].toUpperCase()))
                        .setNameVisible(true).setName(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, vs[1])));
            } else {
                s.addSpawnEntitiesByType(EntityType.valueOf(vs[0].toUpperCase()));
            }

            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle());
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(" ");
            item.setItemMeta(im);
            for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
                inv.setItem(integer, item);
            }
            inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
            inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
            inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l增加生物").create());

            int count = 10;

            for (int i = 0; i < s.getSpawnEntities().size() && i < 28; i++) {
                item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                skull.setDisplayName("".equals(s.getSpawnEntities().get(i).getName()) ? " " : s.getSpawnEntities().get(i).getName());
                if ("".equals(s.getSpawnEntities().get(i).getName())) {
                    skull.setLore(Arrays.asList("§a§l类型：" + s.getSpawnEntities().get(i).getType().name(), "§c未设置名称"));
                } else {
                    skull.setLore(Collections.singletonList("§a§l类型：" + s.getSpawnEntities().get(i).getType().name()));
                }
                skull.setOwner(captureName(s.getSpawnEntities().get(i).getType().name().toLowerCase()));
                item.setItemMeta(skull);
                inv.setItem(count, item);
                count++;
                if ((count + 1) % 9 == 0) {
                    count += 2;
                }
            }
            if (s.getSpawnEntities().size() > 28) {
                inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c生物过多，部分无法显示！").addItemLore("§c共" + (s.getSpawnEntities().size() - 28) + "条无法显示").create());
            }
            e.getWhoClicked().openInventory(inv);
            return;
        }

        if ("addCommand".equalsIgnoreCase(String.valueOf(objects[0]))) {
            InventoryClickEvent e = (InventoryClickEvent) objects[3];
            Spawner s = SpawnerApi.getSpawner(String.valueOf(objects[2]));
            List<String> cmd = s.getSpawnCommand();
            cmd.add(ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, msg));
            s.setSpawnCommand(cmd);
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 54, e.getInventory().getTitle());
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(" ");
            item.setItemMeta(im);
            for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47, 48, 49, 50, 51, 53)) {
                inv.setItem(integer, item);
            }
            inv.setItem(0, new ItemBuilder(Material.ARROW).setItemDisplayName("§a§l<- 返回上一页").create());
            inv.setItem(46, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§c§l清空全部").addItemLore("§c警告！本操作无法撤销").create());
            inv.setItem(52, new ItemBuilder(Material.NETHER_STAR).setItemDisplayName("§a§l新增命令").addItemLore("§a注意：命令将由控制台执行").create());

            int count = 10;

            for (int i = 0; i < s.getSpawnCommand().size() && i < 28; i++) {
                inv.setItem(count, new ItemBuilder(Material.NAME_TAG).setItemDisplayName(s.getSpawnCommand().get(i)).addItemLore("§a点击移除此行").create());
                count++;
                if ((count + 1) % 9 == 0) {
                    count += 2;
                }
            }

            if (s.getSpawnCommand().size() > 28) {
                inv.setItem(44, new ItemBuilder(Material.REDSTONE).setItemDisplayName("§cLore过多，部分无法显示！").addItemLore("§c共" + (s.getSpawnCommand().size() - 28) + "条无法显示").create());
            }

            e.getWhoClicked().openInventory(inv);
        }

        if ("setTime".equalsIgnoreCase(String.valueOf(objects[0]))) {
            Spawner s = (Spawner) objects[1];
            if (!msg.contains(" ")) {
                input.sendMessage("§c格式错误");
                return;
            }
            String[] temps = msg.split(" ", 2);
            int min;
            int max;
            try {
                min = Integer.parseInt(temps[0]);
                max = Integer.parseInt(temps[1]);
            } catch (NumberFormatException e) {
                input.sendMessage("§c输入的不是数字");
                return;
            }
            if (min > max || min < 0) {
                input.sendMessage("§c输入的大小错误");
                return;
            }
            s.setTick(min);
            s.setMaxTick(max);
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            input.sendMessage("§a设置成功");
            Bukkit.dispatchCommand(input, "epicspawner edit " + s.getName());
            return;
        }

        if ("setPlayer".equalsIgnoreCase(String.valueOf(objects[0]))) {
            Spawner s = (Spawner) objects[1];
            int r;
            try {
                r = Integer.parseInt(msg);
            } catch (NumberFormatException e) {
                input.sendMessage("§c输入的大小错误");
                return;
            }
            if (r < 0) {
                input.sendMessage("§c输入的大小错误");
                return;
            }
            s.setActivateRadius(r);
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            input.sendMessage("§a设置成功");
            Bukkit.dispatchCommand(input, "epicspawner edit " + s.getName());
            return;
        }

        if ("setNum".equalsIgnoreCase(String.valueOf(objects[0]))) {
            Spawner s = (Spawner) objects[1];
            int r;
            try {
                r = Integer.parseInt(msg);
            } catch (NumberFormatException e) {
                input.sendMessage("§c输入的大小错误");
                return;
            }
            if (r < 0) {
                input.sendMessage("§c输入的大小错误");
                return;
            }
            s.setMaxMobAlive(r);
            try {
                SpawnerApi.saveSpawner(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            input.sendMessage("§a设置成功");
            Bukkit.dispatchCommand(input, "epicspawner edit " + s.getName());
        }
    }
}
