package com.gamexy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.gamexy.screen.GameScreen;
import com.gamexy.screen.TitleScreen;
import com.gamexy.settings.DisplaySettingsApplier;
import com.gamexy.settings.GameSettings;

public class GamexyGame extends Game {

    private GameSettings settings;

    @Override
    public void create() {
        this.settings = new GameSettings();
        DisplaySettingsApplier.apply(this.settings);
        this.setScreen(new TitleScreen(this, settings));
    }

    public void startGame() {
        switchScreen(new GameScreen(this, settings));
    }

    public void showTitleScreen() {
        switchScreen(new TitleScreen(this, settings));
    }

    private void switchScreen(Screen nextScreen) {
        Screen previous = this.getScreen();
        this.setScreen(nextScreen);
        if (previous != null) {
            previous.dispose();
        }
    }

    @Override
    public void dispose() {
        if (this.getScreen() != null) {
            this.getScreen().dispose();
        }
        super.dispose();
    }
}
