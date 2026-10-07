# Changelog

All notable changes to this project are documented in this file.

The project follows [Semantic Versioning](https://semver.org/).

## [2.7.0] - 2026-10-07

### Added

- External language packs with canonical locale identifiers and runtime language reloads.
- Localization support for configurable fishing content, displays, menus, and legacy UI text.
- Optional EconomyShopGUI integration for selling loose fish and fish bag contents through `/sellgui`.
- Translation coverage auditing for bundled English and Spanish language files.

### Changed

- Target the Paper 1.21 API and document compatibility through Paper 1.21.11.
- Register optional plugin integrations through the plugin integration handler.
- Keep fish sale operations transaction-safe by removing fish only after successful economy deposits.

### Fixed

- Prevent WorldGuard checks from failing when a player context is required.
- Reset accumulated rarity weights during configuration reloads.
- Recover orphaned fishing rod identifiers and update catch statistics correctly.
- Preserve loose fish and fish bag contents when economy transactions fail.
