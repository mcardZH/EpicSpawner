package cn.minezone.spawner.craftspawner;

import cn.minezone.spawner.SpawnerPlugin;
import cn.minezone.spawner.builder.EntityBuilder;
import cn.minezone.spawner.builder.ItemBuilder;
import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author mcard
 */
public class CraftSpawner implements Spawner {

    private String name;
    private List<EntityBuilder> builders = new ArrayList<>();
    private List<ItemStack> items = new ArrayList<>();
    private List<String> commands = new ArrayList<>();
    private List<String> lore = new ArrayList<>();
    private String displayName;
    private int tick = 100;
    private int radius = 5;
    private int alive = 5;
    private int ticks = 200;
    private EntityType type = EntityType.PIG;

    CraftSpawner(String name) {
        setName(name);
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setSpawnEntitiesByType(List<EntityType> types) {
        builders.clear();
        for (EntityType type : types) {
            addSpawnEntitiesByEntityBuilder(new EntityBuilder().setEntityType(type));
        }
    }

    @Override
    public void setSpawnEntitiesByEntityBuilder(List<EntityBuilder> builders) {
        this.builders = builders;
    }

    @Override
    public void addSpawnEntitiesByType(EntityType... type) {
        for (EntityType entityType : type) {
            addSpawnEntitiesByEntityBuilder(new EntityBuilder().setEntityType(entityType));
        }

    }

    @Override
    public void addSpawnEntitiesByEntityBuilder(EntityBuilder... builder) {
        builders.addAll(Arrays.asList(builder));
    }

    @Override
    public List<EntityBuilder> getSpawnEntities() {
        return builders;
    }

    @Override
    public void setSpawnItems(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void addSpawnItem(ItemStack... item) {
        items.addAll(Arrays.asList(item));
    }

    @Override
    public List<ItemStack> getSpawnItem() {
        return items;
    }

    @Override
    public void setSpawnCommand(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void addSpawnCommand(String... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public List<String> getSpawnCommand() {
        return commands;
    }

    @Override
    public void setTick(int i) {
        tick = i;
    }

    @Override
    public int getTick() {
        return tick;
    }

    @Override
    public void setMaxTick(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public int getMaxTick() {
        return ticks;
    }

    @Override
    public void setActivateRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public int getActivateRadius() {
        return radius;
    }

    @Override
    public void setMaxMobAlive(int i) {
        alive = i;
    }

    @Override
    public int getMaxMobAlive() {
        return alive;
    }

    @Override
    public void setSpawnerEntityType(EntityType type) {
        this.type = type;
    }

    @Override
    public EntityType getSpawnerEntityType() {
        return type;
    }

    @Override
    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public void setDisplayName(String name) {
        displayName = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public ItemStack getSpawnerItem() {

        if (Bukkit.getPluginManager().getPlugin(SpawnerPlugin.PLUGIN_NAME).getConfig().getBoolean("lore-mode", true)) {
            if (ChatColor.stripColor(lore.get(lore.size() - 1)).equals(name)) {
                return new ItemBuilder(Material.MOB_SPAWNER)
                        .setItemDisplayName(displayName)
                        .setItemLore(lore)
                        .create();
            }
            return new ItemBuilder(Material.MOB_SPAWNER)
                    .setItemDisplayName(displayName)
                    .setItemLore(lore).addItemLore("§0" + name)
                    .create();
        } else {
            NBTItem nbt = new NBTItem(new ItemBuilder(Material.MOB_SPAWNER)
                    .setItemDisplayName(displayName)
                    .setItemLore(lore)
                    .create());
            nbt.setString("spawnerName", name);


            return nbt.getItem();
        }


    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>(11);
        map.put("name", name);
        map.put("builders", builders);
        map.put("items", items);
        map.put("commands", commands);
        map.put("displayName", displayName);
        map.put("lore", lore);
        map.put("tick", tick);
        map.put("ticks", ticks);
        map.put("radius", radius);
        map.put("alive", alive);
        map.put("type", type.name());
        return map;
    }

    /**
     * 请不要通过此方法来获取对象！
     *
     * @param map 存储
     * @return 刷怪笼
     */
    public static CraftSpawner deserialize(Map<String, Object> map) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EpicSpawner] Loaded " + map.get("name"));
        CraftSpawner cs = new CraftSpawner((String) map.get("name"));
        cs.setDisplayName((String) map.get("displayName"));
        cs.setLore((List<String>) map.get("lore"));
        cs.setSpawnCommand((List<String>) map.get("commands"));
        cs.setSpawnEntitiesByEntityBuilder((List<EntityBuilder>) map.get("builders"));
        cs.setSpawnItems((List<ItemStack>) map.get("items"));
        cs.setTick((Integer) map.get("tick"));
        cs.setMaxTick((Integer) map.get("ticks"));
        cs.setActivateRadius((Integer) map.get("radius"));
        cs.setMaxMobAlive((Integer) map.get("alive"));
        cs.setSpawnerEntityType(EntityType.valueOf(String.valueOf(map.get("type"))));
        return cs;
    }
}
