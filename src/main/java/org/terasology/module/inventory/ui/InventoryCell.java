// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.events.NUIMouseClickEvent;
import org.terasology.nui.events.NUIMouseWheelEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds interaction between an inventory slot and the player.
 */
public class InventoryCell extends ItemCell {
    private static final Logger logger = LoggerFactory.getLogger(InventoryCell.class);
    @LayoutConfig
    private Binding<Integer> targetSlot = new DefaultBinding<>(0);

    private Binding<EntityRef> targetInventory = new DefaultBinding<>(EntityRef.NULL);

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            MouseInput mouseButton = event.getMouseButton();
            if (mouseButton == MouseInput.MOUSE_LEFT) {
                if (event.getKeyboard().isKeyDown(Keyboard.Key.LEFT_SHIFT.getId())) {
                    moveItemSmartly();
                } else {
                    swapItem();
                }
            } else if (mouseButton == MouseInput.MOUSE_RIGHT) {
                int stackSize = InventoryUtils.getStackCount(getTargetItem());
                if (stackSize > 0 && getTransferItem().getClass() != getTargetItem().getClass()) {
                    giveAmount((stackSize + 1) / 2);
                } else {
                    int transferStackSize = InventoryUtils.getStackCount(getTransferItem());
                    if (transferStackSize > 0) {
                        takeAmount(1);
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            KeyboardDevice keyboard = event.getKeyboard();
            int amount = (keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL)) ? 2 : 1;

            //move item to the transfer slot
            if (event.getWheelTurns() > 0) {
                giveAmount(amount);
            } else {
                //get item from transfer slot
                takeAmount(amount);
            }
            return true;
        }
    };

    public InventoryCell() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.addInteractionRegion(interactionListener, icon.getTooltip(), canvas.getRegion());
    }

    @Override
    public EntityRef getTargetItem() {
        return InventoryUtils.getItemAt(getTargetInventory(), getTargetSlot());
    }

    public void bindTargetInventory(Binding<EntityRef> binding) {
        targetInventory = binding;
    }

    public EntityRef getTargetInventory() {
        return targetInventory.get();
    }

    public void setTargetInventory(EntityRef val) {
        targetInventory.set(val);
    }

    public void bindTargetSlot(Binding<Integer> binding) {
        targetSlot = binding;
    }

    public int getTargetSlot() {
        return targetSlot.get();
    }

    public void setTargetSlot(int val) {
        targetSlot.set(val);
    }

    private void swapItem() {
        EntityRef characterEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        inventoryManager.switchItem(getTransferEntity(), characterEntity, 0, getTargetInventory(), getTargetSlot());
    }

    private void giveAmount(int amount) {
        EntityRef characterEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        inventoryManager.moveItem(getTargetInventory(), characterEntity, getTargetSlot(), getTransferEntity(), 0, amount);
    }

    private void takeAmount(int amount) {
        EntityRef characterEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        inventoryManager.moveItem(getTransferEntity(), characterEntity, 0, getTargetInventory(), getTargetSlot(), amount);
    }

    private void moveItemSmartly() {
        EntityRef fromEntity = getTargetInventory();
        int fromSlot = getTargetSlot();
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        InventoryComponent playerInventory = playerEntity.getComponent(InventoryComponent.class);
        if (playerInventory == null) {
            return;
        }
        CharacterComponent characterComponent = playerEntity.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Character entity of player had no character component");
            return;
        }
        int totalSlotCount = playerInventory.itemSlots.size();

        EntityRef interactionTarget = characterComponent.predictedInteractionTarget;
        InventoryComponent interactionTargetInventory = interactionTarget.getComponent(InventoryComponent.class);


        EntityRef targetEntity;
        List<Integer> toSlots = new ArrayList<>(totalSlotCount);
        if (fromEntity.equals(playerEntity)) {

            if (interactionTarget.exists() && interactionTargetInventory != null) {
                targetEntity = interactionTarget;
                toSlots = numbersBetween(0, interactionTargetInventory.itemSlots.size());
            } else {
                targetEntity = playerEntity;
                int hudSlotCount = 10; // TODO use a constant once there is one
                boolean fromHud = (fromSlot < hudSlotCount);
                boolean toHud = !fromHud;
                if (toHud) {
                    toSlots = numbersBetween(0, hudSlotCount);
                } else {
                    toSlots = numbersBetween(hudSlotCount, totalSlotCount);
                }
            }
        } else {
            targetEntity = playerEntity;
            toSlots = numbersBetween(0, totalSlotCount);
        }

        CoreRegistry.get(InventoryManager.class).moveItemToSlots(getTransferEntity(), fromEntity, fromSlot, targetEntity, toSlots);
    }

    private List<Integer> numbersBetween(int start, int exclusiveEnd) {
        List<Integer> numbers = new ArrayList<>();
        for (int number = start; number < exclusiveEnd; number++) {
            numbers.add(number);
        }
        return numbers;
    }


    private EntityRef getTransferEntity() {
        return CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }

    private EntityRef getTransferItem() {
        return InventoryUtils.getItemAt(getTransferEntity(), 0);
    }

}
