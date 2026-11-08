# 0.28 validation

- All local suites passed: original archive/PCX/levels, combat, checkpoint, animations, lifts, doors, items, environment, hero frames, bosses and projectile interception.
- Energy tests cover six pickup mappings across four channels, initial wizard25 reserve, cap100, base shot cost1, empty-energy cast rejection, melee availability, checkpoint round trip/range rejection and legacy defaults through version9.
- APK built; v1/v2/v3 signatures verified; installed on Huawei $DEVICE.
- All78 device assertions passed: new assertions cover starting reserve, original item6 pickup, cap100, shot cost, empty cast rejection, all4 reserve checkpoint and campaign carry.
- Device output: analysis/huawei-028-device-tests.txt.
- Visual inspection: analysis/huawei-energy-028.png confirms readable MANA row alongside score/kills at1920x1080.
- Original wizard spell0 cost is implemented. Other spells, separate hero inventories, exact attack cadence/animations, active projectile checkpoint and full campaign fidelity remain incomplete.
- No original assets packaged.
