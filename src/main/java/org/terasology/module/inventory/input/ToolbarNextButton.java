// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.input;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.ActivateMode;
import org.terasology.input.InputType;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.ControllerId;

@RegisterBindButton(id = "toolbarNext", description = "${engine:menu#next-toolbar-item}", mode = ActivateMode.PRESS, category = "inventory")
@DefaultBinding(type = InputType.MOUSE_WHEEL, id = 1)
@DefaultBinding(type = InputType.CONTROLLER_BUTTON, id = ControllerId.ONE)
public class ToolbarNextButton extends BindButtonEvent {
}
