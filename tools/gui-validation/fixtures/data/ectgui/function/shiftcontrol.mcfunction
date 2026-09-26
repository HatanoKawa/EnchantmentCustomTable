say GUIFIXTURE shiftcontrol
clear @s
setblock 0 -60 2 air
kill @e[type=minecraft:item,distance=..12]
setblock 0 -60 2 minecraft:chest
tp @s 0.5 -60 0.5 0 30
item replace entity @s hotbar.0 with minecraft:dirt 16
