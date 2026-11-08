# 0.31 validation

- Full local suites passed. New checks cover locked-spell skipping, original power-orb level mapping, fireball/lightning projectile kinds/damage/cost/direction, chapter ability reset, checkpoint round trip and invalid selection rejection; versions1..11 defaults remain readable.
- Built APK, verified v1/v2/v3 signatures and installed on Huawei $DEVICE.
- All94 device assertions passed: actual map4 orb56 collection; fireball/lightning costs and kinds; saved abilities/selection; normal-map carry; chapter reset; physical touch of CZAR cycles unlocked spell.
- Device output: analysis/huawei-031-device-tests.txt. Test runner preserves user's checkpoint files.
- Visual inspection: analysis/huawei-spell-ui-031.png confirms readable CZAR2 and MANA footer, selected through touch. Screenshot obtained from instrumentation external files directory; app remains non-debuggable with no added permissions. It does not verify a full fight or all projectile frames in motion.
- Unsupported: fourth basic spell and advanced tiers, separate warrior casting system/inventory, original spawn/cadence details, active projectile checkpoint and full movement/boss fidelity.
- Power-orb byte progression now matches actual4163ae arithmetic. Earlier consumable interpretation was incorrect and is explicitly corrected in FORMAT.md.
- No original game assets packaged in APK/source ZIP.
