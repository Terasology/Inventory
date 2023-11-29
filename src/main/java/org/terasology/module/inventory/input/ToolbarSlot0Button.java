// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.input;

import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "toolbarSlot0", description = "${engine:menu#binding-toolbar-1}", category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.KEY_1)
public class ToolbarSlot0Button extends ToolbarSlotButton {
    public ToolbarSlot0Button() {
        super(0);
    }
}
