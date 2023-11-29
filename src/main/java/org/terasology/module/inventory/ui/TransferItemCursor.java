// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.ui;

import com.google.common.primitives.UnsignedBytes;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.CursorAttachment;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.items.BlockItemComponent;

public class TransferItemCursor extends CursorAttachment implements ControlWidget {

    private Binding<EntityRef> item = new DefaultBinding<>(EntityRef.NULL);

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
        ItemIcon icon = new ItemIcon();
        setAttachment(icon);
        icon.bindIcon(new ReadOnlyBinding<TextureRegion>() {
            @Override
            public TextureRegion get() {
                if (getItem().exists()) {
                    ItemComponent itemComp = getItem().getComponent(ItemComponent.class);
                    if (itemComp != null) {
                        return itemComp.icon;
                    }
                    BlockItemComponent blockItemComp = getItem().getComponent(BlockItemComponent.class);
                    if (blockItemComp == null || blockItemComp.blockFamily == null) {
                        return Assets.getTextureRegion("engine:items#questionMark").orElse(null);
                    }
                }
                return null;
            }
        });
        icon.bindMesh(new ReadOnlyBinding<Mesh>() {
            @Override
            public Mesh get() {
                BlockItemComponent blockItemComp = getItem().getComponent(BlockItemComponent.class);
                if (blockItemComp != null && blockItemComp.blockFamily != null) {
                    return blockItemComp.blockFamily.getArchetypeBlock().getMeshGenerator().getStandaloneMesh();
                }
                return null;
            }
        });
        icon.setMeshTexture(Assets.getTexture("engine:terrain").get());
        icon.bindQuantity(new ReadOnlyBinding<Integer>() {
            @Override
            public Integer get() {
                ItemComponent itemComp = getItem().getComponent(ItemComponent.class);
                if (itemComp != null) {
                    return UnsignedBytes.toInt(itemComp.stackCount);
                }
                return 1;
            }
        });

        bindItem(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                CharacterComponent charComp = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
                if (charComp != null) {
                    return InventoryUtils.getItemAt(charComp.movingItem, 0);
                }
                return EntityRef.NULL;
            }
        });
    }

    public void bindItem(Binding<EntityRef> binding) {
        item = binding;
    }

    public EntityRef getItem() {
        return item.get();
    }

    public void setItem(EntityRef val) {
        item.set(val);
    }

    @Override
    public void onOpened() {
    }

    @Override
    public void onClosed() {
    }

}
