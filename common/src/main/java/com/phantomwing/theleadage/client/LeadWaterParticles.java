package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleColumnUpParticle;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Splashes and bubbles in water raw lead contaminates, purple as the water is: whatever made them - a mob or player
 * jumping in, rain, a bubble column. Each loader's {@code ParticleEngineMixin} hands this every particle the game adds,
 * so the particle settings decide which ones there are, as for any other.
 *
 * <p>Vanilla's splashes and bubbles are blue, which no tint can turn purple, so these are drawn with purple copies of
 * their textures, made as the particle atlas loads by the {@link Source} the atlas file
 * {@code assets/minecraft/atlases/particles.json} lists - from whichever textures are in use, so no copy of Mojang's
 * art ships in the jar. Each loader registers the source's type, as it does {@link MurkyWater}'s.</p>
 */
public final class LeadWaterParticles {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation TYPE_ID = TheLeadAge.resourceLocation("lead_purple");
    public static final MapCodec<Source> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("source").forGetter(Source::source),
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(Source::sprite),
            Codec.floatRange(0.0F, 360.0F).fieldOf("hue").forGetter(Source::hue)
    ).apply(instance, Source::new));
    private static final ResourceLocation[] SPLASHES = {
            TheLeadAge.resourceLocation("lead_splash_0"), TheLeadAge.resourceLocation("lead_splash_1"),
            TheLeadAge.resourceLocation("lead_splash_2"), TheLeadAge.resourceLocation("lead_splash_3")};
    private static final ResourceLocation BUBBLE = TheLeadAge.resourceLocation("lead_bubble");

    private LeadWaterParticles() {
    }

    public static void tint(Particle particle) {
        boolean splash = particle instanceof WaterDropParticle;
        if (!splash && !(particle instanceof BubbleParticle || particle instanceof BubbleColumnUpParticle)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        BlockPos pos = BlockPos.containing(particle.getBoundingBox().getCenter());
        // A splash sits just above the water it came out of.
        float strength = Math.max(LeadWaterTint.strength(pos), LeadWaterTint.strength(pos.below()));
        // The faintly fouled water at the edge turns some of them, the rest staying clear.
        if (strength <= 0.0F || level.random.nextFloat() >= strength * 2.0F) {
            return;
        }
        TextureAtlas particles = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES);
        TextureAtlasSprite sprite = particles.getSprite(splash ? SPLASHES[level.random.nextInt(SPLASHES.length)] : BUBBLE);
        if (!sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            ((SingleQuadParticle) particle).setSprite(sprite);
        }
    }

    /**
     * A texture made from another as the atlas loads: {@code source}'s pixels turned to {@code hue}, in degrees, their
     * saturation and brightness kept, under the name {@code sprite}.
     */
    public record Source(ResourceLocation source, ResourceLocation sprite, float hue) implements SpriteSource {
        @Override
        public void run(ResourceManager manager, SpriteSource.Output output) {
            Optional<Resource> found = manager.getResource(TEXTURE_ID_CONVERTER.idToFile(source));
            if (found.isEmpty()) {
                LOGGER.warn("No texture {} to make the lead-purple texture {} from", source, sprite);
                return;
            }
            output.add(sprite, MurkyWater.madeFrom(sprite, found.get(), this::turned));
        }

        private void turned(NativeImage image) {
            float sector = hue / 60.0F;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getPixel(x, y);
                    int r = argb >> 16 & 0xFF;
                    int g = argb >> 8 & 0xFF;
                    int b = argb & 0xFF;
                    int max = Math.max(r, Math.max(g, b));
                    int min = Math.min(r, Math.min(g, b));
                    if (argb >>> 24 == 0 || max == min) {
                        continue;
                    }
                    float chroma = max - min;
                    float second = chroma * (1.0F - Math.abs(sector % 2.0F - 1.0F));
                    float[] rgb = switch (Mth.floor(sector) % 6) {
                        case 0 -> new float[] {chroma, second, 0.0F};
                        case 1 -> new float[] {second, chroma, 0.0F};
                        case 2 -> new float[] {0.0F, chroma, second};
                        case 3 -> new float[] {0.0F, second, chroma};
                        case 4 -> new float[] {second, 0.0F, chroma};
                        default -> new float[] {chroma, 0.0F, second};
                    };
                    image.setPixel(x, y, (argb & 0xFF000000) | Math.round(rgb[0] + min) << 16
                            | Math.round(rgb[1] + min) << 8 | Math.round(rgb[2] + min));
                }
            }
        }

        @Override
        public MapCodec<Source> codec() {
            return CODEC;
        }
    }
}
