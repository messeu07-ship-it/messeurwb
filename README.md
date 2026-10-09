# ExampleAddon — 번개의 창

RandomWeaponWar 애드온 예시. 삼지창(TRIDENT) 아이템에 우클릭 시 8칸 내 가장 가까운
상대에게 낙뢰 이펙트 + 고정 데미지를 주는 간단한 무기 하나를 등록합니다.

## 빌드 방법

1. `pom.xml`에 이미 `com.github.LunaEunha` / `RandomWeaponWar` / `v1.0.0` 좌표가
   채워져 있습니다 (https://jitpack.io/#LunaEunha/RandomWeaponWar 빌드 성공 확인됨).
   다른 저장소를 쓰려면 이 부분만 바꾸면 됩니다.
2. 빌드:
   ```
   mvn clean package
   ```
   처음 빌드할 때 JitPack이 해당 태그를 실제로 컴파일해서 내려주기 때문에 몇 분 걸릴 수 있습니다.
3. `target/ExampleAddon-1.0.0.jar` 을 서버의
   `plugins/RandomWeaponWar/addons/` 폴더에 넣고 서버를 시작하세요.
4. 콘솔에 아래 로그가 뜨면 성공:
   ```
    ExampleAddon (ExampleAddon) 로드 완료 — 무기 1개 등록됨
   ```
5. 게임 내 `/rww item` GUI에 "[ 예시 애드온 ] 번개의 창"이 다른 무기들과 함께 나타납니다.

## 커스터마이징

`config.yml` (RandomWeaponWar 본체 쪽)에 아래 항목을 추가하면 수치를 조정할 수 있습니다
(코드에 이미 기본값이 있어서 없어도 동작합니다):

```yaml
addons:
  lightningspear:
    attack-damage: 5.0
    attack-speed: -2.4
    skill-damage: 6.0
    range: 8.0
    cooldown: 15
```

`LightningSpearWeapon.java`를 복사해서 다른 무기를 추가로 만들고,
`ExampleAddonMain.createWeapons()`가 반환하는 리스트에 넣기만 하면 여러 개의
무기를 가진 애드온도 그대로 만들 수 있습니다.

더 자세한 애드온 개발 가이드는 RandomWeaponWar 본체의 `ADDON_GUIDE.md`를 참고하세요.
