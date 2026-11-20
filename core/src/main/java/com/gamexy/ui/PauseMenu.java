package com.gamexy.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

/**
 * Diablo-style Gothic pause menu overlay toggled by pressing ESC.
 * Supports Resume, Restart Courtyard, Controls Reference, Settings, and Quit to Desktop.
 */
public class PauseMenu implements Disposable {

    public interface Callbacks {
        void onResume();
        void onRestart();
        void onQuit();
        float getCameraSensitivity();
        void setCameraSensitivity(float sensitivity);
    }

    private enum View {
        MAIN,
        CONTROLS,
        SETTINGS
    }

    private static final String[] MAIN_LABELS = {
        "Resume Game",
        "Restart Courtyard",
        "Controls",
        "Settings",
        "Quit to Desktop"
    };

    private static final String[][] CONTROL_BINDINGS = {
        { "W / A / S / D",        "Move & Face Barbarian" },
        { "Left-Click (Hold)",    "Use Selected Skill" },
        { "1 - 4 / Click Bar",    "Select Active Skill" },
        { "Right-Click Drag",     "Orbit Camera" },
        { "Alt + Drag",           "Orbit Camera" },
        { "Mouse Scroll",         "Zoom Camera In / Out" },
        { "R",                    "Respawn When Fallen" },
        { "Esc",                  "Pause / Resume Game" }
    };

    private final Callbacks callbacks;
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    private View view = View.MAIN;
    private int screenW = 1280;
    private int screenH = 720;

    // Layout rectangles (in Y-up UI space)
    private final Rectangle panelBounds = new Rectangle();
    private final Rectangle[] mainButtons = new Rectangle[MAIN_LABELS.length];
    private final Rectangle backButton = new Rectangle();

    // Settings controls
    private final Rectangle sensitivitySliderTrack = new Rectangle();
    private final Rectangle sensitivityMinusBtn = new Rectangle();
    private final Rectangle sensitivityPlusBtn = new Rectangle();
    private final Rectangle fullscreenToggleBtn = new Rectangle();
    private final Rectangle vsyncToggleBtn = new Rectangle();

    private boolean vsyncEnabled = true;
    private boolean draggingSlider = false;
    private float mouseUiX = -1f;
    private float mouseUiY = -1f;

    public PauseMenu(Callbacks callbacks) {
        this.callbacks = callbacks;
        for (int i = 0; i < mainButtons.length; i++) {
            mainButtons[i] = new Rectangle();
        }
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        this.screenW = Math.max(1, width);
        this.screenH = Math.max(1, height);
        uiCam.setToOrtho(false, screenW, screenH);
        uiCam.update();
        recomputeLayout();
    }

    public void resetView() {
        view = View.MAIN;
        draggingSlider = false;
        recomputeLayout();
    }

    private void recomputeLayout() {
        float panelW;
        float panelH;
        if (view == View.MAIN) {
            panelW = 390f;
            panelH = 420f;
        } else if (view == View.CONTROLS) {
            panelW = 540f;
            panelH = 440f;
        } else {
            panelW = 500f;
            panelH = 380f;
        }

        float px = (screenW - panelW) * 0.5f;
        float py = (screenH - panelH) * 0.5f;
        panelBounds.set(px, py, panelW, panelH);

        // Main menu buttons
        float btnW = panelW - 76f;
        float btnH = 44f;
        float btnGap = 12f;
        float topBtnY = py + panelH - 138f;
        for (int i = 0; i < mainButtons.length; i++) {
            mainButtons[i].set(px + 38f, topBtnY - i * (btnH + btnGap), btnW, btnH);
        }

        // Back button for sub-panels
        float backW = 190f;
        float backH = 42f;
        backButton.set(px + (panelW - backW) * 0.5f, py + 28f, backW, backH);

        // Settings controls
        float row1Y = py + panelH - 152f;
        float minusSize = 30f;
        float trackW = panelW - 160f;
        float trackX = px + (panelW - trackW) * 0.5f;
        sensitivityMinusBtn.set(trackX - minusSize - 12f, row1Y - 9f, minusSize, minusSize);
        sensitivitySliderTrack.set(trackX, row1Y, trackW, 12f);
        sensitivityPlusBtn.set(trackX + trackW + 12f, row1Y - 9f, minusSize, minusSize);

        float toggleW = panelW - 96f;
        float toggleH = 42f;
        float toggleX = px + 48f;
        fullscreenToggleBtn.set(toggleX, py + panelH - 224f, toggleW, toggleH);
        vsyncToggleBtn.set(toggleX, py + panelH - 280f, toggleW, toggleH);
    }

