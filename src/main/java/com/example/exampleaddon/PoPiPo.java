
package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PoPiPo implements BaseWeapon, Listener {

    private final RandomWeaponWar plugin;
    private final NamespacedKey itemKey;

    private double attackDamage;
    private double attackSpeed;

    // 지속 시간 3틱, 구속 39단계
    private final PotionEffect slow = new PotionEffect(
            PotionEffectType.SLOW,
            2,
            5
    );
    private final PotionEffect speed = new PotionEffect(
            PotionEffectType.SPEED,
            2,
            5
    );

    // 플레이어별 패시브 작업
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public PoPiPo(RandomWeaponWar plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "PoPiPo_item");

        loadConfig();

        // 이벤트 등록
        plugin.getServer().getPluginManager()
                .registerEvents(this, plugin);

        // 주 손에 무기를 들고 있는지 자동 검사
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isMainHand(player) && !player.hasPotionEffect(PotionEffectType.HUNGER)) {
                        startTask(player);
                    } else {
                        stopTask(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();

        attackDamage = cfg.getDouble(
                "addons.PoPiPo.attack-damage",
                3.0
        );

        attackSpeed = cfg.getDouble(
                "addons.PoPiPo.attack-speed",
                -0.9
        );
    }

    @Override
    public String getId() {
        return "폿핏포";
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
        ItemStack item = new ItemStack(Material.GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        Component displayName = Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(
                        Component.text("[ ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("D")
                                .color(NamedTextColor.GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text(" ] ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("폿핏포")
                                .color(NamedTextColor.DARK_GREEN)
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
                                        "2틱마다 신속 5와 구속 5를 번갈아 부여받습니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        meta.lore(lore);

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "PoPiPo_attack_damage",
                        attackDamage,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "PoPiPo_attack_speed",
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
        if (item == null || item.getType() != Material.GLASS_PANE) {
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

 // 패시브 작업 시작
    public void startTask(Player player) {
        UUID uuid = player.getUniqueId();

        if (!player.isOnline() || !isMainHand(player)) {
            return;
        }

        // 이미 실행 중이면 중복 생성하지 않음
        if (tasks.containsKey(uuid)) {
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            private boolean applySlow = true;

            @Override
            public void run() {
                if (!player.isOnline()
                        || !isMainHand(player)
                        || player.hasPotionEffect(PotionEffectType.HUNGER)) {
                    stopTask(player);
                    return;
                }

                if (applySlow) {
                    player.addPotionEffect(slow);
                } else {
                    player.addPotionEffect(speed);
                }

                applySlow = !applySlow;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        tasks.put(uuid, task);
    }
    

    // 패시브 작업 중지
    public void stopTask(Player player) {
        UUID uuid = player.getUniqueId();

        BukkitTask task = tasks.remove(uuid);

        if (task != null) {
            task.cancel();
        }
    }
    
    

    // 다른 클래스에서 작업 상태를 확인할 때 사용
    public boolean hasTask(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    // 플레이어가 나가면 작업 즉시 정리
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopTask(event.getPlayer());
    }
}
