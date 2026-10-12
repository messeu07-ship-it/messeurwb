package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class XAccuracy implements BaseWeapon {
	
	private final RandomWeaponWar plugin;
    private final CooldownManager cooldownManager;
    private final NamespacedKey itemKey;
	
    private double attackDamage;
    private double attackSpeed;

    private double damage;
    private double minimumrange;
    private double maximumrange;
    private double penaltydamage;

    private long cooldownMs;
    private long penaltycooldownMs; 
    
    	@Override
	    public String getId() {
	        return "잘-맞히기";
	    }
	    
	    public int getUseCount(UUID uuid) {
	        return 0;
	    }

	    public int getPenaltyCount() {
	        return Integer.MAX_VALUE;
	    }
	    
	    public XAccuracy(
	            RandomWeaponWar plugin,
	            CooldownManager cooldownManager
	    ) {
	        this.plugin = plugin;
	        this.cooldownManager = cooldownManager;
	        this.itemKey = new NamespacedKey(plugin, "XAccuracy_item");

	        loadConfig();
	    }
	    
	    @Override
	    public void loadConfig() {
	        FileConfiguration cfg = plugin.getConfig();

	        attackDamage = cfg.getDouble(
	                "addons.xaccuracy.attack-damage", 2.0
	        );

	        attackSpeed = cfg.getDouble(
	                "addons.xaccuracy.attack-speed", -3.0
	        );

	        damage = cfg.getDouble(
	                "addons.xaccuracy.damage", 7.0
	        );

	        minimumrange = cfg.getDouble(
	                "addons.xaccuracy.minimum-range", 4.0
	        );

	        maximumrange = cfg.getDouble(
	                "addons.xaccuracy.maximum-range", 8.0
	        );

	        penaltydamage = cfg.getDouble(
	                "addons.xaccuracy.penalty-damage", 15.0
	        );

	        cooldownMs = cfg.getLong(
	                "addons.xaccuracy.cooldown", 3
	        ) * 1000L;

	        penaltycooldownMs = cfg.getLong(
	                "addons.xaccuracy.penalty-cooldown", 15
	        ) * 1000L;
	    }
	    
	@Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = item.getItemMeta();
        
        Component displayName = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().decoration(TextDecoration.ITALIC, false)).append(((TextComponent)Component.text("[ ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text("B").color(NamedTextColor.AQUA)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text(" ] ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(Component.text("잘-맞히기").color(TextColor.color(232,165,253)).decoration(TextDecoration.BOLD, true))))));
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("『R』 ").color(NamedTextColor.DARK_GRAY))
       .append(Component.text(minimumrange + "칸 이상 떨어진 " + maximumrange + "칸 내의 바라본 대상에게 " + damage + "의 피해를 줍니다.")
                        .color(NamedTextColor.GRAY)));
        lore.add(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text(
                                        "    대상이 없을 경우 자신이 " + penaltydamage + "의 피해를 받고 " + (penaltycooldownMs / 1000) + "초의 쿨타임을 갖습니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("『C』 ").color(NamedTextColor.DARK_GRAY))
             	       .append(Component.text((cooldownMs / 1000)).color(NamedTextColor.GRAY)));
        meta.lore(lore);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(UUID.randomUUID(), "XAccuracy_attack_damage",
                        attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(UUID.randomUUID(), "XAccuracy_attack_speed",
                        attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
	
	@Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null || item.getType() != Material.CARROT_ON_A_STICK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }
	
    public void onRightClick(Player player) {
        UUID uuid = player.getUniqueId();

        // 쿨타임 확인
        if (cooldownManager.isOnCooldown(getId(), uuid)) {
            long remaining =
                    (cooldownManager.getRemainingMillis(getId(), uuid)
                    + 999) / 1000;

            player.sendMessage(
                    Component.text(
                            "잘-맞히기 남은 쿨타임 : "
                            + remaining + "초"
                    ).color(NamedTextColor.RED)
            );

            return;
        }

        // 바라보는 방향의 대상 탐색
        Player target = getTargetPlayer(player);

        if (target == null) {
        	 player.playSound(player, Sound.ENTITY_SKELETON_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            player.sendMessage(
                    Component.text(
                            "대상 지정 실패!"
                    ).color(NamedTextColor.DARK_RED)
            );
            player.damage(penaltydamage);
            cooldownManager.setCooldown(
                    getId(),
                    uuid,
                    penaltycooldownMs
            );
            return;
        }
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.5f);
        player.sendMessage(
                Component.text(
                        "대상 지정 성공!"
                ).color(NamedTextColor.GREEN)
        );
        target.damage(damage);
        cooldownManager.setCooldown(
                getId(),
                uuid,
                cooldownMs
        );
    }
        
        private Player getTargetPlayer(Player player) {
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection().normalize();
            World world = player.getWorld();

            RayTraceResult entityResult = world.rayTraceEntities(
                    eyeLocation,
                    direction,
                    maximumrange,
                    0.3,
                    entity -> entity instanceof Player
                            && !entity.getUniqueId().equals(player.getUniqueId())
                            && !((Player) entity).hasPotionEffect(PotionEffectType.LUCK)
                            && entity.getLocation().distanceSquared(player.getLocation()) >= minimumrange * minimumrange
            );

            if (entityResult == null
                    || !(entityResult.getHitEntity() instanceof Player)) {
                return null;
            }

            // 시야를 가리는 블록 확인
            RayTraceResult blockResult = world.rayTraceBlocks(
                    eyeLocation,
                    direction,
                    maximumrange,
                    FluidCollisionMode.NEVER,
                    true
            );

            if (blockResult != null) {
                double blockDistance = blockResult.getHitPosition()
                        .distance(eyeLocation.toVector());

                double entityDistance = entityResult.getHitPosition()
                        .distance(eyeLocation.toVector());

                // 블록이 플레이어보다 먼저 있다면 대상 지정 불가
                if (blockDistance <= entityDistance) {
                    return null;
                }
            }

            return (Player) entityResult.getHitEntity();
        }
    
}
