package com.sodirea.meleeonly.sprites;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import static com.sodirea.meleeonly.states.PlayState.PIXELS_TO_METERS;

public class Fox extends Enemy {

    private Box2dSteerable steerable;
    private Wander<Vector2> wander;
    private CentralRayWithWhiskersConfiguration<Vector2> config;
    private RaycastObstacleAvoidance<Vector2> avoidance;
    private Pursue<Vector2> pursue;
    private BlendedSteering<Vector2> steering;
    private SteeringAcceleration<Vector2> steeringOutput;

    private float sprintSpeed; // will be part of the constructor, with hp and damage as well

    public Fox(World world, Vector2 position, Box2dSteerable playerSteerable) {
        super(world, new Texture("fox.png"), position);

        steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());

        steerable = new Box2dSteerable();
        steerable.setMaxLinearAcceleration(1f);
        steerable.setMaxLinearSpeed(.75f);
        steerable.setMaxAngularAcceleration(.5f);
        steerable.setMaxAngularSpeed(3f);
        steerable.setBoundingRadius(getTexture().getWidth()/2);
        steerable.setZeroLinearSpeedThreshold(0.01f);
        steerable.setBody(getBody());

        wander = new Wander<Vector2>(steerable);
        wander.setFaceEnabled(true);
        wander.setAlignTolerance(0.01f);
        wander.setDecelerationRadius(0.5f);
        wander.setTimeToTarget(0.3f);
        wander.setWanderOffset(2f);
        wander.setWanderOrientation(MathUtils.random(360));
        wander.setWanderRadius(1f);
        wander.setWanderRate(MathUtils.PI2*4);

        config = new CentralRayWithWhiskersConfiguration<Vector2>(steerable, 1f, .75f, 30f);
        Box2dRaycastCollisionDetector detector = new Box2dRaycastCollisionDetector(world);
        avoidance = new RaycastObstacleAvoidance<Vector2>(steerable, config, detector);
        avoidance.setDistanceFromBoundary(1f);

        pursue = new Pursue<Vector2>(steerable, playerSteerable);
        pursue.setEnabled(true);

        steering = new BlendedSteering<Vector2>(steerable);
        steering.add(wander, 0.7f);
        steering.add(avoidance, 0.75f);

        setDetectionRange(600f);
    }

    public void sprint(){} // or shadow clones!

    public void setNormalSteering() {
        steering.add(wander, 1f);
        steering.add(avoidance, 0.75f);
        steering.remove(pursue);
        flipAggro();
    }

    public void setAggroSteering() {
        steering.remove(wander);
        steering.remove(avoidance);
        steering.add(pursue, 1f);
        flipAggro();
    }

    @Override
    public void update(float dt) {
        getPosition().set(getBody().getPosition().x/PIXELS_TO_METERS-getTexture().getWidth()/2, getBody().getPosition().y/PIXELS_TO_METERS-getTexture().getHeight()/2); // convert physics body coordinates back to render coordinates. this ensures that the rendering position is always in sync with the physics body's position

        steering.calculateSteering(steeringOutput);
        if (!steeringOutput.linear.isZero()) {
            getBody().applyForceToCenter(steeringOutput.linear.scl(100*getBody().getMass()), true);
        }

        Vector2 velocity = getBody().getLinearVelocity();
        if (!velocity.isZero()) {
            float previousAngle = getBody().getAngle();
            float angle = (float) Math.atan(velocity.y/velocity.x);
            if (velocity.x < 0) {
                angle += Math.PI;
            }
            angle *= (180/Math.PI);

            if (Math.abs(angle - previousAngle) > 2f) { // don't update orientation if the chang eis very minimal; causes stuttering
                getBody().setTransform(getBody().getPosition(), angle);
            }
        }

    }
}
