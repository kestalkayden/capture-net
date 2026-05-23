# Capture Net

A reusable capture-and-release net for Minecraft 26.1.x, available on both Fabric and NeoForge.

- **Capture**: right-click a passive or neutral mob with an empty net — it's stored inside the net.
- **Release**: right-click a block face with a filled net — the mob is released on top.
- **Full NBT preservation**: color, name, age, tame state, and any modded NBT extensions all survive the trip. Pink sheep, named pets, and modded mobs work out of the box.
- **Safety filter**: hostile mobs (anything in `MobCategory.MONSTER`) and bosses (Ender Dragon, Wither, Warden) cannot be captured.

## Recipe

```
S _ S
_ I _
S T S
```

`S` = string, `I` = iron ingot, `T` = stick.

## Tag for cross-mod interop

This mod publishes the item tag `#capturenet:nets` containing all capture-net items. Other mods that want to accept "any capture net" in their workflows should read this tag.

## License

CC0-1.0
