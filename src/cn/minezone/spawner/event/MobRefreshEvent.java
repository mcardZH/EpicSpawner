package cn.minezone.spawner.event;

import cn.minezone.spawner.craftspawner.Spawner;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author mcard
 */
public class MobRefreshEvent extends Event implements Cancellable {

    private final Location location;
    private final Spawner spawner;
    HandlerList hl = new HandlerList();
    private boolean b = false;


    public MobRefreshEvent(Location location, Spawner spawner) {
        this.location = location;
        this.spawner = spawner;
    }

    @Override
    public boolean isCancelled() {
        return b;
    }

    @Override
    public void setCancelled(boolean b) {
        this.b = b;
    }

    @Override
    public HandlerList getHandlers() {
        return hl;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * 获取本次即将刷新的刷怪笼（可以修改）<br/>
     * <b>注意：生物的刷新位置已经赋值，修改会改变刷新位置</b>
     * @return 刷怪笼
     */
    public Spawner getSpawner() {
        return spawner;
    }
}
