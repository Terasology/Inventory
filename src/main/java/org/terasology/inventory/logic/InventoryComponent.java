// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.inventory.logic;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.network.ReplicationCheck;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.engine.world.block.items.AddToBlockBasedItem;
import org.terasology.nui.reflection.metadata.FieldMetadata;

import java.util.List;

/**
 * Allows an entity to store items.
 */
@ForceBlockActive
@AddToBlockBasedItem
public final class InventoryComponent implements Component, ReplicationCheck {

    public boolean privateToOwner = true;

    @Replicate
    @Owns
    public List<EntityRef> itemSlots = Lists.newArrayList();

    public InventoryComponent() {
    }

    public InventoryComponent(int numSlots) {
        for (int i = 0; i < numSlots; ++i) {
            itemSlots.add(EntityRef.NULL);
        }
    }

    @Override
    public boolean shouldReplicate(FieldMetadata<?, ?> field, boolean initial, boolean toOwner) {
        return !privateToOwner || toOwner;
    }
}