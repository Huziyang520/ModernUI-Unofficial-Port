/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc.text;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import icyllis.modernui.graphics.text.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.*;
import net.minecraft.client.gui.font.glyphs.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.IntFunction;

/**
 * This class is used only for <b>compatibility</b>.
 * <p>
 * Some mods may manually call {@link net.minecraft.client.gui.Font#prepareText},
 * we have to provide per-code-point glyph info. Minecraft vanilla maps code points
 * to glyphs without text shaping (no international support). It also ignores
 * resolution level (GUI scale), we assume it's current and round it up. Vanilla
 * doesn't support FreeType embolden as well, we ignore it.
 * <p>
 * This class is similar to {@link FontCollection} but no font itemization.
 * <p>
 * We use our own font atlas and rectangle packing algorithm.
 *
 * @author BloCamLimb
 * @since 3.8
 */
public class StandardFontSet extends FontSet {

    @Unmodifiable
    private List<FontFamily> mFamilies = Collections.emptyList();

    private CodepointMap<BakedGlyph> mGlyphs;

    private final IntFunction<BakedGlyph> mCacheGlyph = this::cacheGlyph;

    private float mResLevel = 2;
    private final FontPaint mStandardPaint = new FontPaint();

    private final GlyphSource mGlyphSource = new GlyphSource() {
        @Nonnull
        @Override
        public BakedGlyph getGlyph(int codepoint) {
            return StandardFontSet.this.getGlyph(codepoint);
        }

        @Nonnull
        @Override
        public BakedGlyph getRandomGlyph(@Nonnull RandomSource random, int width) {
            return StandardFontSet.this.getRandomGlyph(random, width);
        }
    };

    public StandardFontSet(@Nonnull TextureManager texMgr,
                           @Nonnull Identifier fontName) {
        super(new GlyphStitcher(texMgr, fontName));

        mStandardPaint.setFontStyle(FontPaint.NORMAL);
        mStandardPaint.setLocale(Locale.ROOT);
    }

    public void reload(@Nonnull FontCollection fontCollection, int newResLevel) {
        super.reload(Collections.emptyList(), Collections.emptySet());
        mFamilies = fontCollection.getFamilies();
        invalidateCache(newResLevel);
    }

    public void invalidateCache(int newResLevel) {
        if (mGlyphs != null) {
            mGlyphs.clear();
        }
        int fontSize = TextLayoutProcessor.computeFontSize(newResLevel);
        mStandardPaint.setFontSize(fontSize);
        mStandardPaint.setAntiAlias(GlyphManager.sAntiAliasing);
        mStandardPaint.setLinearMetrics(GlyphManager.sFractionalMetrics);
        mResLevel = newResLevel;
    }

