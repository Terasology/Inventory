// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.input.MouseInput;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.events.NUIMouseClickEvent;

/**
 * A region/layer around the inventory grid to allow players to get rid of extra items
 * and have free inventory slots by dropping them onto this layer.
 */
public class DropItemRegion extends CoreHudWidget {

    @In
    private LocalPlayer localPlayer;

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            MouseInput mouseButton = event.getMouseButton();
            if (mouseButton == MouseInput.MOUSE_LEFT || mouseButton == MouseInput.MOUSE_RIGHT) {
                EntityRef playerEntity = localPlayer.getCharacterEntity();
                EntityRef movingItem = playerEntity.getComponent(CharacterComponent.class).movingItem;
                EntityRef item  = InventoryUtils.getItemAt(movingItem, 0);
                if (!item.exists()) {
                    return true;
                }
                int count = 1;
                if (mouseButton == MouseInput.MOUSE_LEFT) {
                    count = InventoryUtils.getStackCount(item);     //Drop complete stack with left click
                }

                InventoryUtils.dropItems(item, count, localPlayer);
                return true;
            }
            return false;
        }
    };

    @Override
    public void initialise() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.addInteractionRegion(interactionListener);
    }
}
