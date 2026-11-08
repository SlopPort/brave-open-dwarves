# 0.29 validation

- Full local suites passed. New checks cover original chapter energy reset on1/11/21/31/41/1001 for wizard and warrior, and preservation on ordinary maps.
- Built APK; verified v1/v2/v3 signatures; installed on Huawei $DEVICE.
- All82 device assertions passed: wizard10->11 reset, retained score/health, restored reset reserve, warrior20->21 reset to second channel. Existing ordinary-map carry assertion also passed.
- Output: analysis/huawei-029-device-tests.txt.
- Corrects0.28 blanket energy carry using evidence412ca2..412d24. Ability levels and selected spell reset are not implemented because these features are still missing; existing character toggle remains provisional.
- Spell unlock research remains incomplete; no invented unlock pickups added.
- APK/source ZIP contain no original game assets.
