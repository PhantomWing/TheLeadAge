package com.phantomwing.theleadage.platform.forge;

import com.phantomwing.theleadage.client.LeadedGlassTrapdoorItemRenderer;
import com.phantomwing.theleadage.item.custom.LeadedGlassTrapdoorItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Forge implementation of {@link com.phantomwing.theleadage.platform.ItemPlatform}. */
public final class ItemPlatformImpl {
    private ItemPlatformImpl() {
    }

    public static Item createLeadedGlassTrapdoorItem(Block block, Item.Properties properties) {
        return new ForgeLeadedGlassTrapdoorItem(block, properties);
    }

    /**
     * The trapdoor item drawn by its own renderer, so it shows its glass design (a builtin/entity item model). Forge
     * asks the item for it on the client alone, so the renderer is never made on a server.
     */
    private static final class ForgeLeadedGlassTrapdoorItem extends LeadedGlassTrapdoorItem {
        private ForgeLeadedGlassTrapdoorItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                private BlockEntityWithoutLevelRenderer renderer;

                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    if (renderer == null) {
                        renderer = new LeadedGlassTrapdoorItemRenderer();
                    }
                    return renderer;
                }
            });
        }
    }
}
