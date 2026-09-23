say GUIFIXTURE einsert
clear @s
setblock 0 -60 2 air
kill @e[type=minecraft:item,distance=..12]
setblock 0 -60 2 enchantment_custom_table:enchanting_custom_table
time set noon
gamerule doDaylightCycle false
gamerule doMobSpawning false
weather clear
tp @s 0.5 -60 0.5 0 30
item replace entity @s hotbar.0 with minecraft:diamond_sword{Enchantments:[{id:"minecraft:sharpness",lvl:1s}]}
item replace entity @s hotbar.1 with minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:sharpness",lvl:1s}]}
item replace entity @s hotbar.2 with minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:unbreaking",lvl:1s}]}
item replace entity @s hotbar.3 with minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:looting",lvl:1s}]}
item replace entity @s hotbar.4 with minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:unbreaking",lvl:1s}]}
item replace entity @s hotbar.5 with minecraft:book
item replace entity @s hotbar.6 with minecraft:dirt
