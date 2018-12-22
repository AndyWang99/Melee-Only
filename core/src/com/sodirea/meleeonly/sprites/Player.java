package com.sodirea.meleeonly.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.sodirea.meleeonly.states.PlayState.PIXELS_TO_METERS;

public class Player {

    private Texture player;
    private Vector2 position;

    private BodyDef playerBodyDef;
    private Body playerBody;
    private PolygonShape playerSquare;
    private FixtureDef playerFixtureDef;
    private Fixture playerFixture;

    public Player(World world) {
        player = new Texture("player.png");
        position = new Vector2(0, 0);

        // creating the player's physics body
        playerBodyDef = new BodyDef();
        playerBodyDef.type = BodyDef.BodyType.DynamicBody;
        playerBodyDef.position.set((position.x+player.getWidth()/2) * PIXELS_TO_METERS, (position.y+player.getHeight()/2) * PIXELS_TO_METERS); // convert render coordinates to physics body coodinates
        playerBody = world.createBody(playerBodyDef);
        playerSquare = new PolygonShape();
        playerSquare.setAsBox((player.getWidth()/2)*PIXELS_TO_METERS, (player.getHeight()/2)*PIXELS_TO_METERS);
        playerFixtureDef = new FixtureDef();
        playerFixtureDef.shape = playerSquare;
        playerFixtureDef.density = 500f;
        playerFixtureDef.friction = 10f;
        playerFixture = playerBody.createFixture(playerFixtureDef);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
        playerBody.setTransform(new Vector2((x+player.getWidth()/2) * PIXELS_TO_METERS, (y+player.getHeight()/2) * PIXELS_TO_METERS), playerBody.getAngle());
    }

    public void setPlayerLinearVelocity(float x, float y) {
        playerBody.setLinearVelocity(x, y);
    }

    public void update(float dt) {
        position.set(playerBody.getPosition().x/PIXELS_TO_METERS-player.getWidth()/2, playerBody.getPosition().y/PIXELS_TO_METERS-player.getHeight()/2); // convert physics body coordinates back to render coordinates. this ensures that the rendering position is always in sync with the physics body's position
    }

    public void render(SpriteBatch sb) {
        sb.draw(player, position.x, position.y);
    }

    public void dispose() {
        player.dispose();
    }
}
