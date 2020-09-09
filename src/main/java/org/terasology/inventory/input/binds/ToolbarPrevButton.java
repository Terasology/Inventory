// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.inventory.input.binds;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.ActivateMode;
import org.terasology.nui.input.InputType;

/**
 *
 */
@RegisterBindButton(id = "toolbarPrev", description = "${engine:menu#previous-toolbar-item}", mode =
        ActivateMode.PRESS, category = "inventory")
@DefaultBinding(type = InputType.MOUSE_WHEEL, id = -1)
public class ToolbarPrevButton extends BindButtonEvent {
}
