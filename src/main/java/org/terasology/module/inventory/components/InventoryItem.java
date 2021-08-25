// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.components;
import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.Block;

import java.util.List;

/**
 * A simple class connecting a resource (a {@link Block} or {@link Prefab}) to a quantity
 */
public class InventoryItem {
    /**
     * A resource uri, may be either a block uri or an item uri.
     */
    public String uri;

    /**
     * Must be greater than 0.
     * <p>
     * Default value is 1.
     */
    public int quantity = 1;

    /**
     * A list of objects to be nested inside this inventory item.
     * <p>
     * Adding inventory items to this list will cause a {@link InventoryComponent} to be added to this object. The
     * nested inventory is filled with the items specified in this list.
     * <p>
     * Default value is the empty list.
     */
    public List<InventoryItem> items = Lists.newLinkedList();
}
