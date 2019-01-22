package com.sodirea.meleeonly.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import static com.sodirea.meleeonly.states.PlayState.PIXELS_TO_METERS;

public abstract class Enemy {
    private Texture enemy;
    private Vector2 position;
    private Vector2 velocity;

    private int hp;
    private int damage;

    private BodyDef enemyBodyDef;
    private Body enemyBody;
    private CircleShape enemyCircle;
    private FixtureDef enemyFixtureDef;
    private Fixture enemyFixture;

    private boolean isAggro;
    private float detectionRange;

    protected Enemy(World world, Texture enemy, Vector2 position) {
        this.enemy = enemy;
        this.position = position;

        enemyBodyDef = new BodyDef();
        enemyBodyDef.type = BodyDef.BodyType.DynamicBody;
        enemyBodyDef.position.set((position.x+enemy.getWidth()/2) * PIXELS_TO_METERS, (position.y+enemy.getHeight()/2) * PIXELS_TO_METERS); // convert render coordinates to physics body coodinates
        enemyBody = world.createBody(enemyBodyDef);
        enemyCircle = new CircleShape();
        // this line may cause issues for sprites when they are NOT X by X pixels. will use a polygonshape in that case (and pass in the shape through the super constructor call)
        enemyCircle.setRadius((enemy.getWidth()/2) * PIXELS_TO_METERS);
        enemyFixtureDef = new FixtureDef();
        enemyFixtureDef.shape = enemyCircle;
        enemyFixtureDef.density = 5000f;
        enemyFixtureDef.friction = 10f;
        enemyFixtureDef.restitution = 0f;
        enemyFixture = enemyBody.createFixture(enemyFixtureDef);

        isAggro = false;
    }

    public Texture getTexture() {
        return enemy;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public int getHp() {
        return hp;
    }

    public void takeDamage(int damageTaken) {
        hp -= damageTaken;
    }

    public int getDamage() {
        return damage;
    }

    public Body getBody() {
        return enemyBody;
    }

    public abstract void setNormalSteering();

    public abstract void setAggroSteering();

    public boolean isAggro() {
        return isAggro;
    }

    public void flipAggro() {
        isAggro = !isAggro;
    }

    public float getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(float detectionRange) {
        this.detectionRange = detectionRange;
    }

    public abstract void update(float dt);

    public void render(SpriteBatch sb) {
        sb.draw(enemy, position.x, position.y, enemy.getWidth()/2, enemy.getHeight()/2, enemy.getWidth(), enemy.getHeight(),1f, 1f, enemyBody.getAngle(), 0, 0, enemy.getWidth(), enemy.getHeight(), false, false);
    }

    public void dispose() {
        enemy.dispose();
    }

}
