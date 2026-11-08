# 0.30 validation

- All local suites passed. New energy checks: full128-tick delay, wizard/warrior channel selection, multiple pulses, automatic cap25, preserved collected excess, no dead-hero regeneration. Checkpoint tests cover saved phase, nonfinite/out-of-range rejection and version10 default.
- Built APK, verified v1/v2/v3 signatures, installed on Huawei $DEVICE.
- All87 device assertions passed. New assertions cover128-tick boundary for each hero, cap25, saved phase and pause freeze.
- Initial device fixture collected an item at spawn and changed another energy reserve; isolated regeneration fixture marks items taken, then passes. User checkpoint files restored by runner.
- Device output: analysis/huawei-030-device-tests.txt.
- No original assets packaged. Original ability unlock progression remains unresolved; other spells are not yet playable. Exact global tick/initialization phase, full movement, boss phases and projectile checkpoint remain incomplete.