    @Nonnull
    private BakedGlyph cacheGlyph(int codePoint) {
        if (isZeroWidthEmojiJoiner(codePoint)) {
            // Code points that only make sense inside a multi-code-point emoji sequence:
            // ZWJ, variation selectors, skin tone modifiers, keycap and tag characters.
            // The world text path resolves glyphs one code point at a time
            // (GlyphSource#getGlyph), so a sequence cannot be composed here, and drawing such
            // a character on its own only produced a missing-glyph square between the parts
            // of the emoji. They are all zero width, so reserve no space for them and let the
            // remaining parts of the sequence be drawn next to each other.
            return new EmptyBakedGlyph(GlyphInfo.simple(0));
        }
        for (FontFamily family : mFamilies) {
            if (!family.hasGlyph(codePoint)) {
                continue;
            }
            Font font = family.getClosestMatch(FontPaint.NORMAL);
            // we MUST check BitmapFont first,
            // because codePoint may be an invalid Unicode code point
            // but vanilla doesn't validate that
            if (font instanceof BitmapFont bitmapFont) {
                var info = bitmapFont.getGlyph(codePoint);
                if (info == null) {
                    continue;
                }
                // bake glyph ourselves
                var glyph = GlyphManager.getInstance().lookupGlyph(
                        bitmapFont,
                        (int) mStandardPaint.getFontSize(),
                        codePoint
                );
                if (glyph != null) {
                    // convert to Minecraft, see SheetGlyphInfo
                    float up = TextLayout.STANDARD_BASELINE_OFFSET +
                            (float) glyph.y / TextLayoutEngine.BITMAP_SCALE;
                    float left = (float) glyph.x / TextLayoutEngine.BITMAP_SCALE;
                    float right = left + (float) glyph.width / TextLayoutEngine.BITMAP_SCALE;
                    float down = up + (float) glyph.height / TextLayoutEngine.BITMAP_SCALE;
                    Identifier textureName = bitmapFont.getCurrentTextureName();
                    return new BakedSheetGlyph(
                            info,
                            new GlyphRenderTypes(
                                    TextRenderType.getOrCreate(textureName, net.minecraft.client.gui.Font.DisplayMode.NORMAL, true),
                                    TextRenderType.getOrCreate(textureName, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, true),
                                    TextRenderType.getOrCreate(textureName, net.minecraft.client.gui.Font.DisplayMode.POLYGON_OFFSET, true),
                                    TextRenderType.getPipelineForGui(TextRenderType.MODE_NORMAL, true)
                            ),
                            GlyphManager.getInstance().getCurrentTexture(bitmapFont).getTextureView(),
                            glyph.u1,
                            glyph.u2,
                            glyph.v1,
                            glyph.v2,
                            left,
                            right,
                            up,
                            down
                    );
                }
                // no pixels
                return new EmptyBakedGlyph(info);
            } else if (font instanceof SpaceFont spaceFont) {
                float adv = spaceFont.getAdvance(codePoint);
                if (!Float.isNaN(adv)) {
                    return new EmptyBakedGlyph(GlyphInfo.simple(adv));
                }
            } else if (font instanceof OutlineFont outlineFont) {
                // no variation selector
                if (!outlineFont.hasGlyph(codePoint, 0)) {
                    continue;
                }
                char[] chars = Character.toChars(codePoint);
                IntArrayList glyphs = new IntArrayList(1);
                float adv = outlineFont.doSimpleLayout(
                        chars,
                        0, chars.length,
                        mStandardPaint, glyphs, null,
                        0, 0
                );
                var info = new StandardGlyphInfo((adv / mResLevel));
                if (glyphs.size() == 1 &&
                        glyphs.getInt(0) != 0) { // 0 is the missing glyph for TTF
                    // bake glyph ourselves
                    var glyph = GlyphManager.getInstance().lookupGlyph(
                            outlineFont,
                            (int) mStandardPaint.getFontSize(),
                            glyphs.getInt(0)
                    );
                    if (glyph != null) {
                        // convert to Minecraft, see SheetGlyphInfo
                        float up = TextLayout.STANDARD_BASELINE_OFFSET +
                                (float) glyph.y / mResLevel;
                        float left = (float) glyph.x / mResLevel;
                        float right = left + (float) glyph.width / mResLevel;
                        float down = up + (float) glyph.height / mResLevel;
                        return new BakedSheetGlyph(
                                info,
                                new GlyphRenderTypes(
                                        TextRenderType.getOrCreate(GlyphManager.FONT_SHEET, net.minecraft.client.gui.Font.DisplayMode.NORMAL, true),
                                        TextRenderType.getOrCreate(GlyphManager.FONT_SHEET, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, true),
                                        TextRenderType.getOrCreate(GlyphManager.FONT_SHEET, net.minecraft.client.gui.Font.DisplayMode.POLYGON_OFFSET, true),
                                        TextRenderType.getPipelineForGui(TextRenderType.MODE_NORMAL, true)
                                ),
                                GlyphManager.getInstance().getFontTexture().getTextureView(),
                                glyph.u1,
                                glyph.u2,
                                glyph.v1,
                                glyph.v2,
                                left,
                                right,
                                up,
                                down
                        );
                    }
                }
                if (adv > 0) {
                    // no pixels, e.g. space
                    return new EmptyBakedGlyph(info);
                }
            } else if (font instanceof EmojiFont emojiFont) {
                // Color emoji are rendered by the Modern Text Engine in the GUI, but MC 26.2
                // renders all 3D world text (signs, name tags, text displays, maps, ...) through
                // TextFeatureRenderer with the glyphs from Font#prepareText, which needs a
                // BakedGlyph here as well. Without this branch every emoji in the world was drawn
                // as the missing glyph, or by a monochrome system font.
                //
                // Never claim ordinary text code points: the emoji set also ships images for the
                // keycap bases ('0'-'9', '#', '*'), claiming those would turn every number,
                // '#' and '*' of the world text into an emoji.
                if (!Emoji.isEmoji(codePoint) || codePoint < 0x100 ||
                        Character.isLetterOrDigit(codePoint)) {
                    continue;
                }
                // Keep the same sequence rule as FontResourceManager#loadEmojis, so that color
                // emoji which require VS16 (U+FE0F) - e.g. U+2764 in "❤️" - are found as well.
                String sequence = new String(Character.toChars(codePoint));
                if (!Emoji.isEmojiPresentation(codePoint)) {
                    sequence += (char) Emoji.VARIATION_SELECTOR_16;
                }
                char[] chars = sequence.toCharArray();
                IntArrayList glyphs = new IntArrayList(1);
                float adv = emojiFont.doComplexLayout(
                        chars,
                        0, chars.length,
                        0, chars.length,
                        false,
                        mStandardPaint, glyphs, null, null,
                        0, null, 0, 0
                );
                if (glyphs.size() == 1) {
                    // bake the emoji into the emoji atlas
                    var glyph = GlyphManager.getInstance().lookupGlyph(
                            emojiFont,
                            (int) mStandardPaint.getFontSize(),
                            glyphs.getInt(0)
                    );
                    if (glyph != null) {
                        // see TextRunRenderState#isColorEmoji
                        float scale = TextLayoutProcessor.sBaseFontSize / GlyphManager.EMOJI_BASE;
                        float left = (float) glyph.x * scale;
                        float right = left + (float) glyph.width * scale;
                        float up = TextLayout.STANDARD_BASELINE_OFFSET + (float) glyph.y * scale;
                        float down = up + (float) glyph.height * scale;
                        return new ColorEmojiBakedGlyph(
                                new StandardGlyphInfo(adv / mResLevel),
                                GlyphRenderTypes.createForColorTexture(GlyphManager.EMOJI_SHEET),
                                GlyphManager.getInstance().getEmojiTexture().getTextureView(),
                                glyph.u1,
                                glyph.u2,
                                glyph.v1,
                                glyph.v2,
                                left,
                                right,
                                up,
                                down
                        );
                    }
                }
                if (adv > 0) {
                    // no pixels, e.g. the emoji atlas is busy this frame
                    return new EmptyBakedGlyph(new StandardGlyphInfo(adv / mResLevel));
                }
            }
        }
        return super.source(false).getGlyph(codePoint); // missing
    }

