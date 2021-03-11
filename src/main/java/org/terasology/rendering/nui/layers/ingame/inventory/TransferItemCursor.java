/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame.inventory;

import com.google.common.primitives.UnsignedBytes;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.CursorAttachment;

/**
 */
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
