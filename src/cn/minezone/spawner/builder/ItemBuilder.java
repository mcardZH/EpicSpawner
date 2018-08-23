package cn.minezone.spawner.builder;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * @author mcard
 */
public class ItemBuilder {
    private Material type;
    private String name;
    private List<String> lore = new ArrayList<>();
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private int amount = 1;

    public ItemBuilder(Material type) {
        this.type = type;
    }

    public ItemBuilder(ItemStack item) {
        if (item.getItemMeta() != null) {
            setItemDisplayName(item.getItemMeta().getDisplayName());
            setItemLore(item.getItemMeta().getLore());
        }
        setAmount(item.getAmount());
        if (item.getEnchantments() != null) {
            setItemEnchantment(item.getEnchantments());
        }
    }

    public ItemBuilder setItemDisplayName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder setItemLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder addItemLore(String... lore) {
        this.lore.addAll(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder setItemEnchantment(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
        return this;
    }

    public ItemBuilder addItemEnchantment(Enchantment type, int level) {
        enchantments.put(type, level);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(type);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        item.setItemMeta(im);
        item.setAmount(amount);
        item.addUnsafeEnchantments(enchantments);
        return item;
    }

    public Material getType() {
        return type;
    }

    public String getDisplayName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public List<String> getLore() {
        return lore;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
}
