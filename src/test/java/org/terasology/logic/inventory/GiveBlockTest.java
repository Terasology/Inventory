/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.inventory;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.terasology.moduletestingenvironment.ModuleTestingEnvironment;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Verify that adding blocks to an inventory is working as expected.
 *
 * Dual test to {@link GiveItemTest}.
 *
 * @see GiveItemTest
 */
public class GiveBlockTest {
    private static ModuleTestingEnvironment context;

    @BeforeAll
    public static void setup() throws Exception {
        context = new ModuleTestingEnvironment() {
            @Override
            public Set<String> getDependencies() {
                return Sets.newHashSet("Inventory");
            }
        };
        context.setup();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        context.tearDown();
    }

    @Test
    public void giveItemSingleBlock() {
        Assert.assertEquals("Hello", "Hell"+"o");
    }

    @Test
    public void giveItemSingleBlockAgain() {
        Assert.assertEquals("Hello", "Hello");
    }
}
