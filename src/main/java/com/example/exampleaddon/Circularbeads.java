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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Circularbeads implements BaseWeapon, Listener{
	
	private final RandomWeaponWar plugin;
    private final CooldownManager cooldownManager;
    private final NamespacedKey itemKey;
    private final NamespacedKey projectileKey;
    
    private double attackDamage;
    private double attackSpeed;
    private int slow;
    private int fatigue;
    private int debuffduration;
    private int damage;
    private int regeneration;
    private int speed;
    private int buffduration;
    private long cooldownMs;
    private final Map<UUID, Integer> beadsMap = new HashMap<>();
	
    public Circularbeads(RandomWeaponWar plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.itemKey = new NamespacedKey(plugin, "Circularbeads_item");
        this.projectileKey = new NamespacedKey(plugin, "circularbeads_projectile");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadConfig();
    }
    
    @Override
    public String getId() {
        return "순환의 구슬";
    }
    
    public int getUseCount(UUID uuid) {
        return 0;
    }

    public int getPenaltyCount() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null || item.getType() != Material.HEART_OF_THE_SEA) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }
    
    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();

        attackDamage = cfg.getDouble("addons.circularbeads.attack-damage", 3.0);
        attackSpeed = cfg.getDouble("addons.circularbeads.attack-speed", -1.5);

        slow = cfg.getInt("addons.circularbeads.slow", 2);
        fatigue = cfg.getInt("addons.circularbeads.fatigue", 2);
        debuffduration = cfg.getInt("addons.circularbeads.debuffduration", 4);

        damage = cfg.getInt("addons.circularbeads.damage", 5);

        regeneration = cfg.getInt("addons.circularbeads.regeneration", 1);
        speed = cfg.getInt("addons.circularbeads.speed", 2);
        buffduration = cfg.getInt("addons.circularbeads.buffduration", 5);

        cooldownMs = cfg.getLong("addons.circularbeads.cooldown", 5) * 1000L;
    }
    
    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        
        Component displayName = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().decoration(TextDecoration.ITALIC, false)).append(((TextComponent)Component.text("[ ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text("A").color(NamedTextColor.LIGHT_PURPLE)).decorate(TextDecoration.BOLD))).append(((TextComponent)Component.text(" ] ").color(NamedTextColor.DARK_GRAY)).decorate(TextDecoration.BOLD))).append(Component.text("순환의 구슬").color(TextColor.color(184, 253, 244)).decoration(TextDecoration.BOLD, true))))));
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("『R』 ").color(NamedTextColor.DARK_GRAY))
       .append(Component.text("사용할 때마다 다음 순서대로 효과가 변경됩니다.")
                        .color(NamedTextColor.GRAY)));
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
        		.append(Component.text("    1. 구속 " + slow + ", 채굴 피로 " + fatigue + "를 " + debuffduration + "초 부여하는 투사체를 발사합니다.")
                .color(NamedTextColor.GRAY)));
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
        		.append(Component.text("    2. " + damage + "의 피해를 주는 투사체를 발사합니다.")
                .color(NamedTextColor.GRAY)));
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
        		.append(Component.text("    3. " + "자신에게 재생 " + regeneration + ", 신속 " + speed + "를 " + buffduration + "초 부여합니다.")
                .color(NamedTextColor.GRAY)));
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
        		.append(Component.text("    3번째 효과 이후에는 1번째 효과로 돌아옵니다.")
                .color(NamedTextColor.GRAY)));
        lore.add(Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("『C』 ").color(NamedTextColor.DARK_GRAY))
             	       .append(Component.text((cooldownMs / 1000)).color(NamedTextColor.GRAY)));
        meta.lore(lore);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(UUID.randomUUID(), "Circularbeads_attack_damage",
                        attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(UUID.randomUUID(), "Circularbeads_attack_speed",
                        attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
        
    @Override
    public void onRightClick(Player player) {
        UUID uuid = player.getUniqueId();

        if (cooldownManager.isOnCooldown(getId(), uuid)) {
            long remaining =
                    (cooldownManager.getRemainingMillis(getId(), uuid) + 999) / 1000;

            player.sendMessage(
                    Component.text("순환의 구슬 남은 쿨타임 : " + remaining + "초")
                            .color(NamedTextColor.RED)
            );
            return;
        }

        // 플레이어별 현재 스킬 순서. 최초 사용은 1번
        int beads = beadsMap.getOrDefault(uuid, 1);

        switch (beads) {
            case 1:
                // 구속 및 채굴 피로 투사체
                launchBead(player, (byte) 1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                break;

            case 2:
                // 데미지 투사체
                launchBead(player, (byte) 2);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                break;

            case 3:
            	player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION,
                        buffduration * 20,
                        Math.max(0, regeneration - 1)
                ));

                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        buffduration * 20,
                        Math.max(0, speed - 1)
                ));
                spawnBuffParticles(player);
                break;
        }

        beadsMap.put(uuid, beads % 3 + 1);

        cooldownManager.setCooldown(getId(), uuid, cooldownMs);
    }
    
    
    
 // 투사체 발사
    private void launchBead(Player player, byte type) {
        Snowball projectile = player.launchProjectile(Snowball.class);

        projectile.setGravity(false);

        // 모든 현재 월드 플레이어에게 눈덩이 엔티티 숨기기
        for (Player viewer : player.getWorld().getPlayers()) {
            viewer.hideEntity(plugin, projectile);
        }

        // 발사 방향과 속도 설정
        projectile.setVelocity(
            player.getLocation().getDirection().normalize().multiply(1.4)
        );

        projectile.getPersistentDataContainer().set(
            projectileKey,
            PersistentDataType.BYTE,
            type
        );

        // 투사체 외형을 갱신
        new BukkitRunnable() {
            double traveled = 0.0;
            Location previousLocation = projectile.getLocation().clone();

            @Override
            public void run() {
                if (!projectile.isValid() || projectile.isDead()) {
                    cancel();
                    return;
                }

                Location loc = projectile.getLocation();

                // 이번 틱에 이동한 거리 누적
                traveled += previousLocation.distance(loc);
                previousLocation = loc.clone();

                // 누적 이동 거리가 8칸 이상이면 제거
                if (traveled >= 16.0) {
                    projectile.remove();
                    cancel();
                    return;
                }

                // 기존 투사체 파티클 효과
                if (type == 1) {
                    // 디버프: 흰색, 회색, 검은색
                    spawnParticleSphere(
                        loc,
                        Color.fromRGB(255, 255, 255),
                        Color.fromRGB(128, 128, 128),
                        Color.fromRGB(20, 20, 20)
                    );
                } else if (type == 2) {
                    // 데미지: 연한 빨강, 빨강, 짙은 빨강
                    spawnParticleSphere(
                        loc,
                        Color.fromRGB(255, 150, 150),
                        Color.fromRGB(255, 0, 0),
                        Color.fromRGB(110, 0, 0)
                    );
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void spawnBuffParticles(Player player) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= 12) {
                    cancel();
                    return;
                }

                Location base = player.getLocation().add(0, 2.0, 0);

                // 위로 올라갔다가 다시 내려오는 움직임
                double height = 0.5 + 0.5 * Math.sin(Math.PI * ticks / 7.0);

                Location particleLoc = base.clone().add(0, height, 0);

                spawnParticleSphere(
                    particleLoc,
                    Color.fromRGB(150, 255, 100),
                    Color.fromRGB(150, 255, 100),
                    Color.fromRGB(150, 255, 100)
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }


    // 세 가지 색상으로 작은 구체 파티클 생성
    private void spawnParticleSphere(
            Location center, Color color1, Color color2, Color color3) {

        Color[] colors = { color1, color2, color3 };
        double radius = 0.22;
        int points = 18;

        for (int i = 0; i < points; i++) {
            double y = 1.0 - (2.0 * (i + 0.5) / points);
            double ringRadius = Math.sqrt(1.0 - y * y);
            double angle = i * Math.PI * (3.0 - Math.sqrt(5.0));

            double x = Math.cos(angle) * ringRadius;
            double z = Math.sin(angle) * ringRadius;

            Location point = center.clone().add(
                x * radius,
                y * radius,
                z * radius
            );

            Color color = colors[i % colors.length];

            center.getWorld().spawnParticle(
                Particle.REDSTONE,
                point,
                1,
                0, 0, 0,
                0,
                new Particle.DustOptions(color, 0.9f)
            );
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (!projectile.getPersistentDataContainer()
                .has(projectileKey, PersistentDataType.BYTE)) {
            return;
        }

        Byte type = projectile.getPersistentDataContainer()
                .get(projectileKey, PersistentDataType.BYTE);

        if (type == null) return;

        Entity shooter = projectile.getShooter() instanceof Entity
                ? (Entity) projectile.getShooter()
                : null;

        Entity hit = event.getHitEntity();

        if (!(hit instanceof LivingEntity)) return;

        LivingEntity target = (LivingEntity) hit;

        // 발사자 자신에게는 적중 효과를 적용하지 않음
        if (target.equals(shooter) || target.hasPotionEffect(PotionEffectType.LUCK)) return;

        if (type == 1) {
        	event.setCancelled(true);
            // 1번: 구속 II + 채굴 피로 II
            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW,
                    debuffduration * 20,
                    Math.max(0, slow - 1)
            ));

            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_DIGGING,
                    debuffduration * 20,
                    Math.max(0, fatigue - 1)
            ));           
            
            
        } else if (type == 2) {
        	event.setCancelled(true);
            if (shooter instanceof Player) {
                target.damage(damage, (Player) shooter);
            } else {
                target.damage(damage);
            }
        }
    }
    
    @Override
    public void clearPlayer(Player player) {
        beadsMap.remove(player.getUniqueId());
    }
}
