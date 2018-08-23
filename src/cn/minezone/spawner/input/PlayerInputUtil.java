package cn.minezone.spawner.input;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mcard
 * @version 1.0
 * 用于获取玩家输入信息<br/>
 * 警告！使用必须将本类注册为插件的监听器之一！否则功能无法正常使用！
 */
public class PlayerInputUtil implements Listener {

    private static Map<Player, PlayerInputCallBack> playerChat = new HashMap<>();
    private static Map<Player, Object[]> playerObjects = new HashMap<>();

    /**
     * 异步获取玩家聊天信息<br/>
     * <b>玩家离线</b>会传递NULL给回调方法
     *
     * @param player 玩家
     * @param call   回调
     */
    public static void getPlayerChat(Player player, PlayerInputCallBack call, Object... objects) {
        playerChat.put(player, call);
        playerObjects.put(player, objects);
    }

    public static boolean isWaitingForPlayerInput(Player p) {
        return playerChat.containsKey(p);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (playerChat.containsKey(e.getPlayer())) {
            e.setCancelled(true);
            playerChat.get(e.getPlayer()).callBack(e.getPlayer(), e.getMessage(), playerObjects.get(e.getPlayer()));
            playerChat.remove(e.getPlayer());
            playerObjects.remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (playerChat.containsKey(e.getPlayer())) {
            playerChat.get(e.getPlayer()).callBack(e.getPlayer(), null, playerObjects.get(e.getPlayer()));
            playerChat.remove(e.getPlayer());
            playerObjects.remove(e.getPlayer());
        }
    }


}
