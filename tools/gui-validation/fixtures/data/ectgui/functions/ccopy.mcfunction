say GUIFIXTURE ccopy
clear @s
setblock 0 -60 2 air
kill @e[type=minecraft:item,distance=..12]
setblock 0 -60 2 enchantment_custom_table:enchantment_conversion_table
time set noon
gamerule doDaylightCycle false
gamerule doMobSpawning false
weather clear
tp @s 0.5 -60 0.5 0 30
item replace entity @s hotbar.0 with minecraft:book 3
item replace entity @s hotbar.1 with minecraft:emerald_block 12
item replace entity @s hotbar.2 with minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:sharpness",lvl:5s}]}
item replace entity @s hotbar.3 with minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:sharpness",lvl:3s},{id:"minecraft:unbreaking",lvl:3s}]}
item replace entity @s hotbar.4 with minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:sharpness",lvl:6s}]}
item replace entity @s hotbar.5 with minecraft:diamond_sword
