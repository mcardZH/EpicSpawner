package cn.minezone.spawner.listener;

import cn.minezone.spawner.input.PlayerInputUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

/**
 * @author mcard
 */
public class PlayerInputHelper implements Listener {

    @EventHandler
    public void onTab(PlayerChatTabCompleteEvent e) {
        if (PlayerInputUtil.isWaitingForPlayerInput(e.getPlayer())) {
            e.getTabCompletions().clear();
            for (EntityType value : EntityType.values()) {
                if (value.isSpawnable()) {
                    e.getTabCompletions().add(value.name());
                }
            }
        }
    }

}
