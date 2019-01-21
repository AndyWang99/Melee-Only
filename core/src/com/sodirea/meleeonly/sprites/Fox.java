package com.sodirea.meleeonly.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import static com.sodirea.meleeonly.states.PlayState.PIXELS_TO_METERS;

public class Fox extends Enemy {

    private float sprintSpeed; // will be part of the constructor, with hp and damage as well

    public Fox(World world, Vector2 position) {
        super(world, new Texture("fox.png"), position);
    }

    public void sprint(){}

    @Override
    public void update(float dt) {
        getPosition().set(getBody().getPosition().x/PIXELS_TO_METERS-getTexture().getWidth()/2, getBody().getPosition().y/PIXELS_TO_METERS-getTexture().getHeight()/2); // convert physics body coordinates back to render coordinates. this ensures that the rendering position is always in sync with the physics body's position

    }
}
