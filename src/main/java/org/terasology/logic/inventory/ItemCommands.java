/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.inventory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.entity.BlockCommands;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RegisterSystem
// TODO: Check whether this is really needed. Was introduced by darshan3 to share item commands through core registry
@Share(ItemCommands.class)
public class ItemCommands extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(ItemCommands.class);

    @In
    private BlockCommands blockCommands;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private PrefabManager prefabManager;

    @In
    private BlockManager blockManager;

    @Command(shortDescription = "Adds an item or block to your inventory",
            helpText = "Puts the desired number of the given item or block with the given shape into your inventory",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String give(
            @Sender EntityRef client,
            @CommandParam("prefabId or blockName") String itemPrefabName,
            @CommandParam(value = "amount", required = false) Integer amount,
            @CommandParam(value = "blockShapeName", required = false) String shapeUriParam) {

        int itemAmount = amount != null ? amount : 1;
        if (itemAmount < 1) {
            return "Requested zero (0) items / blocks!";
        }

        Set<ResourceUrn> matches = assetManager.resolve(itemPrefabName, Prefab.class);

        if (matches.size() == 1) {
            Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef playerEntity = client.getComponent(ClientComponent.class).character;

                for (int quantityLeft = itemAmount; quantityLeft > 0; quantityLeft--) {
                    EntityRef item = entityManager.create(prefab);
                    if (!inventoryManager.giveItem(playerEntity, playerEntity, item)) {
                        item.destroy();
                        itemAmount -= quantityLeft;
                        break;
                    }
                }

                return "You received "
                        + (itemAmount > 1 ? itemAmount + " items of " : "an item of ")
                        + prefab.getName() //TODO Use item display name
                        + (shapeUriParam != null ? " (Item can not have a shape)" : "");
            }

        } else if (matches.size() > 1) {
            return buildAmbiguousObjectIdString("item", itemPrefabName, matches);
        }

        // If no matches are found for items, try blocks
        String message = blockCommands.giveBlock(client, itemPrefabName, amount, shapeUriParam);
        if (message != null) {
            return message;
        }

        return "Could not find an item or block matching \"" + itemPrefabName + "\"";
    }


    @Command(shortDescription = "Removes an item from your inventory",
            helpText = "Removes the desired number of the given item from your inventory",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String remove(
            @Sender EntityRef client,
            @CommandParam("objectUri") String inventoryObjectUri,
            @CommandParam(value = "quantity", required = false) Integer inputQuantity,
            @CommandParam(value = "objectShapeUri", required = false) String shapeUri) {

        final int removalQuantity = inputQuantity != null ? inputQuantity : 1;
        if (removalQuantity < 1) {
            return "Invalid quantity of objects to remove!";
        }

        // assume the object to remove is a block
        Set<ResourceUrn> matchingUrns = assetManager.resolve(inventoryObjectUri, BlockFamilyDefinition.class);
        final Set<ResourceUrn> matchingShapeUrns = (shapeUri == null ? null : assetManager.resolve(shapeUri, BlockShape.class));
        final boolean isBlock = !matchingUrns.isEmpty();
        if (!isBlock) {
            // assume the object to remove is an item
            matchingUrns = assetManager.resolve(inventoryObjectUri, Prefab.class);
        } else {
            // for blocks check shapeUri if applicable
            if (shapeUri != null) {
                if (matchingShapeUrns.isEmpty()) {
                    return "Could not find a shape matching \"" + shapeUri + "\"";
                } else if (matchingShapeUrns.size() > 1) {
                    return buildAmbiguousObjectIdString("shape", shapeUri, matchingShapeUrns);
                }
            }
        }

        // found multiple matches -> cannot remove due to ambiguity
        if (matchingUrns.size() > 1) {
            return buildAmbiguousObjectIdString((isBlock ? "block" : "item"), inventoryObjectUri, matchingUrns);
        }

        // found exactly one match -> try to remove
        int removedItems = 0;
        String displayName = "";
        if (matchingUrns.size() == 1) {
            if (isBlock) {
                BlockFamily blockFamily = assetManager.getAsset(matchingUrns.iterator().next(), BlockFamilyDefinition.class).map(def -> {
                    if (def.isFreeform()) {
                        ResourceUrn shapeUrn = (matchingShapeUrns == null ? new ResourceUrn("engine:cube") : matchingShapeUrns.iterator().next());
                        return blockManager.getBlockFamily(new BlockUri(def.getUrn(), shapeUrn));
                    } else {
                        return blockManager.getBlockFamily(new BlockUri(def.getUrn()));
                    }
                }).orElse(null);

                if (blockFamily != null) {
                    displayName = blockFamily.getDisplayName();
                    removedItems = removeBlock(blockFamily, displayName, removalQuantity, client);
                }

            } else {
                Prefab prefab = assetManager.getAsset(matchingUrns.iterator().next(), Prefab.class).orElse(null);
                if (prefab != null && prefab.hasComponent(ItemComponent.class)) {
                    displayName = prefab.getName();
                    removedItems = removeItem(prefab, displayName, removalQuantity, client);
                }
            }
        }


        if (removedItems > 0) {
            return "You removed "
                    + (removedItems > 1
                        ? removedItems + " " + (isBlock ? "blocks" : "items") + " of "
                        : (isBlock ? "a block" : "an item") + " of ")
                    + displayName;
        } else {    // can also mean that all removal attempts failed
            return "Could not find an item or block matching \""
                    + inventoryObjectUri
                    + "\" in your inventory";
        }
    }

    @Command(shortDescription = "Lists all available items (prefabs)\nYou can filter by adding the beginning of words " +
            "after the commands, e.g.: \"listItems engine: core:\" will list all items from the engine and core module",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String listItems(@CommandParam(value = "startsWith", required = false) String[] startsWith) {

        List<String> stringItems = Lists.newArrayList();

        for (Prefab prefab : prefabManager.listPrefabs()) {
            if (!BlockCommands.uriStartsWithAnyString(prefab.getName(), startsWith)) {
                continue;
            }
            stringItems.add(prefab.getName());
        }

        Collections.sort(stringItems);

        StringBuilder items = new StringBuilder();
        for (String item : stringItems) {
            if (!items.toString().isEmpty()) {
                items.append(Console.NEW_LINE);
            }
            items.append(item);
        }

        return items.toString();
    }

    @Command(shortDescription = "Gives multiple stacks of items matching a search",
            helpText = "Adds all items that match the search parameter into your inventory",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String bulkGiveItem(
            @Sender EntityRef sender,
            @CommandParam("searched") String searched,
            @CommandParam(value = "quantity", required = false) Integer quantityParam) {

        if (quantityParam != null && quantityParam < 1) {
            return "Here, have these zero (0) items just like you wanted";
        }

        List<String> items = Lists.newArrayList();
        for (String item : listItems(null).split("\n")) {
            if (item.contains(searched.toLowerCase())) {
                items.add(item);
            }
        }

        String result = "Found " + items.size() + " item matches when searching for '" + searched + "'.";
        if (items.size() > 0) {
            result += "\nItems:";
            for (String item : items) {
                result += "\n" + item + "\n";
                give(sender, item, quantityParam, null);
            }
        }
        return result;
    }

    private String buildAmbiguousObjectIdString(String objectType, String objectUri, Set<ResourceUrn> possibleMatches) {
        StringBuilder builder = new StringBuilder();
        builder.append("Specified ");
        builder.append(objectType);
        builder.append(" uri \"");
        builder.append(objectUri);
        builder.append("\" is not unique. Possible matches: ");
        Joiner.on(", ").appendTo(builder, possibleMatches);

        return builder.toString();
    }

    private int removeObjectFromSlot(EntityRef slot, EntityRef playerEntity, boolean isStackable, String displayName, int quantity) {
        int quantityLeft = quantity;
        EntityRef result = null;
        if (isStackable) {
            ItemComponent itemComponent = slot.getComponent(ItemComponent.class);
            if (itemComponent != null) {
                if (quantityLeft >= itemComponent.stackCount) {
                    result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, itemComponent.stackCount);
                    quantityLeft = quantityLeft - itemComponent.stackCount;
                } else {
                    result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, quantityLeft);
                    quantityLeft = quantityLeft - quantityLeft;
                }
            }
        } else {
            result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, 1);
            quantityLeft = quantityLeft - 1;
        }

        if (result == null) {
            logger.debug("Could not remove  \""
                    + displayName
                    + "\" from slot " + slot.getId());
        }

        return quantityLeft;
    }

    private int removeItem(Prefab prefab, String displayName, final int removalQuantity, EntityRef client) {
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        List<EntityRef> inventorySlots = Lists.reverse(playerEntity.getComponent(InventoryComponent.class).itemSlots);
        int quantityLeft = removalQuantity;

        for (EntityRef slot : inventorySlots) {
            Prefab slotPrefab = slot.getParentPrefab();

            if (slotPrefab != null && slotPrefab.equals(prefab)) {
                quantityLeft = removeObjectFromSlot(slot,
                        playerEntity,
                        InventoryUtils.isStackable(prefab),
                        displayName,
                        quantityLeft);

                if (quantityLeft == 0) {
                    break;
                }
            }
        }

        return removalQuantity - quantityLeft;
    }

    private int removeBlock(BlockFamily blockFamily, String displayName, final int removalQuantity, EntityRef client) {
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        List<EntityRef> inventorySlots = Lists.reverse(playerEntity.getComponent(InventoryComponent.class).itemSlots);
        int quantityLeft = removalQuantity;

        for (EntityRef slot : inventorySlots) {
            if (slot.hasComponent(BlockItemComponent.class)) {
                BlockFamily slotBlockFamily = slot.getComponent(BlockItemComponent.class).blockFamily;

                if (slotBlockFamily != null && slotBlockFamily.equals(blockFamily)) {
                    quantityLeft = removeObjectFromSlot(slot,
                            playerEntity,
                            blockFamily.getArchetypeBlock().isStackable(),
                            displayName,
                            quantityLeft);

                    if (quantityLeft == 0) {
                        break;
                    }
                }
            }
        }

        return removalQuantity - quantityLeft;
    }
}
