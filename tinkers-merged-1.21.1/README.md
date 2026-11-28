# Tinkers Merged — Minecraft 1.21.1 (NeoForge)

One mod jar for **Minecraft 1.21.1 / NeoForge 21.1.x** that contains:

| Mod id | What | Source of the code |
|---|---|---|
| `modernfoundry` | Tinkers' Construct | [Unofficial 1.21.1 port](https://github.com/kipawepro/TinkersConstruct-port-1.21.1) (MIT), source code |
| `hilt` | Mantle (library), renamed by the port | Bundled inside the jar (jar-in-jar) |
| `tinker_rapier` | Tinkers' Rapier 1.0.1 | Ported from the 1.20.1 Forge jar |
| `tinkers_reforged` | Tinkers Reforged 2.20.0.7 | Ported from the 1.20.1 Forge jar |
| `tinkers_thinking` | Tinkers' Thinking 0.1.6.6.3 | Ported from the 1.20.1 Forge jar |
| `tinkerslevellingaddon` | Tinkers' Levelling Addon 1.5.0 | Ported from the 1.20.1 Forge jar |

**Ready-built jar:** [`dist/TinkersMerged-1.21.1-1.6.2-NeoForge.jar`](dist/TinkersMerged-1.21.1-1.6.2-NeoForge.jar).
Drop it into the `mods` folder of a NeoForge 21.1.x instance. Do **not** also install
Tinkers' Construct, Mantle or any of the four addons separately.

> This is not an official release. Official Tinkers' Construct has no 1.21.1 version, so the
> base is the community port above, which renames Tinkers' Construct to `modernfoundry` and
> Mantle to `hilt`. Worlds and configs from 1.20.1 (`tconstruct:` items) will **not** carry over.

## Why each addon keeps its own mod id

All six are in one jar, but every addon still registers under its original mod id
(`neoforge.mods.toml` lists several `[[mods]]`). That keeps their configs, item ids, language
keys and datapack namespaces working, and lets other mods or packs refer to them as before.

## How the addons were ported

The addons were only available as compiled 1.20.1 Forge jars, so:

1. Decompiled with Vineflower 1.11.1.
2. SRG names (`m_12345_`) mapped to Mojang names using MCPConfig 1.20.1 + Mojang's official
   1.20.1 mappings (method overloads matched by descriptor).
3. Packages remapped: `slimeknights.tconstruct` → `modernmods.modernfoundry`,
   `slimeknights.mantle` → `modernmods.hilt`, `net.minecraftforge` → `net.neoforged`.
4. Hand-ported to the 1.21.1 API, mainly:
   - effects and attributes are `Holder`s; attribute modifiers use `ResourceLocation` ids
   - item NBT → data components (via the port's `TagUtil` helpers)
   - capabilities registered through `RegisterCapabilitiesEvent`
   - networking rewritten with `CustomPacketPayload` / `RegisterPayloadHandlersEvent`
   - drying rack recipe rewritten with codecs (`SingleRecipeInput`); recipe type now registered
   - potion brewing via `RegisterBrewingRecipesEvent`
   - block `codec()`, the split `useItemOn` / `useWithoutItem`, entity data builder,
     `applyEffectTick` returning `boolean`, renamed events (`PlayerTickEvent.Post`,
     `LivingIncomingDamageEvent`, `LivingShieldBlockEvent`), `Bus.GAME`
   - mixin targets updated to 1.21.1 signatures
   - access transformers for the few protected fields the addons touch
5. Data and assets migrated to the 1.21 layout: folder renames (`recipes` → `recipe`,
   `tags/items` → `tags/item` …), namespaces (`tconstruct:` → `modernfoundry:`,
   `mantle:` → `hilt:`, `forge:` → `c:` / `neoforge:`), `neoforge:conditions`, recipe ids
   required by Hilt, `result.item` → `result.id`, flattened number providers, loot table
   and loot function changes, SNBT station-layout icons.

### Fix made to the port itself

The port posted `ModifierRegistrationEvent` and `ModifierModelRegistrationEvent` only on its own
mod event bus. NeoForge 1.21 gives every mod its own bus, so **no addon's code-defined modifiers
were ever registered**. Both events are now posted to every mod's bus
(`ModifierManager.fireRegistryEvent`, `ModifierModelManager`).

## What was left out or changed

- **Optional integrations removed** (those mods are not part of this merge): Tinkers' Thinking's
  support for Tinkers Ingenuity (the curio modifiers `curio_level`, `sculk_heal`),
  Modular Golems and Touhou Little Maid.
- **Data generators** of the addons were not ported; the already-generated JSON is shipped instead.
- **12 data files** where an addon overrode Tinkers' own data (e.g. Tinkers' Thinking's tweaks to the
  `gold` and `paper` materials) keep the **port's** version, because those files are known to load.
- Tinkers' Thinking's arrow "resisting" hit: arrow knockback now comes from 1.21 enchantment
  effects instead of the removed `getKnockback()`.
- Client-only mixins (crossbow arm pose / hand rendering) and the casting-table speed-up mixin are
  marked optional (`require = 0`): if 1.21's code no longer matches, they log a warning instead of
  crashing.

## Testing done

- `./gradlew build` succeeds.
- Dev server (`./gradlew runServer`) and a **clean production NeoForge 21.1.240 server** with only
  the built jar both start with **no errors** from these mods: 4226 recipes, 135 materials,
  361 dynamic + all static modifiers, 59 tool definitions, all loot modifiers decode.

**Not tested:** the game client (no display in the build environment) and actual gameplay —
textures/models in game, books, GUIs, rendering mixins, and whether each tool and modifier
behaves exactly as on 1.20.1. Expect some bugs; please test in a separate world first.

## Building

Needs Java 21.

```bash
./gradlew build -x test
# jar: build/libs/TinkersMerged-1.21.1-1.6.2-NeoForge.jar
./gradlew runServer   # dev server in ./run
./gradlew runClient   # dev client
```

## Licenses and credits

Everything here is MIT-licensed:
Tinkers' Construct and Mantle by SlimeKnights; the unofficial 1.21.1 port by its contributors
(see `PORT_README.md`); Tinkers' Rapier; Tinkers Reforged by mrthomas20121; Tinkers' Thinking by
Creeping Creeper; Tinkers' Levelling Addon by Pyre540. Report problems with this merge here, not
to the original authors.
