
package com.example.exampleaddon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Overheal implements BaseWeapon, Listener {

    private final RandomWeaponWar plugin;
    private final NamespacedKey itemKey;

    // Config 설정값
    private double intervalSeconds;
    private double absorptionPerInterval;
    private double maxAbsorption;

    // 초 단위를 틱으로 변환한 값
    private long intervalTicks;

    // 플레이어별 체력 최대 유지 시간
    private final Map<UUID, Long> tickCounters = new HashMap<>();

    public Overheal(RandomWeaponWar plugin) {
        this.plugin = plugin;

        this.itemKey = new NamespacedKey(
                plugin,
                "overheal_item"
        );

        loadConfig();

        // 이벤트 등록
        plugin.getServer().getPluginManager()
                .registerEvents(this, plugin);

        // 패시브 자동 확인
        new BukkitRunnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    ItemStack mainHand =
                            player.getInventory().getItemInMainHand();

                    // 주 손에 초과회복이 없으면 진행 시간 초기화
                    if (!isWeaponItem(mainHand)) {
                        tickCounters.remove(uuid);
                        continue;
                    }

                    // 체력이 최대가 아니면 진행 시간 초기화
                    if (player.getHealth() + 0.000001
                            < player.getMaxHealth()) {
                        tickCounters.put(uuid, 0L);
                        continue;
                    }

                    // 체력이 최대인 상태에서 시간 누적
                    long ticks = tickCounters.getOrDefault(uuid, 0L) + 1L;

                    if (ticks >= intervalTicks) {
                        ticks = 0L;

                        double absorption =
                                player.getAbsorptionAmount();

                        // 최대 흡수 체력 제한
                        if (absorption < maxAbsorption) {
                            double newAbsorption = Math.min(
                                    maxAbsorption,
                                    absorption + absorptionPerInterval
                            );
                            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                            player.setAbsorptionAmount(newAbsorption);
                        }
                    }

                    tickCounters.put(uuid, ticks);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();

        intervalSeconds = cfg.getDouble(
                "addons.Overheal.interval-seconds",
                3.0
        );

        absorptionPerInterval = cfg.getDouble(
                "addons.Overheal.absorption-per-interval",
                1.0
        );

        maxAbsorption = cfg.getDouble(
                "addons.Overheal.max-absorption",
                20.0
        );

        // 1초 = 20틱, 최소 1틱
        intervalTicks = Math.max(
                1L,
                Math.round(Math.max(0.05, intervalSeconds) * 20.0)
        );

        // 음수 설정값 방지
        absorptionPerInterval = Math.max(
                0.0,
                absorptionPerInterval
        );

        maxAbsorption = Math.max(
                0.0,
                maxAbsorption
        );
    }

    @Override
    public String getId() {
        return "초과회복";
    }

    @Override
    public int getUseCount(UUID uuid) {
        return 0;
    }

    @Override
    public int getPenaltyCount() {
        return Integer.MAX_VALUE;
    }

    // 아이템 생성
    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PINK_DYE);
        ItemMeta meta = item.getItemMeta();

        Component displayName = Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(
                        Component.text("[ ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("B")
                                .color(NamedTextColor.AQUA)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text(" ] ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("초과회복")
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.BOLD)
                );

        meta.displayName(displayName);

        List<Component> lore = List.of(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text("『P』 ")
                                        .color(NamedTextColor.DARK_GRAY)
                        )
                        .append(
                                Component.text(
                                        "체력이 최대인 상태에서 "
                                                + formatNumber(intervalSeconds)
                                                + "초마다 흡수 체력을 "
                                                + formatNumber(absorptionPerInterval)
                                                + "씩 최대 " + maxAbsorption + "까지 획득합니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        meta.lore(lore);

        // 커스텀 아이템 식별 태그
        meta.getPersistentDataContainer().set(
                itemKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    // 소수점이 불필요하면 정수로 표시
    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }

        return String.valueOf(value);
    }

    // 초과회복 아이템인지 판별
    @Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null || item.getType() != Material.PINK_DYE) {
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

    // 패시브 아이템이므로 우클릭 기능 없음
    @Override
    public void onRightClick(Player player) {
    }

    // 플레이어 접속 종료 시 데이터 정리
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        tickCounters.remove(event.getPlayer().getUniqueId());
    }
}
