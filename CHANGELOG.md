# Changelog

## [2.7.1] - 2026-11-04

### Fixed

- Keep fish bag lore synchronized after the bag item moves or is recreated.
- Prevent bulk inventory deposits from counting newly added fish twice when checking bag capacity.

## [2.7.0] - 2026-10-11

### Added

- Optional EconomyShopGUI integration for selling loose fish through its sell GUI.
- Support for selling fish bag contents through EconomyShopGUI while returning the empty bag.

### Changed

- Target the Paper 1.21 API.
- Register optional integrations through the plugin integration handler.
- Remove fish from inventories and bags only after Vault confirms payment.

### Fixed

- Prevent WorldGuard fishing checks from failing when a player context is required.
- Avoid packaging server-provided WorldEdit, WorldGuard, and mcMMO classes in the plugin JAR.
- Reset accumulated rarity weights when configurations are reloaded.
- Recover orphaned fishing rod identifiers and update per-rod catch statistics correctly.
- Preserve fish and bag contents when economy transactions fail.
