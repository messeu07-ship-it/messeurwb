package com.example.exampleaddon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.weapons.BaseWeapon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class HighJumpKick implements BaseWeapon, Listener{

	private final RandomWeaponWar plugin;
    private final NamespacedKey itemKey;

    private double attackDamage;
    private double attackSpeed;
    private int misspercent;
    private double penaltydamage;
    private final Map<UUID, Boolean> tasks = new HashMap<>();
    
    public HighJumpKick(RandomWeaponWar plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "HighJumpKick_item");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadConfig();
    }
    
    @Override
    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        	attackDamage = cfg.getDouble("addons.HighJumpKick.attack-damage", 10);
        	attackSpeed  = cfg.getDouble("addons.HighJumpKick.attack-speed", -1);
        	misspercent  = cfg.getInt("addons.HighJumpKick.miss-percent", 15);
        	penaltydamage = cfg.getDouble("addons.HighJumpKick.penalty-damage", 15.0);
    	}
    
    @Override
    public String getId() {
        return "무릎차기";
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
        ItemStack item = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemMeta meta = item.getItemMeta();

        Component displayName = Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(
                        Component.text("[ ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("HIDDEN")
                                .color(NamedTextColor.DARK_RED)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text(" ] ")
                                .color(NamedTextColor.DARK_GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(
                        Component.text("무릎차기")
                                .color(NamedTextColor.GOLD)
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
        lore.add(
                Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text("『L』 ")
                                        .color(NamedTextColor.DARK_GRAY)
                        )
                        .append(
                                Component.text(
                                        "동시에 타격으로 피해를 주지 못했다면 " + penaltydamage + "의 피해를 받습니다."
                                ).color(NamedTextColor.GRAY)
                        )
        );

        meta.lore(lore);

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "HighJumpKick_attack_damage",
                        attackDamage,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );

        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "HighJumpKick_attack_speed",
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
        if (item == null || item.getType() != Material.NETHERITE_SHOVEL) {
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
    
    private boolean isMainHand(Player player) {
        return isWeaponItem(
                player.getInventory().getItemInMainHand()
        );
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
    	
    	
    	
    	if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // 좌클릭만 처리
        Action action = e.getAction();

        if (action != Action.LEFT_CLICK_AIR
                && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = e.getPlayer();

        // 주 손에 무릎차기를 들고 있고 허기 효과가 없을 때만 실행
        
        if (tasks.get(player.getUniqueId()) == null) {
    		tasks.put(e.getPlayer().getUniqueId(), false);
    		}

        if (!isMainHand(player)
                || player.hasPotionEffect(PotionEffectType.HUNGER)
                || tasks.get(player.getUniqueId())) {
            return;
        }	
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_BIG_FALL, SoundCategory.PLAYERS, 1.0f, 1.3f);
    		e.getPlayer().sendMessage(
                    Component.text(
                            e.getPlayer().getPlayerListName() + "은(는) 의욕이 넘쳐서 땅에 부딪쳤다!"
                    ).color(NamedTextColor.GRAY)
            );
    		e.getPlayer().damage(penaltydamage);      		
    		
    		tasks.put(e.getPlayer().getUniqueId(), true);
    		new BukkitRunnable() {
    			
    			@Override
				public void run() {		
    				what(e.getPlayer());
					cancel();
					return;
				}				
				
				
    		}.runTaskTimer(plugin, 2L, 2L);  		
    	}
    
    private void what(Player p) {
    	tasks.put(p.getUniqueId(), false);   
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler
	private void ondamage2(EntityDamageByEntityEvent event) {
    	if (event.getCause() == DamageCause.ENTITY_ATTACK) {
		if (event.getDamager() instanceof Player p && isMainHand(p) && !p.hasPotionEffect(PotionEffectType.HUNGER)) {
			if (Math.random() < ((double) misspercent) / 100.0) {
				event.setCancelled(true);
			}
			if (event.isCancelled()) {
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BIG_FALL, SoundCategory.PLAYERS, 1.0f, 1.3f);
				 p.sendMessage(
		                    Component.text(
		                    		 p.getPlayerListName() + "은(는) 의욕이 넘쳐서 땅에 부딪쳤다!"
		                    ).color(NamedTextColor.GRAY)
		            );
				 p.damage(penaltydamage);
			}
		}
		}
	}

	
}
