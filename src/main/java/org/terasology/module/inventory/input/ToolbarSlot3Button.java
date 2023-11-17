// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.input;

import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "toolbarSlot3", description = "${engine:menu#binding-toolbar-4}", category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.KEY_4)
public class ToolbarSlot3Button extends ToolbarSlotButton {
    public ToolbarSlot3Button() {
        super(3);
    }
}
