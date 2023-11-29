// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.input;

import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "toolbarSlot8", description = "${engine:menu#binding-toolbar-9}", category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.KEY_9)
public class ToolbarSlot8Button extends ToolbarSlotButton {
    public ToolbarSlot8Button() {
        super(8);
    }
}