    /**
     * Whether the code point is a zero-width part of a multi-code-point emoji sequence.
     */
    private static boolean isZeroWidthEmojiJoiner(int codePoint) {
        return codePoint == 0x200D // ZERO WIDTH JOINER
                || codePoint == 0xFE0E || codePoint == 0xFE0F // variation selectors
                || codePoint == 0x20E3 // COMBINING ENCLOSING KEYCAP
                || Emoji.isEmojiModifier(codePoint) // skin tone modifiers
                || codePoint >= 0xE0020 && codePoint <= 0xE007F; // tag characters
    }

    @Nonnull
    public BakedGlyph getGlyph(int codePoint) {
        if (mGlyphs == null) {
            mGlyphs = new CodepointMap<>(BakedGlyph[]::new, BakedGlyph[][]::new);
        }
        return mGlyphs.computeIfAbsent(codePoint, mCacheGlyph);
    }

    @Nonnull
    @Override
    public GlyphSource source(boolean nonFishyOnly) {
        return mGlyphSource;
    }

    // no obfuscated support

    public static class StandardGlyphInfo implements GlyphInfo {

        private final float mAdvance;

        public StandardGlyphInfo(float advance) {
            mAdvance = advance;
        }

        @Override
        public float getAdvance() {
            return mAdvance;
        }

        @Override
        public float getBoldOffset() {
            return 0.5f;
        }

        @Override
        public float getShadowOffset() {
            return ModernTextRenderer.sShadowOffset;
        }
    }

    public static class EmptyBakedGlyph implements BakedGlyph {

        private final GlyphInfo info;

        public EmptyBakedGlyph(@Nonnull GlyphInfo info) {
            this.info = info;
        }

        @Nonnull
        @Override
        public GlyphInfo info() {
            return info;
        }

        @Nullable
        @Override
        public TextRenderable.Styled createGlyph(float x, float y, int color, int shadowColor,
                                                 @Nonnull Style style, float boldOffset, float shadowOffset) {
            return null;
        }
    }

    /**
     * A baked glyph that is backed by the color emoji atlas.
     * <p>
     * Compared with {@link BakedSheetGlyph}, the normal glyph is never tinted by the text color,
     * because the emoji image already carries its own colors. This is the same behavior as the
     * Modern Text Engine, see {@link TextRunRenderState#isColorEmoji}.
     */
    public static class ColorEmojiBakedGlyph extends BakedSheetGlyph {

        public ColorEmojiBakedGlyph(@Nonnull GlyphInfo info,
                                    @Nonnull GlyphRenderTypes renderTypes,
                                    @Nonnull GpuTextureView textureView,
                                    float u0, float u1, float v0, float v1,
                                    float left, float right, float up, float down) {
            super(info, renderTypes, textureView, u0, u1, v0, v1, left, right, up, down);
        }

        @Nullable
        @Override
        public TextRenderable.Styled createGlyph(float x, float y, int color, int shadowColor,
                                                 @Nonnull Style style, float boldOffset, float shadowOffset) {
            return super.createGlyph(x, y, 0xFFFFFFFF, shadowColor, style, boldOffset, shadowOffset);
        }
    }
}
