# 0.25 validation

- Local suites passed: archive/PCX/levels, combat, checkpoint, animation, lifts, doors, items, environment, hero, bosses, projectile interception.
- Checkpoint tests now cover enemy motion/attack round trip, rejection of NaN/out-of-range timers and teleport state on incompatible enemy type; versions1..8 remain readable.
- Built APK, verified v1/v2/v3 signatures, installed on Huawei $DEVICE.
- All60 device assertions passed. New assertions cover saved attack and teleport countdowns, vertical velocity/turn delay, actual restored cast firing across threshold, and pending teleport preservation.
- Device output: analysis/huawei-025-device-tests.txt.
- Active projectiles and hero motion/attack state are still not checkpointed. Full original behavior and full campaign playthrough remain unverified.
- No original assets packaged.
