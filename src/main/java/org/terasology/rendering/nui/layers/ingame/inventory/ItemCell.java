/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame.inventory;

import com.google.common.primitives.UnsignedBytes;
import org.joml.Vector2i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.TooltipLine;

import java.util.List;
import java.util.Optional;

/**
 * Applies the logic to get information out of the EntityRef item
 */
public abstract class ItemCell extends CoreWidget {
    protected ItemIcon icon = new ItemIcon();
    @LayoutConfig
    private Binding<Boolean> selected = new DefaultBinding<>(false);

    public ItemCell() {
        icon.bindTooltipLines(
                new ReadOnlyBinding<List<TooltipLine>>() {
                    @Override
                    public List<TooltipLine> get() {
                        GetItemTooltip itemTooltip;

                        DisplayNameComponent displayNameComponent = getTargetItem().getComponent(DisplayNameComponent.class);
                        if (displayNameComponent != null) {
                            itemTooltip = new GetItemTooltip(displayNameComponent.name);
                        } else {
                            itemTooltip = new GetItemTooltip();
                        }
                        getTargetItem().send(itemTooltip);

                        return itemTooltip.getTooltipLines();
                    }
                });
        icon.bindIcon(new ReadOnlyBinding<TextureRegion>() {
            @Override
            public TextureRegion get() {
                if (getTargetItem().exists()) {
                    ItemComponent itemComp = getTargetItem().getComponent(ItemComponent.class);
                    if (itemComp != null && itemComp.icon != null) {
                        return itemComp.icon;
                    }
                    BlockItemComponent blockItemComp = getTargetItem().getComponent(BlockItemComponent.class);
                    if (blockItemComp == null || blockItemComp.blockFamily == null) {
                        return Assets.getTextureRegion("engine:items#questionMark").get();
                    }
                }
                return null;
            }
        });
        icon.bindMesh(new ReadOnlyBinding<Mesh>() {
            @Override
            public Mesh get() {
                BlockItemComponent blockItemComp = getTargetItem().getComponent(BlockItemComponent.class);
                if (blockItemComp != null && blockItemComp.blockFamily != null) {
                    return blockItemComp.blockFamily.getArchetypeBlock().getMeshGenerator().getStandaloneMesh();
                }
                return null;
            }
        });
        Optional<Texture> terrainTex = Assets.getTexture("engine:terrain");
        if (terrainTex.isPresent()) {
            icon.setMeshTexture(terrainTex.get());
        } else {
            icon.setMeshTexture(Assets.getTexture("engine:default").get());
        }
        icon.bindQuantity(new ReadOnlyBinding<Integer>() {
            @Override
            public Integer get() {
                ItemComponent itemComp = getTargetItem().getComponent(ItemComponent.class);
                if (itemComp != null) {
                    return UnsignedBytes.toInt(itemComp.stackCount);
                }
                return 1;
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        getTargetItem().send(new BeforeInventoryCellRendered(canvas));

        canvas.drawWidget(icon);

        getTargetItem().send(new InventoryCellRendered(canvas));
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return canvas.calculateRestrictedSize(icon, sizeHint);
    }

    public abstract EntityRef getTargetItem();

    public void bindSelected(Binding<Boolean> binding) {
        selected = binding;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean val) {
        selected.set(val);
    }

    @Override
    public float getTooltipDelay() {
        return 0;
    }

    @Override
    public String getMode() {
        if (isSelected()) {
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }
}
