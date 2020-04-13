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
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RegisterSystem
// TODO: Check whether this is really needed. Was introduced by darshan3 to share item commands through core registry
@Share(InventoryCommands.class)
public class InventoryCommands extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(InventoryCommands.class);

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
            @CommandParam("objectUri") String inventoryObjectUri,
            @CommandParam(value = "quantity", required = false) Integer inputQuantity,
            @CommandParam(value = "objectShapeUri", required = false) String shapeUri) {

        final Optional<Integer> requestedQuantity = Optional.ofNullable(inputQuantity);
        if (requestedQuantity.map(q -> q < 1).orElse(false)) {
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

        int givenItems = 0;
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
                    boolean isStackable = blockFamily.getArchetypeBlock().isStackable();
                    int defaultQuantity = isStackable ? 16 : 1;
                    int stackLimit = isStackable ? 99 : 1;
                    int giveQuantity = (requestedQuantity.isPresent() ? requestedQuantity.get() : defaultQuantity);
                    // TODO: check block quantity vs giveObject(giveQuantity)
                    // TODO: use blockManager instead?
                    EntityRef block = blockItemFactory.newInstance(blockFamily, Math.min(giveQuantity, stackLimit));
                    displayName = blockFamily.getDisplayName();
                    givenItems = giveObject(block, displayName, giveQuantity, client);
                }
            } else {
                Prefab prefab = assetManager.getAsset(matchingUrns.iterator().next(), Prefab.class).orElse(null);
                if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                    boolean isStackable = !prefab.getComponent(ItemComponent.class).stackId.isEmpty();
                    int defaultQuantity = isStackable ? 16 : 1;
                    int stackLimit = isStackable ? 99 : 1;
                    int giveQuantity = (requestedQuantity.isPresent() ? requestedQuantity.get() : defaultQuantity);
                    EntityRef item = entityManager.create(prefab);
                    displayName = prefab.getName();
                    givenItems = giveObject(item, displayName, giveQuantity, client);
                }
            }
        }

        if (givenItems > 0) {
            return "You received "
                    + (removedItems > 1
                    ? removedItems + " " + (isBlock ? "blocks" : "items") + " of "
                    : (isBlock ? "a block" : "an item") + " of ")
                    + displayName;
        } else {
            return "Could not find an item or block matching \""
                    + inventoryObjectUri
                    + "\" in your inventory";
        }
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
                    removedItems = removeItem(item, displayName, removalQuantity, client);
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

    @Command(shortDescription = "Gives multiple stacks of blocks matching a search",
            helpText = "Adds all blocks that match the search parameter into your inventory",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String bulkGiveBlock(
            @Sender EntityRef sender,
            @CommandParam("searched") String searched,
            @CommandParam(value = "quantity", required = false) Integer quantityParam,
            @CommandParam(value = "shapeName", required = false) String shapeUriParam) {

        if (quantityParam != null && quantityParam < 1) {
            return "Here, have these zero (0) blocks just like you wanted";
        }

        String searchLowercase = searched.toLowerCase();
        List<String> blocks = findBlockMatches(searchLowercase);
        String result = "Found " + blocks.size() + " block matches when searching for '" + searched + "'.";
        if (blocks.size() > 0) {
            result += "\nBlocks:";
            for (String block : blocks) {
                result += "\n" + block + "\n";
                result += giveBlock(sender, block, quantityParam, shapeUriParam);
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

    private int giveObject(EntityRef object, String displayName, final int removalQuantity, EntityRef client) {
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        for (int quantityLeft = removalQuantity; quantityLeft > 0; quantityLeft--) {
            if (!inventoryManager.giveItem(playerEntity, playerEntity, object)) {
                object.destroy();
                return quantityLeft;
            }
        }
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
                        !prefab.getComponent(ItemComponent.class).stackId.isEmpty(),
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

    private List<String> findBlockMatches(String searchLowercase) {
        return assetManager.getAvailableAssets(BlockFamilyDefinition.class)
                .stream().<Optional<BlockFamilyDefinition>>map(urn -> assetManager.getAsset(urn, BlockFamilyDefinition.class))
                .filter(def -> def.isPresent() && def.get().isLoadable() && matchesSearch(searchLowercase, def.get()))
                .map(r -> new BlockUri(r.get().getUrn()).toString()).collect(Collectors.toList());
    }
}