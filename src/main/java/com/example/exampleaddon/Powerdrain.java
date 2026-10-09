package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
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
import org.bukkit.util.Vector;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Powerdrain implements BaseWeapon {
	    
	private final RandomWeaponWar plugin;
    private final CooldownManager cooldownManager;
    private final NamespacedKey itemKey;
    
    private double attackDamage;
    private double attackSpeed;
    private int duration;
    private int heal;
    private int weakness;
    private int decreaseduration;
    private int decreaseheal;
    private double range;
    private long cooldownMs;
    private List<Map<UUID, UUID>> targetlist = new ArrayList<>();
    
	
	 public Powerdrain(RandomWeaponWar plugin, CooldownManager cooldownManager) {
	        this.plugin = plugin;
	        this.cooldownManager = cooldownManager;
	        this.itemKey = new NamespacedKey(plugin, "Powerdrain_item");
	        loadConfig();
	    }
	 
	 @Override
	    public void loadConfig() {
	        FileConfiguration cfg = plugin.getConfig();
	        	attackDamage = cfg.getDouble("addons.powerdrain.attack-damage", 2.5);
	        	attackSpeed  = cfg.getDouble("addons.powerdrain.attack-speed", -1);
	        	duration = cfg.getInt("addons.powerdrain.duration", 5);
	            heal  = cfg.getInt("addons.powerdrain.heal", 8);
	            weakness  = cfg.getInt("addons.powerdrain.weakness", 1);
	            decreaseduration  = cfg.getInt("addons.powerdrain.decreaseduration", 2);
	            decreaseheal        = cfg.getInt("addons.powerdrain.decreaseheal", 2);
	            range        = cfg.getDouble("addons.powerdrain.range", 6.0);
	            cooldownMs   = cfg.getLong("addons.powerdrain.cooldown", 17) * 1000L;
	 }
	 
	 @Override
	    public String getId() {
	        return "힘흡수";
	    }
	 
	 @Override
	    public ItemStack createItem() {
	        ItemStack item = new ItemStack(Material.NETHER_WART);
	        ItemMeta meta = item.getItemMeta();

	        Component displayName = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().decoration(TextDecoration.ITALIC, false)).append(((TextComponent)Component.text("[ ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text("A").color(NamedTextColor.LIGHT_PURPLE)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text(" ] ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(Component.text("힘흡수").color(TextColor.color(147, 0, 0)))))));
	        
	        meta.displayName(displayName);
	        
	        List<Component> lore = new ArrayList<>();
	        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
	                .append(Component.text("『R』 ").color(NamedTextColor.DARK_GRAY))
	       .append(Component.text(range + "칸 내 가장 가까운 대상에게 " + duration + "초간 나약함 " + weakness + "를 부여하고 자신은 " + heal + "의 체력을 회복합니다.")
	                        .color(NamedTextColor.GRAY)));
	        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
	                .append(Component.text("    동일 대상에 대한 두번째 사용부터는 나약함의 지속시간이 " + decreaseduration + "초, 회복량이 " + decreaseheal + "로 감소합니다.")
	                        .color(NamedTextColor.GRAY)));
	        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
	                .append(Component.text("『C』 ").color(NamedTextColor.DARK_GRAY))
	             	       .append(Component.text((cooldownMs / 1000)).color(NamedTextColor.GRAY)));
	        meta.lore(lore);

	        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
	                new AttributeModifier(UUID.randomUUID(), "Powerdrain_attack_damage",
	                        attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
	        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
	                new AttributeModifier(UUID.randomUUID(), "Powerdrain_attack_speed",
	                        attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

	        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
	        meta.setUnbreakable(true);
	        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
	        item.setItemMeta(meta);
	        return item;
	    }
	 
	 @Override
	    public boolean isWeaponItem(ItemStack item) {
	        if (item == null || item.getType() != Material.NETHER_WART) return false;
	        ItemMeta meta = item.getItemMeta();
	        if (meta == null) return false;
	        return meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
	    }
	 
	 @Override
	    public void onRightClick(Player player) {
	        UUID uuid = player.getUniqueId();

	        if (cooldownManager.isOnCooldown(getId(), uuid)) {
	        	long remaining = (cooldownManager.getRemainingMillis(getId(), uuid) + 999) / 1000;
	            player.sendMessage(Component.text("힘흡수 남은 쿨타임 : " + remaining + "초").color(NamedTextColor.RED));
	            return;
	        }
	        
	        Player target = player.getWorld().getPlayers().stream()
	                .filter(p -> !p.equals(player))
	                .filter(p -> !p.hasPotionEffect(PotionEffectType.LUCK)) 
	                .filter(p -> p.getLocation().distanceSquared(player.getLocation()) <= range * range)
	                .min(java.util.Comparator.comparingDouble(
	                        p -> p.getLocation().distanceSquared(player.getLocation())))
	                .orElse(null);

	        if (target == null) {
	            player.sendMessage(Component.text(range + "칸 내에 대상이 없습니다.").color(NamedTextColor.GRAY));
	            return;
	        }
	        Map<UUID, UUID> user = new HashMap<>();
	        user.put(uuid, target.getUniqueId());
	        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS,  1.0f, 0.5f);
	        Vector direction = player.getLocation().toVector().subtract(target.getLocation().toVector());
	        double distance = direction.length();
	        direction.normalize();
	        for (double d = 0; d <= distance; d += 0.5) {
	            Vector currentVector = direction.clone().multiply(d);
	            Location currentLoc = target.getLocation().clone().add(currentVector);
	            if (d % 1 == 0) player.getWorld().spawnParticle(Particle.REDSTONE, currentLoc.clone().add(0, 1, 0), 3, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(21, 183, 0), 0.9F));
	            else  player.getWorld().spawnParticle(Particle.REDSTONE, currentLoc.clone().add(0, 1, 0), 3, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(198, 0, 0), 0.9F));
	        }
	            cooldownManager.setCooldown(getId(), uuid, cooldownMs);
	        if (targetlist.contains(user)) {
	        	PotionEffect weak = new PotionEffect(PotionEffectType.WEAKNESS, 20 * decreaseduration, weakness - 1); 
		        target.addPotionEffect(weak);
		        healPlayer(player, decreaseheal);
	        }
	        else {      	
		        PotionEffect weak = new PotionEffect(PotionEffectType.WEAKNESS, 20 * duration, weakness - 1); 
		        target.addPotionEffect(weak);
		        healPlayer(player, heal);	
		        targetlist.add(user);
	        }
	 }
	 
	 @SuppressWarnings("deprecation")
	private void healPlayer(Player player, double amount) {
		    double maxHealth = player.getMaxHealth();

		    player.setHealth(
		        Math.min(maxHealth, player.getHealth() + amount)
		    );
		}
	 
	 @Override
	 public int getUseCount(UUID uuid) {	
		return 0;
	 }

	 @Override
	 public int getPenaltyCount() {	
		 return Integer.MAX_VALUE;
	 }
}
