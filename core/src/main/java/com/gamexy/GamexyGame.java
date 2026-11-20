package com.gamexy;

import com.badlogic.gdx.Game;
import com.gamexy.screen.GameScreen;

public class GamexyGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        super.dispose();
    }
}
