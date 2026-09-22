say GUIFIXTURE cboundary
clear @s
setblock 0 -60 2 air
kill @e[type=minecraft:item,distance=..12]
setblock 0 -60 2 enchantment_custom_table:enchantment_conversion_table
time set noon
gamerule doDaylightCycle false
gamerule doMobSpawning false
weather clear
tp @s 0.5 -60 0.5 0 30
item replace entity @s hotbar.0 with minecraft:book 2
item replace entity @s hotbar.1 with minecraft:emerald 35
item replace entity @s hotbar.2 with minecraft:emerald
item replace entity @s hotbar.3 with minecraft:dirt
