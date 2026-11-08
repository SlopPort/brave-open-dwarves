# 0.23 validation

- Full local suite passed: archive/PCX/maps, combat, checkpoint, ANM, lifts, doors, items, environment, hero frames, bosses and projectile interception.
- New boss checks: 19-tick windup threshold; three shots; actor holds position and faces hero; fixed velocity vectors even when facing left; original type9 and damage2; randomized delay within original range.
- Built APK, verified v1/v2/v3 signatures, installed on Huawei $DEVICE.
- All 53 device assertions passed, including original map40 actor/health, three missile creation, original damage/type, door unlock and defeated-boss checkpoint.
- Device test output: analysis/huawei-023-device-tests.txt.
- Remaining: special teleport phase, flame particles, sounds, exact patrol/gravity and attack-state checkpoint persistence. Original range depletion algorithm is not yet fully traced.
- Tests do not demonstrate a full manual campaign playthrough.

- Visual inspection on Huawei: analysis/huawei-fire-023.png confirms map40, original fire elemental sprite and health bar render correctly in overview. This screenshot does not verify live missile animation.
