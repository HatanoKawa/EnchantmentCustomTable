say GUIFIXTURE esplit
clear @s
setblock 0 -60 2 air
kill @e[type=minecraft:item,distance=..12]
setblock 0 -60 2 enchantment_custom_table:enchanting_custom_table
time set noon
gamerule doDaylightCycle false
gamerule doMobSpawning false
weather clear
tp @s 0.5 -60 0.5 0 30
item replace entity @s hotbar.0 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:looting":3}}]
