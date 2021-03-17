// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.ui;

import org.terasology.module.inventory.components.SelectedInventorySlotComponent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.engine.rendering.nui.layers.hud.UICrosshair;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.LayoutConfig;
import org.terasology.engine.registry.In;

public class InventoryHud extends CoreHudWidget {

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    private UICrosshair crosshair;

    // Set "true" to use the rotating style quickslot; set "false" to get the default style quickslot
    @LayoutConfig
    private boolean rotateItems = false;

    @Override
    public void initialise() {
        for (InventoryCell cell : findAll(InventoryCell.class)) {
            int offset = cell.getTargetSlot();
            if (rotateItems) {
                cell.bindTargetSlot(new TargetSlotBinding(offset, localPlayer));
            } else {
                cell.bindSelected(new SlotSelectedBinding(offset, localPlayer));
            }
            cell.bindTargetInventory(new ReadOnlyBinding<EntityRef>() {
                @Override
                public EntityRef get() {
                    return localPlayer.getCharacterEntity();
                }
            });
        }

        crosshair = find("crosshair", UICrosshair.class);
    }

    public void setChargeAmount(float amount) {
        crosshair.setChargeAmount(amount);
    }


    private static final class SlotSelectedBinding extends ReadOnlyBinding<Boolean> {

        private int slot;
        private LocalPlayer localPlayer;

        private SlotSelectedBinding(int slot, LocalPlayer localPlayer) {
            this.slot = slot;
            this.localPlayer = localPlayer;
        }

        @Override
        public Boolean get() {
            SelectedInventorySlotComponent component = localPlayer.getCharacterEntity().getComponent(SelectedInventorySlotComponent.class);
            return component != null && component.slot == slot;
        }
    }

    private class TargetSlotBinding extends ReadOnlyBinding<Integer> {

        private int offset;
        private LocalPlayer localPlayer;

        public TargetSlotBinding(int targetSlot, LocalPlayer localPlayer) {
            this.offset = targetSlot;
            this.localPlayer = localPlayer;
        }

        @Override
        public Integer get() {
            SelectedInventorySlotComponent component =
                    localPlayer.getCharacterEntity().getComponent(SelectedInventorySlotComponent.class);
            return (component.slot + offset) % 10;
        }
    }
}
