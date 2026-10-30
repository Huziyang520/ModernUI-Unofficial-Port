/*
 * Modern UI.
 * Copyright (C) 2026 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.ui.studio;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Home of the settings studio: a bento grid of tiles. Opening a tile slides a panel over
 * the grid; there is no tab strip and no page view.
 */
public class StudioHome extends FrameLayout {

    public interface Pages {
        void build(int index, StudioPanel panel);
    }

    private final Context mContext;
    private final String[] mTitles;
    private final String[] mSubtitles;
    private final Pages mPages;
    private final LinearLayout mHub;
    private final View mScrim;
    private StudioPanel mOpen;

    public StudioHome(@NonNull Context context, @NonNull String[] titles,
                      @NonNull String[] subtitles, @NonNull Pages pages,
                      @NonNull String header, @NonNull String sub) {
        super(context);
        mContext = context;
        mTitles = titles;
        mSubtitles = subtitles;
        mPages = pages;

        mHub = new LinearLayout(context);
        mHub.setOrientation(LinearLayout.VERTICAL);
        int pad = Studio.dp(context, Studio.PAD);
        mHub.setPadding(pad, Studio.dp(context, 22), pad, Studio.dp(context, 26));

        var title = new TextView(context);
        title.setText(header);
        title.setTextSize(21);
        title.setTextColor(Studio.onSurface(context));
        mHub.addView(title, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        var subTitle = new TextView(context);
        subTitle.setText(sub);
        subTitle.setTextSize(11);
        subTitle.setTextColor(Studio.muted(context));
        var subParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        subParams.setMargins(0, Studio.dp(context, 3), 0, Studio.dp(context, 18));
        mHub.addView(subTitle, subParams);

        for (int row = 0; row * 2 < titles.length; row++) {
            var line = new LinearLayout(context);
            line.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < 2; col++) {
                int index = row * 2 + col;
                var params = new LinearLayout.LayoutParams(0, Studio.dp(context, 116), 1f);
                params.setMargins(col == 0 ? 0 : Studio.dp(context, Studio.GAP) / 2,
                        Studio.dp(context, Studio.GAP) / 2,
                        col == 0 ? Studio.dp(context, Studio.GAP) / 2 : 0,
                        Studio.dp(context, Studio.GAP) / 2);
                if (index < titles.length) {
                    var tile = new StudioTile(context, index, titles[index], subtitles[index]);
                    tile.setOnClickListener(v -> open(index));
                    line.addView(tile, params);
                } else {
                    line.addView(new View(context), params);
                }
            }
            mHub.addView(line, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }

        var scroll = new ScrollView(context);
        scroll.addView(mHub, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        addView(scroll, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mScrim = new View(context);
        mScrim.setBackground(Studio.rounded(context, 0x66000000, 0));
        mScrim.setVisibility(GONE);
        mScrim.setAlpha(0f);
        mScrim.setClickable(true);
        mScrim.setOnClickListener(v -> close());
        addView(mScrim, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    public void open(int index) {
        if (mOpen != null) {
            return;
        }
        var panel = new StudioPanel(mContext);
        panel.setTitle(mTitles[index]);
        panel.setOnBack(this::close);
        mPages.build(index, panel);
        mOpen = panel;

        addView(panel, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mScrim.setVisibility(VISIBLE);
        mScrim.setAlpha(0f);

        float distance = Studio.dp(mContext, 26);
        panel.setTranslationY(distance);
        panel.setAlpha(0f);
        StudioAnim.run(panel, 240, t -> {
            panel.setTranslationY(distance * (1f - t));
            panel.setAlpha(t);
            mScrim.setAlpha(t * 0.55f);
        });
    }

    public void close() {
        var panel = mOpen;
        if (panel == null) {
            return;
        }
        mOpen = null;
        float distance = Studio.dp(mContext, 26);
        StudioAnim.run(panel, 180, t -> {
            panel.setTranslationY(distance * t);
            panel.setAlpha(1f - t);
            mScrim.setAlpha((1f - t) * 0.55f);
        });
        panel.postOnAnimationDelayed(() -> {
            removeView(panel);
            mScrim.setVisibility(GONE);
        }, 200);
    }

    public boolean isPanelOpen() {
        return mOpen != null;
    }
}
