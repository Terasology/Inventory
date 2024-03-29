// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.ui;

import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.rendering.nui.CanvasUtility;
import org.terasology.engine.utilities.Assets;
import org.terasology.math.TeraMath;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.nui.widgets.TooltipLineRenderer;
import org.terasology.nui.widgets.UIList;

import java.util.ArrayList;
import java.util.List;

public class ItemIcon extends CoreWidget {

    @LayoutConfig
    private Binding<TextureRegion> icon = new DefaultBinding<>();
    @LayoutConfig
    private Binding<Mesh> mesh = new DefaultBinding<>();
    @LayoutConfig
    private Binding<Texture> meshTexture = new DefaultBinding<>();
    @LayoutConfig
    private Binding<Integer> quantity = new DefaultBinding<>(1);

    private InteractionListener listener = new BaseInteractionListener();

    private UIList<TooltipLine> tooltip;

    public ItemIcon() {
        tooltip = new UIList<>();
        tooltip.setInteractive(false);
        tooltip.setSelectable(false);
        final UISkin defaultSkin = Assets.getSkin("Inventory:itemTooltip").get();
        tooltip.setSkin(defaultSkin);
        tooltip.setItemRenderer(new TooltipLineRenderer(defaultSkin));
        tooltip.bindList(new DefaultBinding<>(new ArrayList<>()));
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getIcon() != null) {
            canvas.drawTexture(getIcon());
        } else if (getMesh() != null && getMeshTexture() != null) {
            Quaternionf rotation = new Quaternionf().rotationYXZ(TeraMath.PI / 6, -TeraMath.PI / 12, 0);
            CanvasUtility.drawMesh(
                canvas, getMesh(), getMeshTexture(), canvas.getRegion(), rotation,
                new Vector3f(), 1f
            );
        }
        if (getQuantity() > 1) {
            canvas.drawText(Integer.toString(getQuantity()));
        }
        List<TooltipLine> tooltipLines = tooltip.getList();
        if (tooltipLines != null && !tooltipLines.isEmpty()) {
            canvas.addInteractionRegion(listener);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (icon != null) {
            TextureRegion texture = icon.get();
            if  (texture != null) {
                return texture.size();
            }
        }
        return new Vector2i();
    }

    @Override
    public float getTooltipDelay() {
        return 0;
    }

    public void bindIcon(Binding<TextureRegion> binding) {
        icon = binding;
    }

    public TextureRegion getIcon() {
        return icon.get();
    }

    public void setIcon(TextureRegion val) {
        icon.set(val);
    }

    public void bindQuantity(Binding<Integer> binding) {
        quantity = binding;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int val) {
        quantity.set(val);
    }

    public void bindMesh(Binding<Mesh> binding) {
        mesh = binding;
    }

    public Mesh getMesh() {
        return mesh.get();
    }

    public void setMesh(Mesh val) {
        mesh.set(val);
    }

    public void bindMeshTexture(Binding<Texture> binding) {
        meshTexture = binding;
    }

    public Texture getMeshTexture() {
        return meshTexture.get();
    }

    public void setMeshTexture(Texture val) {
        meshTexture.set(val);
    }

    public void bindTooltipLines(Binding<List<TooltipLine>> lines) {
        tooltip.bindList(lines);
    }

    public void setTooltipLines(List<TooltipLine> lines) {
        tooltip.setList(lines);
    }

    @Override
    public UIWidget getTooltip() {
        if (tooltip.getList().size() > 0) {
            return tooltip;
        } else {
            return null;
        }
    }
}
