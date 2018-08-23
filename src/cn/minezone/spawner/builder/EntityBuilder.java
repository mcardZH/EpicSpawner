package cn.minezone.spawner.builder;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mcard
 */
public class EntityBuilder implements ConfigurationSerializable {

    /**
     * 默认是猪
     */
    private EntityType type = EntityType.PIG;
    private String name = "";
    private Location location;
    private boolean visible = false;

    public EntityBuilder() {

    }

    /**
     * 从配置中读取时会自动调用这个构造方法
     *
     * @param map 存储
     */
    public EntityBuilder(Map<String, Object> map) {
        setEntityType(EntityType.valueOf(String.valueOf(map.get("type"))));
        setLocation((Location) map.get("location"));
        setName((String) map.get("name"));
        setNameVisible((Boolean) map.get("visible"));
    }

    /**
     * 设置的对象必须能够生成，否则不会设置
     *
     * @param type 类型
     * @return 自身
     */
    public EntityBuilder setEntityType(EntityType type) {
        if (type.isSpawnable()) {
            this.type = type;
        }
        return this;
    }

    public EntityBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public EntityBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public EntityBuilder setNameVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public Entity spawn() {
        Entity entity = location.getWorld().spawnEntity(location, type);
        entity.setCustomName(name);
        entity.setCustomNameVisible(visible);
        return entity;
    }

    public EntityType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public boolean getNameVisible() {
        return visible;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>(4);
        map.put("type", type.name());
        map.put("name", name);
        map.put("location", location);
        map.put("visible", visible);
        return map;
    }
}
