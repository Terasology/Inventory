/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.logic.inventory;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.reflection.MappedContainer;
import org.terasology.world.block.Block;

import java.util.List;

/**
 * A class that allows you to specify a player's starting inventory easily.
 * <p>
 * The default amount of items to add to the inventory is '1'. Not specifying a quantity explicitly will use this
 * default value.
 * <p>
 * Stackable <em>items</em> may have a quantity greater than their maxStackSize and they will be split into multiple
 * stacks as required. However stackable <em>blocks</em> are limited to maximum stacks of 99, any excess
 * is ignored.</p>
 *
 * <p><strong>Note</strong> it is not possible to specify nested items (e.g. items in a chest).
 * <p>
 * Example: add a delta for the player.prefab with the following
 * <pre>
 * {
 *   "StartingInventory": {
 *     "items": [
 *       { "uri": "CoreAssets:pickaxe" },
 *       { "uri": "CoreAssets:Torch", "quantity": 99 },
 *       { "uri": "CoreAdvancedAssets:chest", "quantity": 3 }
 *     ]
 *   }
 * }
 * </pre>
 */
public class StartingInventoryComponent implements Component {

    public List<InventoryItem> items = Lists.newLinkedList();

    /**
     * A simple class connecting a resource (a {@link Block} or {@link Prefab}) to a quantity
     */
    @MappedContainer
    public static class InventoryItem {
        /**
         * A resource uri, may be either a block uri or an item uri.
         */
        public String uri;

        /**
         * Must be greater than 0.
         */
        public int quantity = 1;
    }
}
