// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.inventory.input.binds;

import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.Keyboard;

/**
 *
 */
@RegisterBindButton(id = "toolbarSlot5", description = "${engine:menu#binding-toolbar-6}", category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.KEY_6)
public class ToolbarSlot5Button extends ToolbarSlotButton {
    public ToolbarSlot5Button() {
        super(5);
    }
}
