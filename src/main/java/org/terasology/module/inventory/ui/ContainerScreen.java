// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UILabel;

/**
 */
public class ContainerScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    @In
    private TranslationSystem i18n;

    private InventoryGrid containerInventory;

    @Override
    public void initialise() {
        InventoryGrid inventory = find("inventory", InventoryGrid.class);
        inventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });
        inventory.setCellOffset(10);

        UILabel containerTitle = find("containerTitle", UILabel.class);
        containerInventory = find("container", InventoryGrid.class);

        EntityRef characterEntity = localPlayer.getCharacterEntity();
        containerInventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return characterEntity.getComponent(CharacterComponent.class).predictedInteractionTarget;
            }
        });
        containerTitle.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                Prefab parentPrefab = characterEntity.getComponent(CharacterComponent.class).predictedInteractionTarget.getParentPrefab();
                DisplayNameComponent displayName = parentPrefab.getComponent(DisplayNameComponent.class);
                if (displayName != null) {
                    // The display name may contain a translatable string reference, thus we attempt to get the translation.
                    // If the string is just non-translatable display name the fallback mechanism will yield just the input string.
                    // NOTE: Unfortunately, this contract is not guaranteed by `TranslationSystem#translate(String)`.
                    return i18n.translate(displayName.name);
                } else {
                    return parentPrefab.getName();
                }
            }
        });
    }

    @Override
    public boolean isModal() {
        return false;
    }

}
