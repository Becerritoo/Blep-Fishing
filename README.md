<a id="readme-top"></a>

<!-- PROJECT LOGO -->
<div align="center">

  ![Banner][logo]
  
  [logo]: https://github.com/Nibrock/nibrock/blob/main/assets/blepassets/header.png "Logo Title Text 2"  
  <p align="center">
    The Most Expansive Fishing Plugin for Minecraft
    <br />
    <br />
    <a href="https://github.com/Kunfury-blep/Blep-Fishing/wiki"><strong>Official Wiki</strong></a>
    <br />
    <br />
    <a href="https://www.spigotmc.org/resources/blep-fishing.78555/">Spigot</a>
    &middot;
    <a href="https://polymart.org/resource/blep-fishing.28">PolyMart</a>
    &middot;
    <a href="https://hangar.papermc.io/Kunfury/BlepFishing">Paper</a>
    <br />
    <br />
    <a href="https://bstats.org/plugin/bukkit/Blep%20Fishing/18201"><strong>Analytics</strong></a>
  </p>
</div>

<!-- ABOUT THE PROJECT -->
## About
Works to completely overhaul the default fishing in minecraft by adding multiplayer Fishing Tournaments, a Treasure System, Fishing Gear and Equipment, and Statistics and Analytics of your Server

## JL Translation Layer (2.6-JL.1)
- Compatible with Paper `1.21.4+` (`api-version: 1.21`).
- Global language is configured in `plugins/BlepFishing/config.yml`:
  - `Language: English`
- On first boot, language files are exported to:
  - `plugins/BlepFishing/lang/*.yml`
- The plugin now seeds external translatable keys for:
  - fish names/lore/description
  - casket names
  - tournament names
  - rarity names
  - area names/hints
  - player panel GUI title/button names/lore
- Hot reload language/config text with:
  - `/bf reloadlang`
  - aliases: `/bf langreload`, `/bf reloadlanguage`, `/bf rlang`

### GUI language key syntax
You can reference language keys directly in `gui.yml` values with:
- `lang:Some.Path.Key`
- `@lang:Some.Path.Key`
- `%lang:Some.Path.Key%`
