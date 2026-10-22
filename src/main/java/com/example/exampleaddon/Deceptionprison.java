package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Deceptionprison implements BaseWeapon, Listener {
	
	private final RandomWeaponWar plugin;
    private final CooldownManager cooldownManager;
    private final NamespacedKey itemKey;
    
    private double attackDamage;
    private double attackSpeed;
    
    private int stackThreshold;

    private double rightTeleportDistance;
    private int rightBuffDurationSeconds;
    private int rightSpeedLevel;
    private double rightAttackDamageBonus;
    private long rightReturnDelaySeconds;
    private long rightCooldownMs;

    private int shiftInvisibilityDurationSeconds;
    private int shiftStunDurationSeconds;
    private int shiftPostStunBuffDurationSeconds;
    private int shiftSpeedLevel;
    private int shiftStrengthLevel;
    private long shiftHitTimeoutSeconds;
    private double shiftMissPenaltyDamage;
    private int shiftGlowingDurationSeconds;
    private long shiftCooldownMs;
    
    private final Map<UUID, Double> MOVE = new HashMap<>();
    private final Map<UUID, Location> beforeLocations = new HashMap<>();
    private final Map<UUID, BossBar> bossbars = new HashMap<>();   
    
    // GUI를 연 플레이어 UUID -> 선택한 대상 플레이어 UUID
    private final Map<UUID, UUID> selectedTargets = new HashMap<>();
    private final List<UUID> buff = new ArrayList<>();
    private final List<UUID> penalty = new ArrayList<>();
    private final List<UUID> shiftbuff = new ArrayList<>();
    
    private static final int PLAYER_SELECT_GUI_SIZE = 54;
    private static final String PLAYER_SELECT_GUI_TITLE = "플레이어 선택";

    private static final String SHIFTRIGHT_CD_ID = "쉬우";
    private static final String RIGHT_CD_ID = "우";

    
    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        
        attackDamage = cfg.getDouble("addons.deceptionprison.attack-damage", 2.0);
        attackSpeed = cfg.getDouble("addons.deceptionprison.attack-speed", -0.1);


        // 패시브

        stackThreshold = cfg.getInt(
                "addons.deceptionprison.passive.stack-threshold", 401
        );

        // 일반 우클릭
        rightTeleportDistance = cfg.getDouble(
                "addons.deceptionprison.right-click.teleport-distance", 7.0
        );

        rightBuffDurationSeconds = cfg.getInt(
                "addons.deceptionprison.right-click.buff-duration-seconds", 5
        ) * 20;

        rightSpeedLevel = cfg.getInt(
                "addons.deceptionprison.right-click.speed-level", 2
        ) - 1;

        rightAttackDamageBonus = cfg.getDouble(
                "addons.deceptionprison.right-click.attack-damage-bonus", 3.0
        );

        rightReturnDelaySeconds = cfg.getLong(
                "addons.deceptionprison.right-click.return-delay-seconds", 5
        ) * 20L;

        rightCooldownMs = cfg.getLong(
                "addons.deceptionprison.right-click.cooldown-seconds", 31
        ) * 1000L;

        // 쉬프트 우클릭
        shiftInvisibilityDurationSeconds = cfg.getInt(
                "addons.deceptionprison.shift-right-click.invisibility-duration-seconds", 8
        ) * 20;

        shiftStunDurationSeconds = cfg.getInt(
                "addons.deceptionprison.shift-right-click.stun-duration-seconds", 3
        ) * 20;

        shiftPostStunBuffDurationSeconds = cfg.getInt(
                "addons.deceptionprison.shift-right-click.post-stun-buff-duration-seconds", 5
        ) * 20;

        shiftSpeedLevel = cfg.getInt(
                "addons.deceptionprison.shift-right-click.speed-level", 5
        ) - 1;

        shiftStrengthLevel = cfg.getInt(
                "addons.deceptionprison.shift-right-click.strength-level", 2
        ) - 1;

        shiftHitTimeoutSeconds = cfg.getLong(
                "addons.deceptionprison.shift-right-click.hit-timeout-seconds", 8
        ) * 20L;

        shiftMissPenaltyDamage = cfg.getDouble(
                "addons.deceptionprison.shift-right-click.miss-penalty-damage", 15.0
        );

        shiftGlowingDurationSeconds = cfg.getInt(
                "addons.deceptionprison.shift-right-click.glowing-duration-seconds", 6
        ) * 20;

        shiftCooldownMs = cfg.getLong(
                "addons.deceptionprison.shift-right-click.cooldown-seconds", 70
        ) * 1000L;
    }
	
	public Deceptionprison(RandomWeaponWar plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.itemKey = new NamespacedKey(plugin, "Deceptionprison_item");
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    ItemStack mainHand =
                            player.getInventory().getItemInMainHand();

                    if (!isWeaponItem(mainHand)
                            || player.hasPotionEffect(PotionEffectType.HUNGER)) {

                        beforeLocations.remove(uuid);

                        BossBar bar = bossbars.remove(uuid);
                        if (bar != null) {
                            bar.removeAll();
                        }

                        continue;
                    }

                    // 이동 스택 초기화
                    MOVE.putIfAbsent(uuid, 0.0);

                    Location currentLocation =
                            player.getLocation().clone();

                    Location beforelocation =
                            beforeLocations.put(uuid, currentLocation);

                    // 최초 측정 시에는 이동 거리 계산을 생략
                    if (beforelocation != null
                            && beforelocation.getWorld() != null
                            && beforelocation.getWorld().equals(
                                    currentLocation.getWorld())) {

                        double distance =
                                beforelocation.distance(currentLocation);

                        MOVE.put(
                                uuid,
                                MOVE.getOrDefault(uuid, 0.0) + distance
                        );
                    }

                    double move = MOVE.getOrDefault(uuid, 0.0);
                    double threshold = Math.max(1.0, stackThreshold);

                    // 보스바 생성
                    BossBar bossbar = bossbars.get(uuid);

                    if (bossbar == null) {
                        bossbar = Bukkit.createBossBar(
                                "",
                                BarColor.PURPLE,
                                BarStyle.SOLID
                        );

                        bossbars.put(uuid, bossbar);
                        bossbar.addPlayer(player);
                    }

                    // Component 형식의 보스바 텍스트
                    Component title = Component.empty()
                            .append(
                                    Component.text("{")
                                            .color(NamedTextColor.DARK_PURPLE)
                                            .decorate(TextDecoration.BOLD)
                                            .decorate(TextDecoration.ITALIC)
                                            .decorate(TextDecoration.STRIKETHROUGH)
                            )
                            .append(
                                    Component.text("이")
                                            .color(NamedTextColor.DARK_GRAY)
                                            .decorate(TextDecoration.BOLD)
                                            .decorate(TextDecoration.ITALIC)
                                            .decorate(TextDecoration.STRIKETHROUGH)
                            )
                            .append(
                                    Component.text("미")
                                            .color(NamedTextColor.GRAY)
                                            .decorate(TextDecoration.BOLD)
                                            .decorate(TextDecoration.ITALIC)
                                            .decorate(TextDecoration.STRIKETHROUGH)
                            )
                            .append(
                                    Component.text("...")
                                            .color(NamedTextColor.DARK_GRAY)
                                            .decorate(TextDecoration.BOLD)
                                            .decorate(TextDecoration.ITALIC)
                                            .decorate(TextDecoration.STRIKETHROUGH)
                            )
                            .append(
                                    Component.text("}")
                                            .color(NamedTextColor.DARK_PURPLE)
                                            .decorate(TextDecoration.BOLD)
                                            .decorate(TextDecoration.ITALIC)
                                            .decorate(TextDecoration.STRIKETHROUGH)
                            )
                            .append(
                                    Component.text(" : ")
                                            .color(NamedTextColor.DARK_GRAY)
                                            .decorate(TextDecoration.BOLD)
                            )
                            .append(
                                    Component.text(String.format("%.1f", move))
                                            .color(NamedTextColor.DARK_PURPLE)
                                            .decorate(TextDecoration.BOLD)
                            );

                    bossbar.setTitle(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(title));

                    // 진행도 갱신
                    bossbar.setProgress(
                            Math.max(0.0, Math.min(1.0, move / threshold))
                    );

                    // 스택이 목표치에 도달하면 초기화
                    if (move >= threshold) {
                        MOVE.put(uuid, 0.0);

                        cooldownManager.clearCooldown(RIGHT_CD_ID, uuid);
                        cooldownManager.clearCooldown(SHIFTRIGHT_CD_ID, uuid);

                        player.playSound(
                                player.getLocation(),
                                Sound.ENTITY_ENDERMAN_DEATH,
                                SoundCategory.PLAYERS,
                                1.0f,
                                1.2f
                        );

                        player.sendMessage(
                                Component.text("쿨타임이 초기화 되었습니다...")
                                        .color(NamedTextColor.DARK_GRAY)
                        );

                        // 초기화된 스택을 보스바에도 즉시 반영
                        bossbar.setProgress(0.0);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        }
	
	
	
	 @Override
	    public String getId() {
	        return "기만으로의 유폐";
	    }
	    
	    public int getUseCount(UUID uuid) {
	        return 0;
	    }

	    public int getPenaltyCount() {
	        return Integer.MAX_VALUE;
	    }
	
	
	   
	private Component gradientText(
	        String text,
	        int[] colors,
	        TextDecoration... decorations
	) {
	    if (text.length() != colors.length) {
	        throw new IllegalArgumentException(
	                "텍스트 글자 수와 색상 배열 길이가 다릅니다: "
	                        + text + " ("
	                        + text.length() + " / "
	                        + colors.length + ")"
	        );
	    }

	    Component result = Component.empty()
	            .decoration(TextDecoration.ITALIC, false);

	    for (int i = 0; i < text.length(); i++) {
	        int rgb = colors[i];

	        Component character = Component.text(
	                String.valueOf(text.charAt(i))
	        ).color(
	                TextColor.color(
	                        (rgb >> 16) & 0xFF,
	                        (rgb >> 8) & 0xFF,
	                        rgb & 0xFF
	                )
	        ).decoration(TextDecoration.ITALIC, false);

	        for (TextDecoration decoration : decorations) {
	            character = character.decorate(decoration);
	        }

	        result = result.append(character);
	    }

	    return result;
	}
	
	 private Component gradientText(
	            String text,
	            int startColor,
	            int endColor,
	            TextDecoration... decorations
	    ) {
	        int[] colors = new int[text.length()];

	        int startR = (startColor >> 16) & 0xFF;
	        int startG = (startColor >> 8) & 0xFF;
	        int startB = startColor & 0xFF;

	        int endR = (endColor >> 16) & 0xFF;
	        int endG = (endColor >> 8) & 0xFF;
	        int endB = endColor & 0xFF;

	        for (int i = 0; i < text.length(); i++) {
	            double ratio = text.length() <= 1
	                    ? 0.0
	                    : (double) i / (text.length() - 1);

	            int r = (int) Math.round(startR + (endR - startR) * ratio);
	            int g = (int) Math.round(startG + (endG - startG) * ratio);
	            int b = (int) Math.round(startB + (endB - startB) * ratio);

	            colors[i] = (r << 16) | (g << 8) | b;
	        }

	        return gradientText(text, colors, decorations);
	    }
	    
	
	@Override
    public ItemStack createItem() {
		
		ItemStack item = new ItemStack(Material.DROPPER);
        ItemMeta meta = item.getItemMeta();
	
	// 무기 이름: 기만으로의 유폐
	Component displayName = Component.empty()
	        .decoration(TextDecoration.ITALIC, false)
	        .append(
	                Component.text("[ ")
	                        .color(NamedTextColor.DARK_GRAY)
	                        .decorate(TextDecoration.BOLD)
	        )
	        .append(
	                Component.text("M")
	                        .color(TextColor.color(54, 52, 230))
	                        .decorate(TextDecoration.BOLD)
	        )
	        .append(
	                Component.text("y")
	                        .color(TextColor.color(92, 42, 208))
	                        .decorate(TextDecoration.BOLD)
	        )
	        .append(
	                Component.text("t")
	                        .color(TextColor.color(130, 31, 187))
	                        .decorate(TextDecoration.BOLD)
	        )
	        .append(
	                Component.text("h")
	                        .color(TextColor.color(169, 21, 165))
	                        .decorate(TextDecoration.BOLD)
	        )
	        .append(
	                Component.text("i")
	                        .color(TextColor.color(207, 10, 144))
	                        .decorate(TextDecoration.BOLD)
	        )
	        .append(
	                Component.text("c")
	                        .color(TextColor.color(245, 0, 122))
	                        .decorate(TextDecoration.BOLD)
	        )
	        .append(
	                Component.text(" ] ")
	                        .color(NamedTextColor.DARK_GRAY)
	                        .decorate(TextDecoration.BOLD)
	        ).append(gradientText(
	        "기만으로의 유폐",
	        
	        		0x373737, 0xE1E600
	        ,
	        TextDecoration.BOLD,
	        TextDecoration.ITALIC,
	        TextDecoration.STRIKETHROUGH
	));

	meta.displayName(displayName);

	List<Component> lore = new ArrayList<>();

	// ── 패시브 ──

	// ── 패시브 ──

	lore.add(
	    Component.empty()
	        .decoration(TextDecoration.ITALIC, false)
	        .append(Component.text("『P』 ").color(NamedTextColor.DARK_GRAY))
	        .append(Component.text("어디에도 존재하지 않는")
	                .color(NamedTextColor.DARK_GRAY))
	);

	lore.add(
	    Component.text(
	        "   어떤 방식으로든 좌표가 변경된다면 그 거리만큼 ").color(NamedTextColor.GRAY)
	    .append(
                Component.text("{")
                        .color(NamedTextColor.DARK_PURPLE)
                        .decorate(TextDecoration.BOLD)
                        .decorate(TextDecoration.ITALIC)
                        .decorate(TextDecoration.STRIKETHROUGH)
        )
        .append(
                Component.text("이")
                        .color(NamedTextColor.DARK_GRAY)
                        .decorate(TextDecoration.BOLD)
                        .decorate(TextDecoration.ITALIC)
                        .decorate(TextDecoration.STRIKETHROUGH)
        )
        .append(
                Component.text("미")
                        .color(NamedTextColor.GRAY)
                        .decorate(TextDecoration.BOLD)
                        .decorate(TextDecoration.ITALIC)
                        .decorate(TextDecoration.STRIKETHROUGH)
        )
        .append(
                Component.text("...")
                        .color(NamedTextColor.DARK_GRAY)
                        .decorate(TextDecoration.BOLD)
                        .decorate(TextDecoration.ITALIC)
                        .decorate(TextDecoration.STRIKETHROUGH)
        )
        .append(
                Component.text("}")
                        .color(NamedTextColor.DARK_PURPLE)
                        .decorate(TextDecoration.BOLD)
                        .decorate(TextDecoration.ITALIC)
                        .decorate(TextDecoration.STRIKETHROUGH)
        ).append(
                    Component.text("가 증가합니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	        )
	);

	lore.add(
	    Component.text("   ") .append(
                Component.text("{")
                .color(NamedTextColor.DARK_PURPLE)
                .decorate(TextDecoration.BOLD)
                .decorate(TextDecoration.ITALIC)
                .decorate(TextDecoration.STRIKETHROUGH)
)
.append(
        Component.text("이")
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.BOLD)
                .decorate(TextDecoration.ITALIC)
                .decorate(TextDecoration.STRIKETHROUGH)
)
.append(
        Component.text("미")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.BOLD)
                .decorate(TextDecoration.ITALIC)
                .decorate(TextDecoration.STRIKETHROUGH)
)
.append(
        Component.text("...")
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.BOLD)
                .decorate(TextDecoration.ITALIC)
                .decorate(TextDecoration.STRIKETHROUGH)
)
.append(
        Component.text("}")
                .color(NamedTextColor.DARK_PURPLE)
                .decorate(TextDecoration.BOLD)
                .decorate(TextDecoration.ITALIC)
                .decorate(TextDecoration.STRIKETHROUGH)
).append(
                Component.text("가 " + stackThreshold
	        + "에 도달하면 자동으로 전부 소모하여 이 무기의 쿨타임을 즉시 초기화합니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	)
	    
	);
	
	lore.add(Component.text(
	        "   ")
            .append(
	        gradientText(
	                "\"이미... 저라는걸 유지할 수 없게 되버렸습니다...\"",
	                0x373737, 0xE1E600,
	                TextDecoration.BOLD,
	                TextDecoration.ITALIC,
	                TextDecoration.STRIKETHROUGH
	        )
	        )
	);
	
	

	// ── 우클릭 ──

	lore.add(
	    Component.empty()
	        .decoration(TextDecoration.ITALIC, false)
	        .append(Component.text("『R』 ").color(NamedTextColor.DARK_GRAY))
	        .append(Component.text("허상처럼 사라져버리는")
	                .color(NamedTextColor.WHITE))
	);

	lore.add(
	    Component.text(
	        "   선택한 플레이어로부터 뒤로 "
	        + rightTeleportDistance
	        + "칸 떨어진 위치에 순간이동합니다. (순간이동 위치에 블럭이 있거나 자기장 밖일 경우 더 가까운 위치로 순간이동합니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	);

	lore.add(
	    Component.text(
	        "   순간이동 직후 " + rightBuffDurationSeconds / 20
	        + "초간 은신과 신속 " + (rightSpeedLevel + 1)
	        + "가 부여되고 주는 피해가 "
	        + rightAttackDamageBonus + " 증가합니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	);

	lore.add(
	    Component.text("   " +
	        rightReturnDelaySeconds / 20 + "초 후 원래 위치로 되돌아갑니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	);
	
	lore.add(Component.text(
	        "   ")
            .append(
	        gradientText(
	                "\"제가 어딘가에 있었다면... 저는 더이상 그곳에 없습니다...\"",
	                0x373737, 0xE1E600,
	                TextDecoration.BOLD,
	                TextDecoration.ITALIC,
	                TextDecoration.STRIKETHROUGH
	        )
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
	                        Component.text(rightCooldownMs / 1000)
	                                .color(NamedTextColor.GRAY)
	                )
	);

	// ── 쉬프트 우클릭 ──

	lore.add(
	    Component.empty()
	        .decoration(TextDecoration.ITALIC, false)
	        .append(Component.text("『SR』 ").color(NamedTextColor.DARK_GRAY))
	        .append(Component.text("기회와 대가")
	                .color(NamedTextColor.DARK_PURPLE))
	);

	lore.add(
	    Component.text(
	        "   " + shiftInvisibilityDurationSeconds / 20
	        + "초간 은신하고 " + shiftStunDurationSeconds / 20 + "초간 좌표가 고정됩니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	);

	lore.add(
	    Component.text(
	        "   좌표 고정 종료 후 " + shiftPostStunBuffDurationSeconds / 20
	        + "초간 신속 " + (shiftSpeedLevel + 1)
	        + "와 힘 " + (shiftStrengthLevel + 1)
	        + "가 부여되고 타격 시 상대를 밀쳐냅니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	);

	lore.add(
	    Component.text(
	        "   능력 사용 후 " + shiftHitTimeoutSeconds / 20
	        + "초간 누구도 타격하지 못했다면 자신에게 "
	        + shiftMissPenaltyDamage
	        + "의 피해를 주고 "
	        + shiftGlowingDurationSeconds / 20 + "초간 발광합니다."
	    ).color(NamedTextColor.GRAY)
	        .decoration(TextDecoration.ITALIC, false)
	);
	
	lore.add(Component.text(
	        "   ")
            .append(
	        gradientText(
	                "\"쉽게 바스라질것만 같은... 나약한 기회더라도...!\"",
	                0x373737, 0xE1E600,
	                TextDecoration.BOLD,
	                TextDecoration.ITALIC,
	                TextDecoration.STRIKETHROUGH
	        )
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
	                        Component.text(shiftCooldownMs / 1000)
	                                .color(NamedTextColor.GRAY)
	                )
	);
	
	lore.add(
	        gradientText(
	                "\"기만만을 내뱉던 자는... 결국 자신마저 기만에 가둬버리고 말았습니다...\"",
	                0x373737, 0xE1E600,
	                TextDecoration.BOLD,
	                TextDecoration.ITALIC,
	                TextDecoration.STRIKETHROUGH
	        )
	);
	
	meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
            new AttributeModifier(UUID.randomUUID(), "deceptionprison_attack_damage",
                    attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
    meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
            new AttributeModifier(UUID.randomUUID(), "deceptionprison_attack_speed",
                    attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

    meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
    meta.setUnbreakable(true);
    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
	meta.lore(lore);
	item.setItemMeta(meta);
	return item;
	
}
	
	@Override
    public boolean isWeaponItem(ItemStack item) {
        if (item == null || item.getType() != Material.DROPPER) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }
    
    @Override
    public void onRightClick(Player player) {
        UUID uuid = player.getUniqueId();

        if (player.isSneaking()) {
            if (cooldownManager.isOnCooldown(SHIFTRIGHT_CD_ID, uuid)) {
                long remaining =
                        (cooldownManager.getRemainingMillis(SHIFTRIGHT_CD_ID, uuid) + 999) / 1000;

                player.sendMessage(
                        Component.text("기회와 대가 남은 쿨타임 : " + remaining + "초")
                                .color(NamedTextColor.RED)
                );
                return;
            }

            PotionEffect invisible = new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    shiftInvisibilityDurationSeconds,
                    10
            );
            player.addPotionEffect(invisible);
            stun(player);
            cooldownManager.setCooldown(SHIFTRIGHT_CD_ID, uuid, shiftCooldownMs);
            penalty.add(player.getUniqueId());
	    	new BukkitRunnable() {				
				@Override
				public void run() {
					if (penalty.contains(player.getUniqueId())) {
						 player.getWorld().playSound(
					                player.getLocation(),
					                Sound.ENTITY_ELDER_GUARDIAN_CURSE,
					                SoundCategory.PLAYERS,
					                1.0f,
					                0.7f
					        );
						player.damage(shiftMissPenaltyDamage);
						PotionEffect glow = new PotionEffect(
		    	                PotionEffectType.GLOWING,
		    	                shiftGlowingDurationSeconds,
		    	                10
		    	        );
						player.addPotionEffect(glow);
						player.sendMessage(
		   	                    Component.text("대가를 받았습니다...")
		   	                            .color(NamedTextColor.DARK_RED)
		   	            );
						penalty.remove(player.getUniqueId());
					}
					shiftbuff.remove(player.getUniqueId());
					cancel();
					return;
				}
			}.runTaskLater(plugin, shiftHitTimeoutSeconds);
        }
        else {
        // 일반 우클릭은 먼저 대상을 선택하는 GUI를 엽니다.
        if (cooldownManager.isOnCooldown(RIGHT_CD_ID, uuid)) {
            long remaining =
                    (cooldownManager.getRemainingMillis(RIGHT_CD_ID, uuid) + 999) / 1000;

            player.sendMessage(
                    Component.text("허상처럼 사라져버리는 남은 쿨타임 : " + remaining + "초")
                            .color(NamedTextColor.RED)
            );
            return;
        }

        openPlayerSelectGUI(player);
    }}
    
    private void stun(Player p) {
    	Location loc = p.getLocation();	
    	new BukkitRunnable() {
    		int duration = 0;
			@Override
			public void run() {
				if (p.isDead()) {
					cancel();
					return;
				}
				p.teleport(loc);
				duration++;
				if (duration >= shiftStunDurationSeconds) {
					shiftduration(p);
					cancel();
					return;
				}
			}
		}.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void shiftduration(Player p) {
    	    	PotionEffect speed = new PotionEffect(
    	                PotionEffectType.SPEED,
    	                shiftPostStunBuffDurationSeconds,
    	                shiftSpeedLevel
    	        );
    	    	PotionEffect streng = new PotionEffect(
    	                PotionEffectType.INCREASE_DAMAGE,
    	                shiftPostStunBuffDurationSeconds,
    	                shiftStrengthLevel
    	        );
    	    	 p.addPotionEffect(speed);
    	    	 p.addPotionEffect(streng);    	    	
    	    	shiftbuff.add(p.getUniqueId());
    	    	
    	    	
    }
    
    /** 현재 온라인이며 관전자가 아닌 플레이어 목록을 담은 큰 상자 GUI를 엽니다. */
    @SuppressWarnings("deprecation")
	private void openPlayerSelectGUI(Player viewer) {
        PlayerSelectHolder holder = new PlayerSelectHolder();
        Inventory inventory = Bukkit.createInventory(
                holder,
                PLAYER_SELECT_GUI_SIZE,
                PLAYER_SELECT_GUI_TITLE
        );
        holder.setInventory(inventory);

        List<Player> candidates = new ArrayList<>(viewer.getWorld().getPlayers());
        candidates.removeIf(target ->
                !target.isOnline() || target.getGameMode() == GameMode.SPECTATOR || target.hasPotionEffect(PotionEffectType.LUCK) || target == viewer
        );
        candidates.sort((first, second) ->
                String.CASE_INSENSITIVE_ORDER.compare(first.getName(), second.getName())
        );
        
        if (candidates.isEmpty()) {
            viewer.sendMessage(
                    Component.text("선택할 수 있는 플레이어가 없습니다.")
                            .color(NamedTextColor.RED)
            );
            return;
        }

        int slot = 0;
        for (Player target : candidates) {
            if (slot >= PLAYER_SELECT_GUI_SIZE) {
                break;
            }

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta == null) {
                continue;
            }

            skullMeta.setOwningPlayer(target);
            skullMeta.displayName(
                    Component.text(target.getName()).color(NamedTextColor.WHITE)
            );
            head.setItemMeta(skullMeta);

            inventory.setItem(slot, head);
            holder.targetsBySlot.put(slot, target.getUniqueId());
            slot++;
        }

        

        viewer.openInventory(inventory);
    }

    @EventHandler
    public void onPlayerSelectInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof PlayerSelectHolder holder)) {
            return;
        }

        // GUI 및 플레이어 인벤토리 모두 아이템 이동을 허용하지 않습니다.
        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player viewer)) {
            return;
        }

        UUID targetId = holder.targetsBySlot.get(event.getRawSlot());
        if (targetId == null) {
            return;
        }

        Player target = Bukkit.getPlayer(targetId);

        // GUI를 연 뒤 관전자로 전환되거나 로그아웃한 경우도 선택을 거부합니다.
        if (target == null || !target.isOnline()
                || target.getGameMode() == GameMode.SPECTATOR) {
            viewer.sendMessage(
                    Component.text("해당 플레이어는 더 이상 선택할 수 없습니다.")
                            .color(NamedTextColor.RED)
            );
            openPlayerSelectGUI(viewer);
            return;
        }

        UUID viewerId = viewer.getUniqueId();

        // 선택한 대상 저장. 이후 스킬 구현에서 getSelectedTarget(viewer)를 사용할 수 있습니다.
        selectedTargets.put(viewerId, targetId);
        viewer.closeInventory();

        // 기존 일반 우클릭 효과는 대상을 선택한 뒤 발동합니다.
        
        
        
        rightduration(viewer, viewer.getLocation(), target.getLocation());
        
        cooldownManager.setCooldown(RIGHT_CD_ID, viewerId, rightCooldownMs);
    }
    
    private void rightduration(Player p, Location l1, Location l2) {
    	PotionEffect speed = new PotionEffect(
                PotionEffectType.SPEED,
                rightBuffDurationSeconds,
                rightSpeedLevel
        );
        PotionEffect invisible = new PotionEffect(
                PotionEffectType.INVISIBILITY,
                rightBuffDurationSeconds,
                10
        );
        p.addPotionEffect(speed);
        p.addPotionEffect(invisible);
        l2.setPitch(0);
        Vector vec = l2.getDirection().clone().normalize().multiply(-1 * rightTeleportDistance);
        Location loc = l2.clone().add(vec);
        for (; (!loc.getBlock().getType().isAir() || !loc.getWorld().getWorldBorder().isInside(loc));) {
        	if (loc.distanceSquared(l2) <= 1.2 * 1.2) break;
        	loc.add(loc.getDirection().normalize().clone().multiply(0.5));
        }
        p.teleport(loc); 
        spawnPurpleCircle(loc.clone().add(0, 1.5, 0), 0.5, 20);
        spawnPurpleCircle(loc.clone().add(0, 0.5, 0), 0.5, 20);
             p.getWorld().playSound(
                p.getLocation(),
                Sound.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                1.0f,
                1.5f
        );
    	buff.add(p.getUniqueId());
    	new BukkitRunnable() {
			@Override
			public void run() {
				if (!buff.contains(p.getUniqueId())) {
					cancel();
					return;
				}
				p.teleport(l1);
				buff.remove(p.getUniqueId());
				cancel();
				return;
			}
		}.runTaskLater(plugin, rightReturnDelaySeconds);
    }
    
    public void spawnPurpleCircle(Location center, double radius, int points) {
        World world = center.getWorld();

        if (world == null || points <= 0) {
            return;
        }

        Particle.DustOptions dust = new Particle.DustOptions(
                Color.fromRGB(160, 32, 240),
                1.2f
        );

        for (int i = 0; i < points; i++) {
            double angle = 2.0 * Math.PI * i / points;

            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY();

            world.spawnParticle(
                    Particle.REDSTONE,
                    x, y, z,
                    0,
                    dust
            );
        }
    }
    
    @EventHandler
   	private void ondamage2(EntityDamageByEntityEvent event) {
   		if (event.getDamager() instanceof Player p) {
   			if (buff.contains(p.getUniqueId())) {
   			p.getWorld().playSound(
                    p.getLocation(),
                    Sound.ENTITY_ENDERMAN_HURT,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.2f
            );
    		p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, event.getEntity().getLocation().clone().add(0, 1.5, 0), 50, 0.5, 0, 0.5);
    		event.setDamage(event.getDamage() + rightAttackDamageBonus);
    		
   		}
   			if (penalty.contains(p.getUniqueId())) {
   				p.sendMessage(
   	                    Component.text("대가로부터 벗어났습니다...")
   	                            .color(NamedTextColor.DARK_PURPLE)
   	            );
   				penalty.remove(p.getUniqueId());
   			}
   			
   			if (shiftbuff.contains(p.getUniqueId())) {
   				p.getWorld().spawnParticle(Particle.PORTAL, event.getEntity().getLocation().clone().add(0, 1, 0), 50, 0.5, 1, 0.5);
   				Vector vec = p.getLocation().clone().getDirection().normalize();
   				event.getEntity().setVelocity(vec.multiply(2.2).setY(0.2));
   			}
    }
    }
    @EventHandler
    public void onPlayerSelectInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PlayerSelectHolder) {
            event.setCancelled(true);
        }
    }
    
    
    
    @EventHandler
    public void onPlayerSelectQuit(PlayerQuitEvent event) {
        UUID quitId = event.getPlayer().getUniqueId();
        selectedTargets.remove(quitId);
        selectedTargets.entrySet().removeIf(entry -> entry.getValue().equals(quitId));
    }

    /** 이 무기를 사용한 플레이어가 마지막으로 선택한 대상. 대상이 관전자/오프라인이면 null입니다. */
    public Player getSelectedTarget(Player selector) {
        UUID targetId = selectedTargets.get(selector.getUniqueId());
        if (targetId == null) {
            return null;
        }

        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()
                || target.getGameMode() == GameMode.SPECTATOR) {
            selectedTargets.remove(selector.getUniqueId());
            return null;
        }

        return target;
    }

    private static final class PlayerSelectHolder implements InventoryHolder {
        private Inventory inventory;
        private final Map<Integer, UUID> targetsBySlot = new HashMap<>();

        private void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    @Override
    public void clearPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        MOVE.remove(uuid);
        beforeLocations.remove(uuid);
        selectedTargets.remove(uuid);
        selectedTargets.entrySet().removeIf(entry -> entry.getValue().equals(uuid));
        buff.remove(uuid);
        penalty.remove(uuid);
        shiftbuff.remove(uuid);
        
        BossBar bar = bossbars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }
    
    
	
}
