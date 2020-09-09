// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.rendering.nui.layers.hud;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.inventory.logic.InventoryUtils;
import org.terasology.inventory.logic.events.DropItemRequest;
import org.terasology.math.geom.Vector3f;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.events.NUIMouseClickEvent;
import org.terasology.nui.input.MouseInput;

/**
 * A region/layer around the inventory grid to allow players to get rid of extra items and have free inventory slots by
 * dropping them onto this layer.
 */
public class DropItemRegion extends CoreHudWidget {

    @In
    private LocalPlayer localPlayer;

    private final InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            MouseInput mouseButton = event.getMouseButton();
            if (mouseButton == MouseInput.MOUSE_LEFT || mouseButton == MouseInput.MOUSE_RIGHT) {
                EntityRef playerEntity = localPlayer.getCharacterEntity();
                EntityRef movingItem = playerEntity.getComponent(CharacterComponent.class).movingItem;
                EntityRef item = InventoryUtils.getItemAt(movingItem, 0);
                if (!item.exists()) {
                    return true;
                }
                int count = 1;
                if (mouseButton == MouseInput.MOUSE_LEFT) {
                    count = InventoryUtils.getStackCount(item);     //Drop complete stack with left click
                }

                Vector3f position = localPlayer.getViewPosition();
                Vector3f direction = localPlayer.getViewDirection();
                Vector3f newPosition = new Vector3f(position.x + direction.x * 1.5f,
                        position.y + direction.y * 1.5f,
                        position.z + direction.z * 1.5f
                );
                //send DropItemRequest
                Vector3f impulseVector = new Vector3f(direction);
                playerEntity.send(new DropItemRequest(item, playerEntity,
                        impulseVector,
                        newPosition,
                        count));
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
