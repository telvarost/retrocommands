package com.matthewperiut.spc.command;

import com.matthewperiut.spc.api.Command;
import com.matthewperiut.spc.api.ItemInstanceStr;
import com.matthewperiut.spc.util.SharedCommandSource;
import net.minecraft.entity.EntityBase;
import net.minecraft.entity.EntityRegistry;
import net.minecraft.entity.Living;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;



public class Give implements Command {

    public static boolean givePlayerItemInstance(SharedCommandSource commandSource, PlayerBase player, ItemInstance instance) {
        ItemInstance[] inventory = player.inventory.main;
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = instance;
                return true;
            }
        }

        commandSource.sendFeedback("Cannot give " + instance.getType().getTranslatedName() + " because inventory is full");
        return false;
    }

    public static int nameToItemId(String n) {
        String name = n.replace("_", "");
        for (int i = 0; i < ItemBase.byId.length; i++) {
            if (ItemBase.byId[i] == null)
                continue;
            String translatedName = ItemBase.byId[i].getTranslatedName().replace(" ", ""); // Remove spaces
            if (translatedName.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void command(SharedCommandSource commandSource, String[] parameters) {
        if (parameters.length > 1 && !parameters[1].equals("help")) {
            int itemNumber;
            int meta = 0;

            String setSPCStr = "";

            try {
                itemNumber = Integer.parseInt(parameters[1]);
            } catch (NumberFormatException e) {
                itemNumber = nameToItemId(parameters[1]);
            }

            if (itemNumber == -1) {
                commandSource.sendFeedback("Item not found");
                return;
            }

            ItemBase itemType = ItemBase.byId[itemNumber];
            if (itemType == null) {
                commandSource.sendFeedback("Item not found");
                return;
            }
            int stackSize = itemType.getMaxStackSize();


            if (parameters.length > 2) {
                int requestedCount = 0;
                try {
                    requestedCount = Integer.parseInt(parameters[2]);
                } catch (NumberFormatException e) {
                    commandSource.sendFeedback("Requested number of items '" + parameters[2] + "' is not a number");
                    return;
                }
                stackSize = requestedCount;

                if (parameters.length > 3) {
                    if (itemType.usesMeta() || itemType.getTranslatedName().equals("Monster Spawner")) {
                        try {
                            meta = Integer.parseInt(parameters[3]);
                        } catch (NumberFormatException e) {
                            if (itemType.getTranslatedName().equals("Monster Spawner")) {
                                EntityBase entity = EntityRegistry.create(parameters[3], commandSource.getPlayer().level);
                                if (entity instanceof Living l) {
                                    setSPCStr = l.getStringId();
                                    commandSource.sendFeedback(l.getStringId() + " inside");
                                } else if (entity != null) {
                                    commandSource.sendFeedback("Entity must be living in spawners");
                                } else {
                                    commandSource.sendFeedback("Invalid entity parameter");
                                }
                            } else {
                                commandSource.sendFeedback("Metadata not a number");
                            }
                        }
                    } else {
                        commandSource.sendFeedback("Metadata not applicable");
                    }
                }
            }

            ItemInstance item = new ItemInstance(itemType, stackSize, meta);

            if (!setSPCStr.isEmpty()) {
                ((ItemInstanceStr) ((Object) item)).spc$setStr(setSPCStr);
            }

            if (givePlayerItemInstance(commandSource, commandSource.getPlayer(), item)) {
                commandSource.sendFeedback("Gave " + item.count + " " + itemType.getTranslatedName());
            }
            return;
        }
        manual(commandSource);
    }

    @Override
    public String name() {
        return "give";
    }

    @Override
    public void manual(SharedCommandSource commandSource) {
        commandSource.sendFeedback("Usage: /give {id} {count} {optional:meta/mob_id}");
        commandSource.sendFeedback("Info: gives the player items");
        commandSource.sendFeedback("id: can be number or name");
        commandSource.sendFeedback("count: number of item");
        commandSource.sendFeedback("meta: sub-item selection");
        commandSource.sendFeedback("list mobs with /mobs");
    }
}
