package com.sodirea.meleeonly;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sodirea.meleeonly.states.GameStateManager;
import com.sodirea.meleeonly.states.PlayState;

public class MeleeOnly extends ApplicationAdapter {
	public static final int WIDTH = 2400;
	public static final int HEIGHT = 1440;
	public static final String TITLE = "Melee Only";
	private SpriteBatch sb;
	private GameStateManager gsm;

	@Override
	public void create () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		sb = new SpriteBatch();
		gsm = new GameStateManager();
		gsm.push(new PlayState(gsm));
	}

	@Override
	public void render () { // update and render the top-most state in the GameStateManager on every render call
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		gsm.update(Gdx.graphics.getDeltaTime());
		gsm.render(sb);
	}

	@Override
	public void dispose () {
		sb.dispose();
	}
}