    /**
     * Handles Escape while paused.
     * Returns true if Escape navigated back from a sub-panel; false if on MAIN (caller should resume).
     */
    public boolean handleEscape() {
        if (view != View.MAIN) {
            view = View.MAIN;
            draggingSlider = false;
            recomputeLayout();
            return true;
        }
        return false;
    }

    public void handleMouseMoved(int screenX, int screenY) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
    }

    public boolean handleTouchDown(int screenX, int screenY, int button) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
        if (button != Input.Buttons.LEFT) {
            return true;
        }

        if (view == View.MAIN) {
            for (int i = 0; i < mainButtons.length; i++) {
                if (mainButtons[i].contains(mouseUiX, mouseUiY)) {
                    onMainButtonClicked(i);
                    return true;
                }
            }
        } else if (view == View.CONTROLS) {
            if (backButton.contains(mouseUiX, mouseUiY)) {
                view = View.MAIN;
                recomputeLayout();
                return true;
            }
        } else if (view == View.SETTINGS) {
            if (backButton.contains(mouseUiX, mouseUiY)) {
                view = View.MAIN;
                draggingSlider = false;
                recomputeLayout();
                return true;
            }
            if (sensitivityMinusBtn.contains(mouseUiX, mouseUiY)) {
                callbacks.setCameraSensitivity(callbacks.getCameraSensitivity() - 0.1f);
                return true;
            }
            if (sensitivityPlusBtn.contains(mouseUiX, mouseUiY)) {
                callbacks.setCameraSensitivity(callbacks.getCameraSensitivity() + 0.1f);
                return true;
            }
            // Generous vertical hit zone around the slider track
            if (mouseUiX >= sensitivitySliderTrack.x - 10f
                && mouseUiX <= sensitivitySliderTrack.x + sensitivitySliderTrack.width + 10f
                && mouseUiY >= sensitivitySliderTrack.y - 14f
                && mouseUiY <= sensitivitySliderTrack.y + sensitivitySliderTrack.height + 14f) {
                draggingSlider = true;
                updateSensitivityFromX(mouseUiX);
                return true;
            }
            if (fullscreenToggleBtn.contains(mouseUiX, mouseUiY)) {
                toggleFullscreen();
                return true;
            }
            if (vsyncToggleBtn.contains(mouseUiX, mouseUiY)) {
                vsyncEnabled = !vsyncEnabled;
                Gdx.graphics.setVSync(vsyncEnabled);
                return true;
            }
        }
        return true;
    }

    public boolean handleTouchDragged(int screenX, int screenY) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
        if (view == View.SETTINGS && draggingSlider) {
            updateSensitivityFromX(mouseUiX);
        }
        return true;
    }

    public boolean handleTouchUp(int screenX, int screenY, int button) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
        if (button == Input.Buttons.LEFT) {
            draggingSlider = false;
        }
        return true;
    }

    private void onMainButtonClicked(int index) {
        switch (index) {
            case 0:
                callbacks.onResume();
                break;
            case 1:
                callbacks.onRestart();
                break;
            case 2:
                view = View.CONTROLS;
                recomputeLayout();
                break;
            case 3:
                view = View.SETTINGS;
                recomputeLayout();
                break;
            case 4:
                callbacks.onQuit();
                break;
            default:
                break;
        }
    }

    private void updateSensitivityFromX(float x) {
        float ratio = MathUtils.clamp(
            (x - sensitivitySliderTrack.x) / Math.max(1f, sensitivitySliderTrack.width),
            0f, 1f);
        float sens = MathUtils.lerp(0.3f, 2.5f, ratio);
        callbacks.setCameraSensitivity(sens);
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
        } else {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(mode);
        }
    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        // 1. Filled geometry: dimmed backdrop, gothic panel, buttons, sliders
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Full-screen dark vignette backdrop
        shapes.setColor(0.02f, 0.015f, 0.03f, 0.72f);
        shapes.rect(0f, 0f, screenW, screenH);

        // Main Gothic Panel shadow & backing
        shapes.setColor(0.0f, 0.0f, 0.0f, 0.55f);
        shapes.rect(panelBounds.x + 6f, panelBounds.y - 6f, panelBounds.width, panelBounds.height);

        shapes.setColor(0.08f, 0.065f, 0.09f, 0.96f);
        shapes.rect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height);

        // Top crimson header accent bar
        shapes.setColor(0.42f, 0.08f, 0.09f, 0.95f);
        shapes.rect(panelBounds.x + 2f, panelBounds.y + panelBounds.height - 62f, panelBounds.width - 4f, 60f);

        // Subtle gold divider under header
        shapes.setColor(0.68f, 0.52f, 0.24f, 0.90f);
        shapes.rect(panelBounds.x + 18f, panelBounds.y + panelBounds.height - 64f, panelBounds.width - 36f, 2f);

        if (view == View.MAIN) {
            for (int i = 0; i < mainButtons.length; i++) {
                boolean isQuit = (i == mainButtons.length - 1);
                drawButtonFill(mainButtons[i], isQuit);
            }
        } else if (view == View.CONTROLS) {
            // Alternating row shading for readability
            float startY = panelBounds.y + panelBounds.height - 108f;
            float rowH = 31f;
            for (int i = 0; i < CONTROL_BINDINGS.length; i++) {
                if (i % 2 == 0) {
                    shapes.setColor(0.12f, 0.10f, 0.14f, 0.75f);
                    shapes.rect(panelBounds.x + 26f, startY - i * rowH - 22f, panelBounds.width - 52f, rowH - 2f);
                }
            }
            drawButtonFill(backButton, false);
        } else if (view == View.SETTINGS) {
            // Slider track & fill
            drawButtonFill(sensitivityMinusBtn, false);
            drawButtonFill(sensitivityPlusBtn, false);

            shapes.setColor(0.16f, 0.14f, 0.20f, 1f);
            shapes.rect(sensitivitySliderTrack.x, sensitivitySliderTrack.y,
                sensitivitySliderTrack.width, sensitivitySliderTrack.height);

            float sensRatio = MathUtils.clamp((callbacks.getCameraSensitivity() - 0.3f) / (2.5f - 0.3f), 0f, 1f);
            shapes.setColor(0.76f, 0.52f, 0.18f, 1f);
            shapes.rect(sensitivitySliderTrack.x, sensitivitySliderTrack.y,
                sensitivitySliderTrack.width * sensRatio, sensitivitySliderTrack.height);

            // Slider thumb knob
            float knobX = sensitivitySliderTrack.x + sensitivitySliderTrack.width * sensRatio;
            float knobY = sensitivitySliderTrack.y + sensitivitySliderTrack.height * 0.5f;
            shapes.setColor(0.98f, 0.84f, 0.34f, 1f);
            shapes.circle(knobX, knobY, 9f, 20);

            drawButtonFill(fullscreenToggleBtn, false);
            drawButtonFill(vsyncToggleBtn, false);
            drawButtonFill(backButton, false);
        }

        shapes.end();

        // 2. Ornate Gothic Borders & Outlines
        shapes.begin(ShapeRenderer.ShapeType.Line);

        // Double gold/bronze panel border
        shapes.setColor(0.66f, 0.52f, 0.28f, 0.98f);
        shapes.rect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height);
        shapes.setColor(0.38f, 0.28f, 0.16f, 0.90f);
        shapes.rect(panelBounds.x + 4f, panelBounds.y + 4f, panelBounds.width - 8f, panelBounds.height - 8f);

        // Corner decorative squares
        float c = 9f;
        shapes.setColor(0.88f, 0.72f, 0.34f, 0.95f);
        shapes.rect(panelBounds.x - 2f, panelBounds.y - 2f, c, c);
        shapes.rect(panelBounds.x + panelBounds.width - c + 2f, panelBounds.y - 2f, c, c);
        shapes.rect(panelBounds.x - 2f, panelBounds.y + panelBounds.height - c + 2f, c, c);
        shapes.rect(panelBounds.x + panelBounds.width - c + 2f, panelBounds.y + panelBounds.height - c + 2f, c, c);

        if (view == View.MAIN) {
            for (int i = 0; i < mainButtons.length; i++) {
                boolean isQuit = (i == mainButtons.length - 1);
                drawButtonBorder(mainButtons[i], isQuit);
            }
        } else if (view == View.CONTROLS) {
            drawButtonBorder(backButton, false);
        } else if (view == View.SETTINGS) {
            drawButtonBorder(sensitivityMinusBtn, false);
            drawButtonBorder(sensitivityPlusBtn, false);
            shapes.setColor(0.55f, 0.44f, 0.26f, 0.9f);
            shapes.rect(sensitivitySliderTrack.x, sensitivitySliderTrack.y,
                sensitivitySliderTrack.width, sensitivitySliderTrack.height);
            drawButtonBorder(fullscreenToggleBtn, false);
            drawButtonBorder(vsyncToggleBtn, false);
            drawButtonBorder(backButton, false);
        }

        shapes.end();

        // 3. Text layer
        batch.begin();

        float centerX = panelBounds.x + panelBounds.width * 0.5f;
        float titleY = panelBounds.y + panelBounds.height - 20f;

        if (view == View.MAIN) {
            font.getData().setScale(1.45f);
            drawCentered("GAME PAUSED", centerX, titleY, new Color(1.0f, 0.86f, 0.42f, 1f));

            font.getData().setScale(0.86f);
            drawCentered("Press ESC to resume", centerX, panelBounds.y + panelBounds.height - 76f,
                new Color(0.75f, 0.72f, 0.78f, 0.92f));

            font.getData().setScale(1.08f);
            for (int i = 0; i < mainButtons.length; i++) {
                Rectangle r = mainButtons[i];
                boolean hovered = r.contains(mouseUiX, mouseUiY);
                Color textCol = hovered ? new Color(1.0f, 0.90f, 0.48f, 1f) : new Color(0.92f, 0.90f, 0.86f, 1f);
                drawCentered(MAIN_LABELS[i], r.x + r.width * 0.5f, r.y + r.height * 0.5f + 7f, textCol);
            }
        } else if (view == View.CONTROLS) {
            font.getData().setScale(1.40f);
            drawCentered("CONTROLS", centerX, titleY, new Color(1.0f, 0.86f, 0.42f, 1f));

            float startY = panelBounds.y + panelBounds.height - 108f;
            float rowH = 31f;
            float keyX = panelBounds.x + 38f;
            float descX = panelBounds.x + 230f;

            font.getData().setScale(0.96f);
            for (int i = 0; i < CONTROL_BINDINGS.length; i++) {
                float y = startY - i * rowH;
                font.setColor(1.0f, 0.82f, 0.36f, 1f);
                font.draw(batch, CONTROL_BINDINGS[i][0], keyX, y);
                font.setColor(0.88f, 0.88f, 0.92f, 1f);
                font.draw(batch, CONTROL_BINDINGS[i][1], descX, y);
            }

            font.getData().setScale(1.05f);
            drawButtonLabel(backButton, "Back (Esc)");
        } else if (view == View.SETTINGS) {
            font.getData().setScale(1.40f);
            drawCentered("SETTINGS", centerX, titleY, new Color(1.0f, 0.86f, 0.42f, 1f));

            // Sensitivity label
            int sensPct = Math.round(callbacks.getCameraSensitivity() * 100f);
            font.getData().setScale(1.02f);
            drawCentered("Camera Orbit Sensitivity: " + sensPct + "%",
                centerX, sensitivitySliderTrack.y + 42f, new Color(0.94f, 0.90f, 0.82f, 1f));

            font.getData().setScale(1.15f);
            drawButtonLabel(sensitivityMinusBtn, "-");
            drawButtonLabel(sensitivityPlusBtn, "+");

            font.getData().setScale(1.02f);
            String fsLabel = "Display Mode: " + (Gdx.graphics.isFullscreen() ? "Fullscreen" : "Windowed (1280x720)");
            drawButtonLabel(fullscreenToggleBtn, fsLabel);

            String vsyncLabel = "VSync: " + (vsyncEnabled ? "ON" : "OFF");
            drawButtonLabel(vsyncToggleBtn, vsyncLabel);

            font.getData().setScale(1.05f);
            drawButtonLabel(backButton, "Back (Esc)");
        }

        font.getData().setScale(1.0f);
        batch.end();
    }

    private void drawButtonFill(Rectangle r, boolean danger) {
        boolean hovered = r.contains(mouseUiX, mouseUiY);
        if (danger) {
            if (hovered) {
                shapes.setColor(0.46f, 0.11f, 0.11f, 0.98f);
            } else {
                shapes.setColor(0.26f, 0.08f, 0.09f, 0.94f);
            }
        } else {
            if (hovered) {
                shapes.setColor(0.28f, 0.20f, 0.14f, 0.98f);
            } else {
                shapes.setColor(0.14f, 0.12f, 0.16f, 0.94f);
            }
        }
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void drawButtonBorder(Rectangle r, boolean danger) {
        boolean hovered = r.contains(mouseUiX, mouseUiY);
        if (hovered) {
            shapes.setColor(1.0f, 0.82f, 0.32f, 1f);
            shapes.rect(r.x - 1f, r.y - 1f, r.width + 2f, r.height + 2f);
        } else if (danger) {
            shapes.setColor(0.65f, 0.24f, 0.22f, 0.9f);
        } else {
            shapes.setColor(0.48f, 0.38f, 0.24f, 0.88f);
        }
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void drawButtonLabel(Rectangle r, String text) {
        boolean hovered = r.contains(mouseUiX, mouseUiY);
        Color col = hovered ? new Color(1.0f, 0.90f, 0.48f, 1f) : new Color(0.92f, 0.90f, 0.86f, 1f);
        drawCentered(text, r.x + r.width * 0.5f, r.y + r.height * 0.5f + 7f, col);
    }

    private void drawCentered(String text, float cx, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width * 0.5f, y);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
