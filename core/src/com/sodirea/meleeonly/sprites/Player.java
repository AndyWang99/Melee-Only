package com.sodirea.meleeonly.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {

    private Texture player;
    private Vector2 position;

    public Player() {
        player = new Texture("player.png");
        position = new Vector2();
    }

    public Player(float x, float y) {
        player = new Texture("player.png");
        position = new Vector2(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }

    public void update(float dt) {

    }

    public void render(SpriteBatch sb) {
        sb.draw(player, position.x, position.y);
    }

    public void dispose() {
        player.dispose();
    }
}
