package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Balloonarmor implements BaseWeapon, Listener{
	
	private final RandomWeaponWar plugin;
    private final NamespacedKey itemKey;
	
    private double health;
    private int increasedamage;
    
    public Balloonarmor(RandomWeaponWar plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "Balloonarmor_item");
        loadConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        health = cfg.getDouble("addons.Balloonarmor.health", 40.0);
        increasedamage  = cfg.getInt("addons.Balloonarmor.increasedamage", 80);
 }
    
    @Override
    public String getId() {
        return "풍선갑옷";
    }
    
    public int getUseCount(UUID uuid) {
        return 0;
    }

    public int getPenaltyCount() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
        armorMeta.setColor(Color.fromRGB(37,224,252)); 
        item.setItemMeta(armorMeta);
        
        Component displayName = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().decoration(TextDecoration.ITALIC, false)).append(((TextComponent)Component.text("[ ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text("C").color(NamedTextColor.YELLOW)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text(" ] ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(Component.text("풍선갑옷").color(TextColor.color(37,224,252)).decoration(TextDecoration.BOLD, true))))));
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>();
        lore.add(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text("『P』 ")
                                        .color(NamedTextColor.DARK_GRAY)
                        )
                        .append(
                                Component.text(
                                        "착용 시 받는 피해가 " + increasedamage + "% 증가합니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        meta.lore(lore);
        meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH,
                new AttributeModifier(UUID.randomUUID(), "Balloonarmor_health",
                        health, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE);
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_CHESTPLATE) return false;
        ItemMeta meta = item.getItemMeta();
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
        armorMeta.setColor(Color.fromRGB(37,224,252)); 
        item.setItemMeta(armorMeta);
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }

	@Override
	public void onRightClick(Player player) {
	}
	
	private boolean isWearing(Player player) {
        return this.isWeaponItem(player.getInventory().getChestplate());
    }
	
	@EventHandler
	private void ondamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player p && isWearing(p)) {
			event.setDamage(event.getDamage() * (1 + (((double) increasedamage) / 100.0)));
		}
	}
	
	@EventHandler
	private void ondamage2(EntityDamageByEntityEvent event) {
		ondamage(event);
	}
	
	@EventHandler
	private void ondamage3(EntityDamageByBlockEvent event) {
		ondamage(event);
	}
}
