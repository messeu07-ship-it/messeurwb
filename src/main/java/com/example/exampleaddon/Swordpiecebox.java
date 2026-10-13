
package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Swordpiecebox implements BaseWeapon, Listener {

    private final RandomWeaponWar plugin;
    private final CooldownManager cooldownManager;

    // 칼날 조각 주머니 식별 키
    private final NamespacedKey itemKey;

    // 칼날 조각 식별 키
    private final NamespacedKey itemKey2;

    // 칼날 조각의 고유 UUID 저장 키
    private final NamespacedKey itemUniqueKey;

    private double attackDamage;
    private double attackSpeed;
    private int minimum;
    private int maximum;
    private long cooldownMs;

    // 모든 칼날 조각에 동일한 속성 UUID 사용
    private static final UUID DAMAGE_MODIFIER_ID =
            UUID.fromString("d5a18251-c48b-4d7e-9f47-000000000001");

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString("d5a18251-c48b-4d7e-9f47-000000000002");

    public Swordpiecebox(
            RandomWeaponWar plugin,
            CooldownManager cooldownManager
    ) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;

        this.itemKey = new NamespacedKey(
                plugin, "Swordpiecebox_item"
        );

        this.itemKey2 = new NamespacedKey(
                plugin, "Swordpiece_item"
        );

        this.itemUniqueKey = new NamespacedKey(
                plugin, "Swordpiece_unique_id"
        );

        loadConfig();

        // 칼날 조각 타격 이벤트 등록
        plugin.getServer().getPluginManager()
                .registerEvents(this, plugin);
    }

    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();

        attackDamage = cfg.getDouble(
                "addons.swordpiecebox.attack-damage", 2.0
        );

        attackSpeed = cfg.getDouble(
                "addons.swordpiecebox.attack-speed", -3.0
        );

        minimum = cfg.getInt(
                "addons.swordpiecebox.minimum", 1
        );

        maximum = cfg.getInt(
                "addons.swordpiecebox.maximum", 3
        );

        cooldownMs = cfg.getLong(
                "addons.swordpiecebox.cooldown", 10
        ) * 1000L;
    }

    @Override
    public String getId() {
        return "칼날조각 주머니";
    }

    @Override
    public int getUseCount(UUID uuid) {
        return 0;
    }

    @Override
    public int getPenaltyCount() {
        return Integer.MAX_VALUE;
    }

    // 칼날 조각 주머니 생성
    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.GRAY_SHULKER_BOX);
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
                        Component.text("칼날조각 ")
                                .color(TextColor.color(186, 216, 203))
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("주머니")
                                .color(TextColor.color(187, 130, 72))
                                .decorate(TextDecoration.BOLD)
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
                                        "칼날조각을 " + minimum + " ~ "
                                                + maximum + "개 획득합니다."
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

        // 주머니 자체의 공격 능력치
        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "Swordpiecebox_attack_damage",
                        attackDamage,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "Swordpiecebox_attack_speed",
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

    // 주머니인지 판별
    @Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null
                || item.getType() != Material.GRAY_SHULKER_BOX) {
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

    // 칼날 조각 한 개 생성
    private ItemStack createSwordpiece() {
        ItemStack piece = new ItemStack(Material.IRON_NUGGET, 1);
        ItemMeta meta = piece.getItemMeta();

        meta.displayName(
                Component.text("칼날조각")
                        .color(TextColor.color(186, 216, 203))
                        .decoration(TextDecoration.ITALIC, false)
                        .decorate(TextDecoration.BOLD)
        );

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
                                        "타격 시 파괴됩니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        meta.lore(lore);

        // 공격력 +4
        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        DAMAGE_MODIFIER_ID,
                        "Swordpiece_attack_damage",
                        4.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        // 공격 속도 +999
        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        "Swordpiece_attack_speed",
                        999.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        // 칼날 조각임을 표시하는 태그
        meta.getPersistentDataContainer().set(
                itemKey2,
                PersistentDataType.BYTE,
                (byte) 1
        );

        // 각 칼날 조각에 서로 다른 UUID 저장
        // PDC 값이 서로 다르므로 아이템끼리 겹쳐지지 않는다.
        meta.getPersistentDataContainer().set(
                itemUniqueKey,
                PersistentDataType.STRING,
                UUID.randomUUID().toString()
        );
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        piece.setItemMeta(meta);

        return piece;
    }

    // 칼날 조각 주머니 사용
    @Override
    public void onRightClick(Player player) {
        UUID uuid = player.getUniqueId();

        if (cooldownManager.isOnCooldown(getId(), uuid)) {
            long remaining =
                    (cooldownManager.getRemainingMillis(getId(), uuid)
                            + 999) / 1000;

            player.sendMessage(
                    Component.text(
                            "칼날조각 주머니 남은 쿨타임 : "
                                    + remaining + "초"
                    ).color(NamedTextColor.RED)
            );

            return;
        }

        // 최소·최대 개수 정렬
        int low = Math.max(0, Math.min(minimum, maximum));
        int high = Math.max(0, Math.max(minimum, maximum));

        if (high <= 0) {
            player.sendMessage(
                    Component.text("지급할 칼날조각 개수가 없습니다.")
                            .color(NamedTextColor.RED)
            );
            return;
        }

        // 최소와 최대를 모두 포함하여 무작위 개수 결정
        int amount = (int) ThreadLocalRandom.current()
                .nextLong(low, (long) high + 1L);

        if (amount <= 0) {
            return;
        }
        
        int emptySlots = 0;

        for (ItemStack slot : player.getInventory().getStorageContents()) {
            if (slot == null || slot.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        // 지급할 개수만큼 공간이 없으면 사용 취소
        if (emptySlots < amount) {
            player.sendMessage(
                    Component.text("인벤토리에 공간이 없습니다!")
                            .color(NamedTextColor.RED)
            );
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.0f, 0.8f);
            return;
        }
        
     // 매번 새 UUID를 가진 칼날 조각을 하나씩 지급
        for (int i = 0; i < amount; i++) {
            ItemStack reward = createSwordpiece();
            player.getInventory().addItem(reward);
        }

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_ITEM_PICKUP,
                SoundCategory.PLAYERS,
                1.0f,
                1.2f
        );

        player.sendMessage(
                Component.text(
                        "칼날조각 " + amount + "개를 획득했습니다."
                ).color(NamedTextColor.GREEN)
        );

        // 사용 완료 후 쿨타임 적용
        cooldownManager.setCooldown(
                getId(),
                uuid,
                cooldownMs
        );
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSwordpieceHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (weapon.getType() != Material.IRON_NUGGET) {
            return;
        }

        ItemMeta meta = weapon.getItemMeta();

        if (meta == null) {
            return;
        }

        // 일반 철 조각은 소모 대상이 아니다.
        if (!meta.getPersistentDataContainer().has(
                itemKey2,
                PersistentDataType.BYTE
        )) {
            return;
        }
        
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 2.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.ITEM_CRACK, event.getEntity().getLocation().clone().add(0, 2, 0), 35, 0.1, 0, 0.1, new ItemStack(Material.IRON_NUGGET));
            player.getInventory().setItemInMainHand(
                    new ItemStack(Material.AIR));
    }
}
