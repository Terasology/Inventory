// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.module.inventory.components.InventoryComponent;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression coverage for the bug fixed alongside {@link #preSave} / {@link #postSave}: an explicit or shutdown
 * save calls those, not {@link #preAutoSave} / {@link #postAutoSave} - so without the delegation added here,
 * whatever was on the player's drag-and-drop cursor at that moment never made it into the persisted inventory.
 */
public class InventoryUIClientSystemTest {

    private InventoryUIClientSystem system;
    private InventoryManager inventoryManager;
    private LocalPlayer localPlayer;
    private EntityRef playerEntity;
    private EntityRef movingItemSlot;
    private InventoryComponent playerInventory;

    @BeforeEach
    public void setup() {
        system = new InventoryUIClientSystem();
        inventoryManager = mock(InventoryManager.class);
        localPlayer = mock(LocalPlayer.class);
        Context context = new ContextImpl();
        context.put(InventoryManager.class, inventoryManager);
        context.put(LocalPlayer.class, localPlayer);
        InjectionHelper.inject(system, context);

        playerEntity = mock(EntityRef.class);
        movingItemSlot = mock(EntityRef.class);
        CharacterComponent characterComponent = new CharacterComponent();
        characterComponent.movingItem = movingItemSlot;
        playerInventory = new InventoryComponent(9);

        when(localPlayer.getCharacterEntity()).thenReturn(playerEntity);
        when(playerEntity.getComponent(CharacterComponent.class)).thenReturn(characterComponent);
        when(playerEntity.getComponent(InventoryComponent.class)).thenReturn(playerInventory);
    }

    @Test
    public void preSaveFlushesCursorItemIntoInventoryLikePreAutoSaveDoes() {
        EntityRef heldItem = mock(EntityRef.class);
        when(inventoryManager.getItemInSlot(movingItemSlot, 0)).thenReturn(heldItem);
        when(inventoryManager.getStackSize(heldItem)).thenReturn(1);

        system.preSave();

        List<Integer> expectedSlots = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
        verify(inventoryManager).moveItemToSlots(movingItemSlot, movingItemSlot, 0, playerEntity, expectedSlots);
    }

    @Test
    public void postSaveRestoresItemToCursorLikePostAutoSaveDoes() {
        EntityRef heldItem = mock(EntityRef.class);
        when(inventoryManager.getItemInSlot(movingItemSlot, 0)).thenReturn(heldItem);
        when(inventoryManager.getStackSize(heldItem)).thenReturn(1);
        system.preSave();

        // The flush landed the item in slot 0 of the player's own inventory.
        when(inventoryManager.getItemInSlot(playerEntity, 0)).thenReturn(heldItem);

        system.postSave();

        verify(inventoryManager, times(1))
                .moveItem(eq(playerEntity), eq(movingItemSlot), eq(0), eq(movingItemSlot), eq(0), eq(1));
    }

    @Test
    public void preSaveIsANoOpWhenNothingIsOnTheCursor() {
        when(inventoryManager.getItemInSlot(movingItemSlot, 0)).thenReturn(EntityRef.NULL);
        when(inventoryManager.getStackSize(EntityRef.NULL)).thenReturn(0);

        system.preSave();

        verify(inventoryManager, never())
                .moveItemToSlots(any(), any(), anyInt(), any(), Mockito.anyList());
    }
}
