package com.gamexy.ui.menu;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gamexy.ui.UiFonts;

/**
 * Common contract for a Gothic menu panel view hosted by {@link com.gamexy.screen.TitleScreen}
 * or {@link com.gamexy.ui.PauseMenu}.
 */
public interface GothicMenuView {

    void layout(int screenW, int screenH, float userUiScale);

    float getUiScale();

    default float getBackdropAlpha() {
        return 0.72f;
    }

    default void onEnter() {}

    default void onCancel() {}

    boolean handleTouchDown(float mouseUiX, float mouseUiY, int button);

    default boolean handleTouchDragged(float mouseUiX, float mouseUiY) {
        return true;
    }

    default boolean handleTouchUp(float mouseUiX, float mouseUiY, int button) {
        return true;
    }

    void renderFilled(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY);

    void renderBorders(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY);

    void renderText(SpriteBatch batch, UiFonts fonts, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY);
}
