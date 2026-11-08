# 0.24 validation

- Full local reader, combat, checkpoint, ANM, lift, door, item, environment, hero, boss and projectile suites passed.
- New teleport tests: unique eligible destination uses exact original center/feet coordinates; delay ranges; pending teleport state; cast suspends teleport countdown; no-candidate map terminates safely.
- APK built and v1/v2/v3 signatures verified; installed on Huawei $DEVICE.
- All56 device checks passed. New checks verify eligible floor and empty cell above, quick follow-up cast delay, and teleported position round trip.
- Device output: analysis/huawei-024-device-tests.txt.
- No full manual campaign playthrough claimed. Particle/sound effects, exact movement and saving pending/timer state remain incomplete.
- Original assets are not packaged in APK or source ZIP.
