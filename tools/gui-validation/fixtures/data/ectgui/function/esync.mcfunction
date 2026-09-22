say GUIFIXTURE esync
clear @s
setblock 0 -60 2 air
kill @e[type=minecraft:item,distance=..12]
setblock 0 -60 2 enchantment_custom_table:enchanting_custom_table
time set noon
gamerule doDaylightCycle false
gamerule doMobSpawning false
weather clear
tp @s 0.5 -60 0.5 0 30
item replace entity @s hotbar.0 with minecraft:diamond_sword[minecraft:enchantments={levels:{"minecraft:sharpness":3,"minecraft:looting":3,"minecraft:unbreaking":3,"minecraft:mending":1}}]
