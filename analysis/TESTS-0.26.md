# 0.26 validation and pause checkpoint

- Full local suites passed: archive/PCX/maps, combat, checkpoint, ANM, lifts, doors, items, environment, hero frames, bosses, projectile interception.
- New local checks: original map50 includes lich plus8 bats; health800 and92x105 geometry; cast animations; all3 seeded spell patterns and damage; icicle viewport placement/speed; lich teleport floor coordinates.
- Built APK and verified v1/v2/v3 signatures; installed on Huawei $DEVICE.
- All67 device assertions passed. New checks cover map50 actor/health, each of3 spell patterns, teleport position/state checkpoint, final arena exit unlock, and defeated-lich checkpoint.
- Device output: analysis/huawei-026-device-tests.txt.
- Spell device fixture temporarily clears collision cells to isolate pattern generation, then reloads original map before teleport/exit/save tests. This does not prove a full manual boss fight or campaign playthrough.
- Remaining fidelity gaps: exact movement, knockback/hit-triggered teleports, death/respawn phases, final-exit conditions, particle/sound effects, weapons/items/mana, projectile range depletion and active projectile checkpoint persistence.
- Work must pause at0.26 as explicitly requested by user. Resume only on user request; do not automatically proceed to0.27.
- APK/source ZIP contain no original game assets.

- Visual inspection: analysis/huawei-lich-026.png confirms original map50, lich sprite/health bar and eight bats render in overview on Huawei. The screenshot does not prove live spell animation.
