package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class DestroyPhenomenon implements BaseWeapon{

	private final RandomWeaponWar plugin;
    private final CooldownManager cooldownManager;
    private final NamespacedKey itemKey;
    
    private double attackDamage;
    private double attackSpeed;
    private double range;
    private long cooldownMs;
    
    public DestroyPhenomenon(RandomWeaponWar plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.itemKey = new NamespacedKey(plugin, "DestroyPhenomenon_item");
        loadConfig();
    }
    
    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        	attackDamage = cfg.getDouble("addons.destroyphenomenon.attack-damage", 3);
        	attackSpeed  = cfg.getDouble("addons.destroyphenomenon.attack-speed", -1);
            range        = cfg.getDouble("addons.destroyphenomenon.range", 8.0);
            cooldownMs   = cfg.getLong("addons.destroyphenomenon.cooldown", 10) * 1000L;
 }
    
    @Override
    public String getId() {
        return "현상파괴";
    }
    
    public int getUseCount(UUID uuid) {
        return 0;
    }

    public int getPenaltyCount() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        
        Component displayName = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().decoration(TextDecoration.ITALIC, false)).append(((TextComponent)Component.text("[ ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text("B").color(NamedTextColor.AQUA)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text(" ] ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(Component.text("현상파괴").color(TextColor.color(31, 28, 64)).decoration(TextDecoration.BOLD, true))))));
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("『R』 ").color(NamedTextColor.DARK_GRAY))
       .append(Component.text(range + "칸 내 자신을 포함한 모든 대상이 가진 포션효과를 제거합니다.")
                        .color(NamedTextColor.GRAY)));
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("『C』 ").color(NamedTextColor.DARK_GRAY))
             	       .append(Component.text((cooldownMs / 1000)).color(NamedTextColor.GRAY)));
        meta.lore(lore);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(UUID.randomUUID(), "DestroyPhenomenon_attack_damage",
                        attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(UUID.randomUUID(), "DestroyPhenomenon_attack_speed",
                        attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null || item.getType() != Material.BEDROCK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }
    
    @Override
    public void onRightClick(Player player) {
        UUID uuid = player.getUniqueId();

        if (cooldownManager.isOnCooldown(getId(), uuid)) {
        	long remaining = (cooldownManager.getRemainingMillis(getId(), uuid) + 999) / 1000;
            player.sendMessage(Component.text("현상파괴 남은 쿨타임 : " + remaining + "초").color(NamedTextColor.RED));
            return;
        }
        
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 0.5f);
        
        cooldownManager.setCooldown(getId(), uuid, cooldownMs);
        
        for (Player p : player.getWorld().getPlayers()) {
        	if (!p.hasPotionEffect(PotionEffectType.LUCK)) {
        	if (p.getLocation().distanceSquared(player.getLocation()) <= range * range) {
        		p.getWorld().spawnParticle(Particle.BLOCK_CRACK, p.getLocation().clone().add(0, 0.5, 0), 30, 0.5, 0, 0.5, Material.GLASS.createBlockData());
        		p.getWorld().spawnParticle(Particle.BLOCK_CRACK, p.getLocation().clone().add(0, 1.5, 0), 30, 0.5, 0, 0.5, Material.GLASS.createBlockData());
        		 Collection<PotionEffect> effects = p.getActivePotionEffects();
                 for (PotionEffect effect : effects) {
                	 if (effect.getType().equals(PotionEffectType.NIGHT_VISION) || effect.getType().equals(PotionEffectType.SATURATION)) continue;
                     p.removePotionEffect(effect.getType());        
                 }
        		}
        	}
        }
    }   
}
