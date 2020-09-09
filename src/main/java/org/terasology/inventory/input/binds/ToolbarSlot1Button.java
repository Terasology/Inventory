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
@RegisterBindButton(id = "toolbarSlot1", description = "${engine:menu#binding-toolbar-2}", category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.KEY_2)
public class ToolbarSlot1Button extends ToolbarSlotButton {
    public ToolbarSlot1Button() {
        super(1);
    }
}
