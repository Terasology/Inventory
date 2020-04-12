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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.utilities.Assets;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.entity.BlockCommands;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
            @CommandParam("objectID") String inventoryObjectId,
            @CommandParam(value = "quantity", required = false) Integer inputQuantity,
            @CommandParam(value = "blockShapeName", required = false) String shapeUriParam) {

        int removalQuantity = inputQuantity != null ? inputQuantity : 1;
        if (removalQuantity < 1) {
            return "Invalid quantity of objects to remove!";
        }

        String message = null;
        Set<ResourceUrn> matches = assetManager.resolve(inventoryObjectId, Prefab.class);
        if (matches.size() == 1) {
            Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
            message = removeItem(prefab, removalQuantity, client);
        } else if (matches.size() > 1) {
            message = buildAmbiguousObjectIdString("item", inventoryObjectId, matches);
        }

        if (message == null) {
            // If no matches are found for items, try blocks
            message = removeBlock(client, inventoryObjectId, removalQuantity, shapeUriParam);
        }

        if (message != null) {
            return message;
        } else {
            return "Could not find an item or block matching \"" + inventoryObjectId + "\"";
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

    private String buildAmbiguousObjectIdString(String objectType, String objectId, Set<ResourceUrn> possibleMatches) {
        StringBuilder builder = new StringBuilder();
        builder.append("Specified ");
        builder.append(objectType);
        builder.append(" id \"");
        builder.append(objectId);
        builder.append("\" is not unique. Possible matches: ");
        Joiner.on(", ").appendTo(builder, possibleMatches);

        return builder.toString();
    }

    private String removeItem(Prefab prefab, int removalQuantity, EntityRef client) {

        if (prefab != null && prefab.hasComponent(ItemComponent.class)) {
            boolean isStackable = !prefab.getComponent(ItemComponent.class).stackId.isEmpty();

            EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
            List<EntityRef> inventorySlots = Lists.reverse(playerEntity.getComponent(InventoryComponent.class).itemSlots);
            int quantityLeft = removalQuantity;
            int removedItems = 0;

            for (EntityRef slot : inventorySlots) {
                Prefab slotPrefab = slot.getParentPrefab();

                if (slotPrefab != null && slotPrefab.equals(prefab)) {

                    EntityRef result = null;
                    if (isStackable) {
                        ItemComponent itemComponent = slot.getComponent(ItemComponent.class);
                        if (itemComponent != null) {
                            if (quantityLeft >= itemComponent.stackCount) {
                                result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, itemComponent.stackCount);
                                removedItems = removedItems + itemComponent.stackCount;
                                quantityLeft = quantityLeft - itemComponent.stackCount;
                            } else {
                                result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, quantityLeft);
                                removedItems = removedItems + quantityLeft;
                                quantityLeft = quantityLeft - quantityLeft;
                            }
                        }
                    } else {
                        result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, 1);
                        result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, 1);
                        removedItems = removedItems + 1;
                        quantityLeft = quantityLeft - 1;
                    }

                    if (result == null) {
                        logger.debug("Could not remove  \""
                                + prefab.getName()
                                + "\" from slot " + slot.getId());
                    } else if (result == EntityRef.NULL) {
                        if (quantityLeft == 0) {
                            break;
                        }
                    }
                }
            }

            if (removedItems > 0) {
                return "You removed "
                        + (removedItems > 1 ? removedItems + " items of " : "an item of ")
                        + prefab.getName();
            } else {    // can also mean that all removal attempts failed
                return "Nothing to remove, you don't have \""
                        + prefab.getName()
                        + "\" in your inventory";
            }
        }

        return null;
    }


    public String removeBlock(
            @Sender EntityRef sender,
            @CommandParam("blockName") String uri,
            @CommandParam(value = "quantity", required = false) Integer quantityParam,
            @CommandParam(value = "shapeName", required = false) String shapeUriParam) {
        Set<ResourceUrn> matchingUris = Assets.resolveAssetUri(uri, BlockFamilyDefinition.class);

        BlockFamily blockFamily = null;

        if (matchingUris.size() == 1) {
            Optional<BlockFamilyDefinition> def = Assets.get(matchingUris.iterator().next(), BlockFamilyDefinition.class);
            if (def.isPresent()) {
                if (def.get().isFreeform()) {
                    if (shapeUriParam == null) {
                        blockFamily = blockManager.getBlockFamily(new BlockUri(def.get().getUrn(), new ResourceUrn("engine:cube")));
                    } else {
                        Set<ResourceUrn> resolvedShapeUris = Assets.resolveAssetUri(shapeUriParam, BlockShape.class);
                        if (resolvedShapeUris.isEmpty()) {
                            return "Found block. No shape found for '" + shapeUriParam + "'";
                        } else if (resolvedShapeUris.size() > 1) {
                            return buildAmbiguousObjectIdString("block", shapeUriParam, resolvedShapeUris);
                        }
                        blockFamily = blockManager.getBlockFamily(new BlockUri(def.get().getUrn(), resolvedShapeUris.iterator().next()));
                    }
                } else {
                    blockFamily = blockManager.getBlockFamily(new BlockUri(def.get().getUrn()));
                }
            }

            if (blockFamily == null) {
                //Should never be reached
                return "Block not found";
            }

            int quantity = quantityParam != null ? quantityParam : 1;

            return removeBlock(blockFamily, quantity, sender);

        } else if (matchingUris.size() > 1) {
            return buildAmbiguousObjectIdString("block", uri, matchingUris);
        }

        return null;
    }

    private String removeBlock(BlockFamily blockFamily, int removalQuantity, EntityRef client) {
        boolean isStackable = blockFamily.getArchetypeBlock().isStackable();

        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        List<EntityRef> inventorySlots = Lists.reverse(playerEntity.getComponent(InventoryComponent.class).itemSlots);
        int quantityLeft = removalQuantity;
        int removedItems = 0;

        for (EntityRef slot : inventorySlots) {
            if (slot.hasComponent(BlockItemComponent.class)) {
                BlockFamily slotBlockFamily = slot.getComponent(BlockItemComponent.class).blockFamily;

                if (slotBlockFamily != null && slotBlockFamily.equals(blockFamily)) {

                    EntityRef result = null;
                    if (isStackable) {
                        ItemComponent itemComponent = slot.getComponent(ItemComponent.class);
                        if (itemComponent != null) {
                            if (quantityLeft >= itemComponent.stackCount) {
                                result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, itemComponent.stackCount);
                                removedItems = removedItems + itemComponent.stackCount;
                                quantityLeft = quantityLeft - itemComponent.stackCount;
                            } else {
                                result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, quantityLeft);
                                removedItems = removedItems + quantityLeft;
                                quantityLeft = quantityLeft - quantityLeft;
                            }
                        }
                    } else {
                        result = inventoryManager.removeItem(playerEntity, EntityRef.NULL, slot, true, 1);
                        removedItems = removedItems + 1;
                        quantityLeft = quantityLeft - 1;
                    }

                    if (result == null) {
                        logger.debug("Could not remove \""
                                + blockFamily.getDisplayName()
                                + "\" from slot " + slot.getId());
                    } else if (result == EntityRef.NULL) {
                        if (quantityLeft == 0) {
                            break;
                        }
                    }
                }
            }
        }

        if (removedItems > 0) {
            return "You removed "
                    + (removedItems > 1 ? removedItems + " blocks of " : "a block of ")
                    + blockFamily.getDisplayName();
        } else {    // can also mean that all removal attempts failed
            return "Nothing to remove, you don't have \""
                    + blockFamily.getDisplayName()
                    + "\" in your inventory";
        }
    }
}
