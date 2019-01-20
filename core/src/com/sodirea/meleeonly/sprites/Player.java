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

    private Texture attack;
    private Vector2 attackedSpot;
    private float attackedAngle;
    private boolean isAttacking;
    private double attackTimer;
    // NOTE: the delay between attacks is a property of individual weapons, which will be implemented later in the future. As is damage and range (the multiplier 1.3x in translateX/Y)
    // also, the translateX/Y's value will ALSO be different for different weapons. here, we assume the attack sprite (a property of the weapon) is the same size as the player
    // which is not necessarily true for all weapons. In the case that they are different sizes, we would need to center the attack sprite on top of the player first,
    // i.e. shift the sprite (player.getWidth()-attack.getWidth())/2 before applying a translate. note the equation covers
    // both cases where the attack sprite is larger, or smaller than the player. (if larger, negative shift, so left/down.
    // if smaller, positive shift, so right/up)
    // TODO:: create enemy class (abstract), with their physics bodies. create weapon class (abstract). create sensor fixtures for weapon class that activate at the same time and position as the attack sprite is drawn, and in the world contact listener, if the sensor detects contact between Weapon.class and Enemy.class, then do damage to enemy.
    private double timeUntilNextAttack;

    private BodyDef playerBodyDef;
    private Body playerBody;
    private CircleShape playerCircle;
    private FixtureDef playerFixtureDef;
    private Fixture playerFixture;

    public Player(World world) {
        player = new Texture("player.png");
        position = new Vector2(0, 0);

        attack = new Texture("attack.png");
        attackedSpot = new Vector2();
        attackedAngle = 0;
        isAttacking = false;
        attackTimer = 0;
        timeUntilNextAttack = 0;


        // creating the player's physics body
        playerBodyDef = new BodyDef();
        playerBodyDef.type = BodyDef.BodyType.DynamicBody;
        playerBodyDef.position.set((position.x+player.getWidth()/2) * PIXELS_TO_METERS, (position.y+player.getHeight()/2) * PIXELS_TO_METERS); // convert render coordinates to physics body coodinates
        playerBody = world.createBody(playerBodyDef);
        playerCircle = new CircleShape();
        playerCircle.setRadius((player.getWidth()/2) * PIXELS_TO_METERS);
        playerFixtureDef = new FixtureDef();
        playerFixtureDef.shape = playerCircle;
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

    public void setAngle(float angle) {
        playerBody.setTransform(playerBody.getWorldCenter(), angle);
    }

    public void attack() {
        if (!isAttacking && timeUntilNextAttack <= 0) {
            isAttacking = true;
            attackedSpot.x = position.x;
            attackedSpot.y = position.y;
            attackedAngle = playerBody.getAngle();
        }
    }

    public void update(float dt) {
        position.set(playerBody.getPosition().x/PIXELS_TO_METERS-player.getWidth()/2, playerBody.getPosition().y/PIXELS_TO_METERS-player.getHeight()/2); // convert physics body coordinates back to render coordinates. this ensures that the rendering position is always in sync with the physics body's position

        if (isAttacking) {
            System.out.println(dt);
            attackTimer += dt;
            if (attackTimer > 0.5) { // stop attacking after 0.5s
                isAttacking = false;
                attackTimer = 0;
                timeUntilNextAttack = 0.5;
            }
        } else {
            if (timeUntilNextAttack != 0) {
                timeUntilNextAttack -= dt;
            }
        }
    }

    public void render(SpriteBatch sb) {
        sb.draw(player, position.x, position.y, player.getWidth()/2, player.getHeight()/2, player.getWidth(), player.getHeight(),1f, 1f, playerBody.getAngle(), 0, 0, player.getWidth(), player.getHeight(), false, false);
        if (isAttacking) {
            // the attack sprite starts aligned with the player.
            // need to translate it to a radius of player.getWidth() (or height), in the direction of getAngle().
            // so find the amount needed to push it in x,y components
            float translateX = (float) (player.getWidth()*Math.cos(attackedAngle * Math.PI/180) * 1.3); // multiply by 1.3 so that it doesn't feel too cluttered
            float translateY = (float) (player.getHeight()*Math.sin(attackedAngle * Math.PI/180) * 1.3);
            sb.draw(attack, attackedSpot.x + translateX, attackedSpot.y + translateY, attack.getWidth()/2, attack.getHeight()/2, attack.getWidth(), attack.getHeight(),1f, 1f, attackedAngle, 0, 0, attack.getWidth(), attack.getHeight(), false, false);

        }
    }

    public void dispose() {
        attack.dispose();
        player.dispose();
    }
}
