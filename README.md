# Inventory Bogosorter

This is aims to replace the popular Inventory Tweaks mod.

### Why?

Inventory Tweaks API is very limited. It doesn't work well with modular gui libraries like ModularUI. It also has desync bugs, since it is client side only.

### Why rewrite and not just fork?

The Inventory Tweaks code is very unpleasant to work with. I rather write my own clean mod.

---

## Features

- sorting of player inventories in (almost) all modded GUI's (default key is middle mouse button)
- sorting of many modded inventories
- sort buttons for each sortable inventory
- configuring of sort rules (open config with K by default)
- automatically switching out tools wich are about to break
- automatically refill broken tools or used up items
- scroll through vertical slots above a hotbar slots while holding ALT
- several key shortcuts to move items:
  - CTRL + LMB: transfers a single item
  - CTRL + RMB: transfers a single item into an empty slot
  - Space + LMB: transfer the whole inventory
  - ALT + LMB: transfers all items of the same type
  - Space + Q: throws the whole inventory into the world
  - ALT + Q: throws all the items of the same type into the world

## TODO's

- sorting profiles
  - bind certain profiles to a certain block? (might be difficult)
  - radial menu to quickly choose profile
  - choose profile for ae2 and jei
- configurable sort sound
- animation?


### Credits
Dropoff Feature - https://www.curseforge.com/minecraft/mc-mods/dropoff - https://github.com/SCP002/DropOff/tree/1.7.10
Usage Ticker - Quark - https://github.com/VazkiiMods/Quark/blob/1.14/src/main/java/vazkii/quark/client/module/UsageTickerModule.java
