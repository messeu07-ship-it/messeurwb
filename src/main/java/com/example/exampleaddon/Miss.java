package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Miss implements BaseWeapon, Listener{
	
	private final RandomWeaponWar plugin;
    private final NamespacedKey itemKey;

    private double attackDamage;
    private double attackSpeed;
    private int misspercent;
    
    public Miss(RandomWeaponWar plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "Miss_item");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadConfig();
    }
    
    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        	attackDamage = cfg.getDouble("addons.Miss.attack-damage", 6);
        	attackSpeed  = cfg.getDouble("addons.Miss.attack-speed", -2);
        	misspercent  = cfg.getInt("addons.Miss.miss-percent", 30);
    	}
    
    @Override
    public String getId() {
        return "!감나빗";
    }

    @Override
    public int getUseCount(UUID uuid) {
        return 0;
    }

    @Override
    public int getPenaltyCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.STONE_SHOVEL);
        ItemMeta meta = item.getItemMeta();

        Component displayName = Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(
                        Component.text("[ ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("A")
                                .color(NamedTextColor.LIGHT_PURPLE)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text(" ] ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("!감나빗")
                                .color(NamedTextColor.GRAY)
                                .decorate(TextDecoration.BOLD)
                );

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
                                        "타격 시 " + misspercent + "% 확률로 빗나갑니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        meta.lore(lore);

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "Miss_attack_damage",
                        attackDamage,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "Miss_attack_speed",
                        attackSpeed,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        meta.getPersistentDataContainer().set(
                itemKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        item.setItemMeta(meta);

        return item;
    }

    @Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null || item.getType() != Material.STONE_SHOVEL) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(
                itemKey,
                PersistentDataType.BYTE
        );
    }

    @Override
    public void onRightClick(Player player) {
        // 패시브 무기이므로 우클릭 기능 없음
    }

    // 주 손에 폿핏포를 들고 있는지 확인
    private boolean isMainHand(Player player) {
        return isWeaponItem(
                player.getInventory().getItemInMainHand()
        );
    }
    
    @EventHandler
	private void ondamage2(EntityDamageByEntityEvent event) {
		if (!event.isCancelled() && event.getDamager() instanceof Player p && isMainHand(p)) {
			if (Math.random() < ((double) misspercent) / 100.0) {
				event.setCancelled(true);
				 p.sendMessage(
		                    Component.text(
		                            "!감나빗"
		                    ).color(NamedTextColor.GRAY)
		            );
				 p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, event.getEntity().getLocation().clone().add(0, 1, 0), 1);
				 p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.5f);
			}
		}
	}

    
    

}
