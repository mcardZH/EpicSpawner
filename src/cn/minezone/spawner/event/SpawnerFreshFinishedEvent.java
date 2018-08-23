package cn.minezone.spawner.event;

import cn.minezone.spawner.craftspawner.Spawner;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * 刷怪笼更新完成事件
 *
 * @author mcard
 */
public class SpawnerFreshFinishedEvent extends Event {

    private Block b;
    private final Spawner spawner;
    private final List<Entity> entities;

    /**
     * 刷新完成事件
     *
     * @param b 刷新完成的刷怪笼方块
     * @param spawner  对应的配置
     * @param entities 生成的实体（包含实体和物品），注意：不包含命令导致的实体生成
     */
    public SpawnerFreshFinishedEvent(Block b, Spawner spawner, List<Entity> entities) {
        this.b = b;
        this.spawner = spawner;
        this.entities = entities;
    }

    public Block getBblock() {
        return b;
    }

    public Spawner getSpawner() {
        return spawner;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }
}
