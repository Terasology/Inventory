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
@RegisterBindButton(id = "toolbarSlot4", description = "${engine:menu#binding-toolbar-5}", category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.KEY_5)
public class ToolbarSlot4Button extends ToolbarSlotButton {
    public ToolbarSlot4Button() {
        super(4);
    }
}
