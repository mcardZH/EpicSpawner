package cn.minezone.spawner.craftspawner;

import cn.minezone.spawner.builder.EntityBuilder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * @author mcard
 */
public interface Spawner extends ConfigurationSerializable {

    /**
     * 取刷怪笼名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 设置刷新生物
     *
     * @param types 生物类型
     */
    void setSpawnEntitiesByType(List<EntityType> types);

    /**
     * 设置刷新生物
     *
     * @param builders 自定义生物构造器（其中的Location属性会被忽略）
     */
    void setSpawnEntitiesByEntityBuilder(List<EntityBuilder> builders);

    /**
     * 增加刷新生物类型
     *
     * @param type 类型
     */
    void addSpawnEntitiesByType(EntityType... type);

    /**
     * 增加刷新生物类型
     *
     * @param builder 自定义生物构造器（其中的Location属性会被忽略）
     */
    void addSpawnEntitiesByEntityBuilder(EntityBuilder... builder);

    /**
     * 获取全部刷新构造器
     *
     * @return 自定义生物构造器（其中的Location非实际位置）
     */
    List<EntityBuilder> getSpawnEntities();

    /**
     * 设置刷新物品
     *
     * @param items 物品
     */
    void setSpawnItems(List<ItemStack> items);

    /**
     * 增加刷新物品
     *
     * @param item 物品
     */
    void addSpawnItem(ItemStack... item);

    /**
     * 获取全部刷新物品
     *
     * @return 物品
     */
    List<ItemStack> getSpawnItem();

    /**
     * 设置刷新指令
     *
     * @param commands 指令
     */
    void setSpawnCommand(List<String> commands);

    /**
     * 增加刷新指令
     *
     * @param commands 指令
     */
    void addSpawnCommand(String... commands);

    /**
     * 获取全部刷新指令
     *
     * @return 指令
     */
    List<String> getSpawnCommand();

    /**
     * 设置间隙刷怪时间
     * @param i 20ticks ≈ 1s
     */
    void setTick(int i);

    /**
     * 获取间隙刷怪时间
     * @return 20ticks ≈ 1s
     */
    int getTick();

    /**
     * 设置最大间隔时间
     * @param ticks 20ticks ≈ 1s
     */
    void setMaxTick(int ticks);

    /**
     * 获取最大间隔时间
     * @return 20ticks ≈ 1s
     */
    int getMaxTick();

    /**
     * 设置刷怪笼活跃半径，有玩家在此半径内才会刷怪
     * @param radius 半径（格）
     */
    void setActivateRadius(int radius);

    /**
     * 获取活跃半径
     * @return 半径（格）
     */
    int getActivateRadius();

    /**
     * 设置这个刷怪笼能同时存在的最大怪物数（超过这个数量会暂停刷怪）
     * @param i 数量
     */
    void setMaxMobAlive(int i);

    /**
     * 获取最大同时存在数（周遭实体最大数量，不含玩家）
     * @return 数量
     */
    int getMaxMobAlive();

    /**
     * 设置刷怪笼里面转圈圈的物体
     * @param type 类型
     */
    void setSpawnerEntityType(EntityType type);

    /**
     * 获取刷怪笼里面转圈圈的物体
     * @return 类型
     */
    EntityType getSpawnerEntityType();

    /**
     * 设置刷怪笼物品的Lore
     *
     * @param lore lore
     */
    void setLore(List<String> lore);

    /**
     * 获取刷怪笼物品的Lore
     *
     * @return lore
     */
    List<String> getLore();

    /**
     * 设置名称
     * @param name 名称
     */
    void setDisplayName(String name);

    /**
     * 获取名称
     * @return 名称
     */
    String getDisplayName();

    /**
     * 获取刷怪笼物品
     * 警告：本插件使用一行自定义的NBT标签来确定刷怪笼种类
     * 即spawnerName标签
     *
     * @return 取回本刷怪笼的物品
     */
    ItemStack getSpawnerItem();

    /**
     * 序列化用于配置存储
     *
     * @return 存储
     */
    @Override
    Map<String, Object> serialize();

}
