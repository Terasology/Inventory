/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


@InputCategory(id = "inventory",
        displayName = "${engine:menu#category-inventory}",
        ordering = {
                "Inventory:dropItem",
                "Inventory:inventory",
                "Inventory:toolbarPrev",
                "Inventory:toolbarNext",
                "Inventory:toolbarSlot0",
                "Inventory:toolbarSlot1",
                "Inventory:toolbarSlot2",
                "Inventory:toolbarSlot3",
                "Inventory:toolbarSlot4",
                "Inventory:toolbarSlot5",
                "Inventory:toolbarSlot6",
                "Inventory:toolbarSlot7",
                "Inventory:toolbarSlot8",
                "Inventory:toolbarSlot9"
        }) package org.terasology.input.binds.inventory;

import org.terasology.input.InputCategory;
