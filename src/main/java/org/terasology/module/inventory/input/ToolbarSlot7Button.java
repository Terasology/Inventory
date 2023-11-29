// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.input;

import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "toolbarSlot7", description = "${engine:menu#binding-toolbar-8}", category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.KEY_8)
public class ToolbarSlot7Button extends ToolbarSlotButton {
    public ToolbarSlot7Button() {
        super(7);
    }
}
