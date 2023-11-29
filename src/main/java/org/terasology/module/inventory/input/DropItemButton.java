// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.input;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "dropItem", description = "${engine:menu#drop-item}", repeating = true, category = "inventory")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.Q)
public class DropItemButton extends BindButtonEvent {
}
