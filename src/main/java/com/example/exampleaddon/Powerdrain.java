package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
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

    // 이미 흡수 효과를 적용한 사용자-대상 조합
    private final List<Map<UUID, UUID>> targetlist = new ArrayList<>();

    public Powerdrain(
            RandomWeaponWar plugin,
            CooldownManager cooldownManager
    ) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.itemKey = new NamespacedKey(plugin, "Powerdrain_item");

        loadConfig();
    }

    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();

        attackDamage = cfg.getDouble(
                "addons.powerdrain.attack-damage", 2.5
        );

        attackSpeed = cfg.getDouble(
                "addons.powerdrain.attack-speed", -1
        );

        duration = cfg.getInt(
                "addons.powerdrain.duration", 5
        );

        heal = cfg.getInt(
                "addons.powerdrain.heal", 8
        );

        weakness = cfg.getInt(
                "addons.powerdrain.weakness", 1
        );

        decreaseduration = cfg.getInt(
                "addons.powerdrain.decreaseduration", 2
        );

        decreaseheal = cfg.getInt(
                "addons.powerdrain.decreaseheal", 3
        );

        range = cfg.getDouble(
                "addons.powerdrain.range", 6.0
        );

        cooldownMs = cfg.getLong(
                "addons.powerdrain.cooldown", 17
        ) * 1000L;
    }

    @Override
    public String getId() {
        return "힘흡수";
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.NETHER_WART);
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
                        Component.text("힘흡수")
                                .color(TextColor.color(147, 0, 0))
                );

        meta.displayName(displayName);

        List<Component> lore = new ArrayList<>();

        lore.add(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text("『R』 ")
                                        .color(NamedTextColor.DARK_GRAY)
                        )
                        .append(
                                Component.text(
                                        range + "칸 내 바라보는 대상에게 "
                                        + duration + "초간 나약함 "
                                        + weakness + "를 부여하고 "
                                        + "자신은 " + heal
                                        + "의 체력을 회복합니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        lore.add(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text(
                                        "    동일 대상에 대한 두번째 사용부터는 "
                                        + "나약함의 지속시간이 "
                                        + decreaseduration + "초, "
                                        + "회복량이 " + decreaseheal
                                        + "으로 감소합니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        lore.add(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text("『C』 ")
                                        .color(NamedTextColor.DARK_GRAY)
                        )
                        .append(
                                Component.text(cooldownMs / 1000)
                                        .color(NamedTextColor.GRAY)
                        )
        );

        meta.lore(lore);

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "Powerdrain_attack_damage",
                        attackDamage,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "Powerdrain_attack_speed",
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
        if (item == null
                || item.getType() != Material.NETHER_WART) {
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
        UUID uuid = player.getUniqueId();

        // 쿨타임 확인
        if (cooldownManager.isOnCooldown(getId(), uuid)) {
            long remaining =
                    (cooldownManager.getRemainingMillis(getId(), uuid)
                    + 999) / 1000;

            player.sendMessage(
                    Component.text(
                            "힘흡수 남은 쿨타임 : "
                            + remaining + "초"
                    ).color(NamedTextColor.RED)
            );

            return;
        }

        // 바라보는 방향의 대상 탐색
        Player target = getTargetPlayer(player);

        if (target == null) {
            player.sendMessage(
                    Component.text(
                            "바라보는 방향에 사거리 내 대상이 없습니다."
                    ).color(NamedTextColor.GRAY)
            );

            return;
        }

        UUID targetUuid = target.getUniqueId();

        Map<UUID, UUID> userTarget = new HashMap<>();
        userTarget.put(uuid, targetUuid);

        // 흡수 사운드
        player.playSound(
                player.getLocation(),
                Sound.ENTITY_GENERIC_DRINK,
                SoundCategory.PLAYERS,
                1.0f,
                0.5f
        );

        // 대상에서 사용자 쪽으로 이동하는 혼합색 구체
        playDrainParticles(player, target);

        // 동일 사용자-대상 조합인지 확인
        if (targetlist.contains(userTarget)) {
            PotionEffect weak = new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    decreaseduration * 20,
                    Math.max(0, weakness - 1)
            );

            target.addPotionEffect(weak);

            healPlayer(player, decreaseheal);

        } else {
            PotionEffect weak = new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    duration * 20,
                    Math.max(0, weakness - 1)
            );

            target.addPotionEffect(weak);

            healPlayer(player, heal);

            targetlist.add(userTarget);
        }

        // 성공적으로 사용한 경우 쿨타임 적용
        cooldownManager.setCooldown(
                getId(),
                uuid,
                cooldownMs
        );
    }

    /**
     * 플레이어가 바라보는 방향에서 가장 먼저 맞는 플레이어를 찾는다.
     * 블록이 대상보다 먼저 충돌하면 대상을 선택하지 않는다.
     */
    private Player getTargetPlayer(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        World world = player.getWorld();

        RayTraceResult entityResult = world.rayTraceEntities(
                eyeLocation,
                direction,
                range,
                0.3,
                entity -> entity instanceof Player
                        && !entity.getUniqueId().equals(player.getUniqueId())
                        && !((Player) entity).hasPotionEffect(PotionEffectType.LUCK)
        );

        if (entityResult == null
                || !(entityResult.getHitEntity() instanceof Player)) {
            return null;
        }

        // 시야를 가리는 블록 확인
        RayTraceResult blockResult = world.rayTraceBlocks(
                eyeLocation,
                direction,
                range,
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

    /**
     * 대상에게서 사용자 쪽으로 빨간색과 초록색이 섞인
     * 하나의 작은 구체를 이동시킨다.
     */
    private void playDrainParticles(Player player, Player target) {
        World world = player.getWorld();

        // 대상의 몸통 부근에서 시작
        Vector start = target.getLocation()
                .clone()
                .add(0, 1.0, 0)
                .toVector();

        // 사용자의 몸통 부근에서 종료
        Vector end = player.getLocation()
                .clone()
                .add(0, 1.0, 0)
                .toVector();

        Vector travel = end.clone().subtract(start);

        // 거리에 맞춰 애니메이션 길이 결정
        int totalTicks = Math.max(
                4,
                Math.min(
                        12,
                        (int) Math.ceil(start.distance(end) * 2)
                )
        );

        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()
                        || !target.isOnline()
                        || !player.getWorld().equals(world)
                        || !target.getWorld().equals(world)) {
                    cancel();
                    return;
                }

                double progress = Math.min(
                        1.0,
                        tick / (double) totalTicks
                );

                // 대상에서 사용자 방향으로 이동하는 중심 좌표
                Location center = start.clone()
                        .add(travel.clone().multiply(progress))
                        .toLocation(world);

                // 하나의 구체 안에 빨간색과 초록색을 혼합
                spawnMixedSphere(world, center);

                if (tick >= totalTicks) {
                    cancel();
                    return;
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * 빨간색과 초록색 REDSTONE 파티클을 같은 구체 표면에
     * 번갈아 배치한다.
     */
    private void spawnMixedSphere(World world, Location center) {
        double radius = 0.12;
        int points = 30;

        Particle.DustOptions green = new Particle.DustOptions(
                Color.fromRGB(21, 183, 0),
                0.8f
        );

        Particle.DustOptions red = new Particle.DustOptions(
                Color.fromRGB(198, 0, 0),
                0.8f
        );

        for (int i = 0; i < points; i++) {
            // 구체 표면에 파티클을 고르게 배치
            double y = 1.0 - 2.0 * (i + 0.5) / points;
            double ringRadius = Math.sqrt(1.0 - y * y);
            double angle = i * Math.PI * (3.0 - Math.sqrt(5.0));

            double x = Math.cos(angle) * ringRadius;
            double z = Math.sin(angle) * ringRadius;

            Location particleLocation = center.clone().add(
                    x * radius,
                    y * radius,
                    z * radius
            );

            // 하나의 구체 표면에서 색상 교차 배치
            Particle.DustOptions dust =
                    (i % 2 == 0) ? green : red;

            world.spawnParticle(
                    Particle.REDSTONE,
                    particleLocation,
                    1,
                    0, 0, 0,
                    0,
                    dust
            );
        }
    }

    @SuppressWarnings("deprecation")
    private void healPlayer(Player player, double amount) {
        double maxHealth = player.getMaxHealth();

        player.setHealth(
                Math.min(
                        maxHealth,
                        player.getHealth() + amount
                )
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