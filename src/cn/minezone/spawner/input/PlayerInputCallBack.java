package cn.minezone.spawner.input;

import org.bukkit.entity.Player;

/**
 * @author mcard
 */
public interface PlayerInputCallBack {
    /**
     * 回调方法
     * @param input 输入的玩家
     * @param msg 输入的信息
     * @param objects 构造时传递的自定义参数
     */
    void callBack(Player input, String msg, Object... objects);
}
