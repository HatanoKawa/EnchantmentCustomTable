say GUIFIXTURE estrict
clear @s
setblock 0 -60 2 air
kill @e[type=minecraft:item,distance=..12]
setblock 0 -60 2 enchantment_custom_table:enchanting_custom_table
time set noon
gamerule doDaylightCycle false
gamerule doMobSpawning false
weather clear
tp @s 0.5 -60 0.5 0 30
item replace entity @s hotbar.0 with minecraft:diamond_sword[minecraft:enchantments={levels:{"minecraft:sharpness":2}}]
item replace entity @s hotbar.1 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:sharpness":2}}]
item replace entity @s hotbar.2 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:sharpness":1}}]
item replace entity @s hotbar.3 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:looting":4}}]
item replace entity @s hotbar.4 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:sharpness":3}}]
item replace entity @s hotbar.5 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:sharpness":5}}]
item replace entity @s hotbar.6 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:sharpness":1,"minecraft:unbreaking":1}}]
item replace entity @s hotbar.7 with minecraft:enchanted_book[minecraft:stored_enchantments={levels:{"minecraft:sharpness":4}}]
