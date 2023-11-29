// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ItemAuthoritySystem extends BaseComponentSystem {
    @In
    private InventoryManager inventoryManager;

    @Priority(EventPriority.PRIORITY_TRIVIAL)
    @ReceiveEvent(components = ItemComponent.class)
    public void usedItem(ActivateEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp.consumedOnUse) {
            int slot = InventoryUtils.getSlotWithItem(event.getInstigator(), item);
            inventoryManager.removeItem(event.getInstigator(), event.getInstigator(), slot, true, 1);
        }
    }
}
