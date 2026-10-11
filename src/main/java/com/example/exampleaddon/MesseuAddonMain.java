package com.example.exampleaddon;

import com.randomweaponwar.RandomWeaponWar;
import com.randomweaponwar.addon.RwwAddon;
import com.randomweaponwar.managers.CooldownManager;
import com.randomweaponwar.weapons.BaseWeapon;

import java.util.List;

/**
 * 예시 애드온 진입점. addon.yml 의 main 항목이 이 클래스를 가리킨다.
 * 반드시 기본 생성자(인자 없음)가 있어야 AddonManager 가 리플렉션으로 생성할 수 있다.
 */
public class MesseuAddonMain implements RwwAddon {

    @Override
    public List<BaseWeapon> createWeapons(RandomWeaponWar plugin, CooldownManager cooldownManager) {
        return List.of(
                new Powerdrain(plugin, cooldownManager),
                new Circularbeads(plugin, cooldownManager),
                new DestroyPhenomenon(plugin, cooldownManager)
        );
    }

    @Override
    public String getName() {
        return "MesseuAddon";
    }
    
    
}
