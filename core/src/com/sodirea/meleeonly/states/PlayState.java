package com.sodirea.meleeonly.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.sodirea.meleeonly.MeleeOnly;
import com.sodirea.meleeonly.sprites.Player;

import java.util.ArrayList;
import java.util.Random;

import static com.sodirea.meleeonly.states.PlayState.Walker.MAX_WALKER_NUM;
import static com.sodirea.meleeonly.states.PlayState.Walker.WALKER_CHANGE_DIR_CHANCE;
import static com.sodirea.meleeonly.states.PlayState.Walker.WALKER_DESPAWN_CHANCE;
import static com.sodirea.meleeonly.states.PlayState.Walker.WALKER_SPAWN_CHANCE;

public class PlayState extends State {

    public static final Vector2 MAP_DIM = new Vector2(7500, 7500);      // max dimensions of our game world in px. this means that map.length * UNIT_DIM = MAP_DIM always!!!!
    public static final Vector2 UNIT_DIM = new Vector2(150, 150);         // each unit is 50px x 50px, so our world is composed of 100 x 100 units
    public static final int MIN_NUM_TILES = 300;
    public static final int MAX_NUM_TILES = 500;
    public static final float PIXELS_TO_METERS = 0.01f;
    public static final float TIME_STEP = 1 / 300f;

    private boolean[][] map;                                                  // if true, then that pair of indices is walkable. false means it is a wall
    private Texture catBg;
    private Texture wall;
    private Texture floor;
    private Player player;
    private Stage stage;
    private Touchpad pad;
    private World world;

    public PlayState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });

        cam.setToOrtho(false, MeleeOnly.WIDTH, MeleeOnly.HEIGHT);
        //cam.setToOrtho(false, MAP_DIM.x, MAP_DIM.y);

        map = new boolean[50][50];                                           // each pair of indices represent one unit in our game world
        player = new Player(world);
        int trueCount = 0;
        while (trueCount < MIN_NUM_TILES || trueCount > MAX_NUM_TILES) {       // while the number of floors are less than min or greater than max, keep generating the map until we get something in between
            generateMap();
            trueCount = 0;
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    if (map[i][j]) {
                        trueCount++;
                    }
                }
            }
            System.out.println(trueCount);
        }

        catBg = new Texture("catbg.png");
        wall = new Texture("wall.png");
        floor = new Texture("floor.png");

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                // if this pair of coordinates is false, then it is a wall. render it into our game world
                // but if it is false, it does not necessarily mean it is a wall in the physics world;
                // only walls that are adjacent to floor (i.e. "true" in the map) will have a physics body
                if (!map[i][j]) {
                    // check if there is an adjacent floor (true in our map) to this wall (false in our map)
                    if (i > 0 && map[i-1][j]
                            || i < map.length-1 && map[i+1][j]
                            || j > 0 && map[i][j-1]
                            || j < map[i].length-1 && map[i][j+1]) { // if there is, then add a physics body to our world for this wall
                        BodyDef wallBodyDef;
                        Body wallBody;
                        PolygonShape wallBox;
                        FixtureDef wallFixtureDef;
                        Fixture wallFixture;
                        wallBodyDef = new BodyDef();
                        wallBodyDef.position.set((i * UNIT_DIM.x + wall.getWidth() / 2) * PIXELS_TO_METERS, (j * UNIT_DIM.y + wall.getHeight() / 2) * PIXELS_TO_METERS);
                        wallBody = world.createBody(wallBodyDef);
                        wallBox = new PolygonShape();
                        wallBox.setAsBox(wall.getWidth() / 2 * PIXELS_TO_METERS, wall.getHeight() / 2 * PIXELS_TO_METERS);
                        wallFixtureDef = new FixtureDef();
                        wallFixtureDef.shape = wallBox;
                        wallFixtureDef.density = 0.0f;
                        wallFixtureDef.friction = 0.0f;
                        wallFixture = wallBody.createFixture(wallFixtureDef);
                    }
                }
            }
        }

        Texture knob = new Texture("knob.png");
        Texture padbg = new Texture("padbg.png");

        stage = new Stage(new ScalingViewport(Scaling.fill, cam.viewportWidth, cam.viewportHeight));
        Skin skin = new Skin();
        skin.add("knob", knob);
        skin.add("background", padbg);
        Touchpad.TouchpadStyle style = new Touchpad.TouchpadStyle();
        style.knob = skin.getDrawable("knob");
        style.background = skin.getDrawable("background");
        pad = new Touchpad(0.5f, style);
        pad.setBounds(stage.getWidth()/12, stage.getHeight()/6, padbg.getWidth(), padbg.getHeight());
        pad.setSize(padbg.getWidth(), padbg.getHeight());
        pad.addListener(new ChangeListener() {              // listener for checking the coordinates of the touchpad
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Vector2 knobCoord = new Vector2(((Touchpad) actor).getKnobPercentX(), ((Touchpad) actor).getKnobPercentY());
                // set their running direction based on the coordinate of the knob
                player.setPlayerLinearVelocity(knobCoord.x/PIXELS_TO_METERS/3, knobCoord.y/PIXELS_TO_METERS/3);

                // set their looking direction (i.e. player's angle) based on coordinates of the knob
                if (knobCoord.x != 0 || knobCoord.y != 0) {
                    System.out.println(knobCoord);
                    float angle = (float) Math.atan(knobCoord.y/knobCoord.x);
                    if (knobCoord.x < 0) {
                        angle += (float) Math.PI;
                    }
                    player.setAngle(angle * (float) (180/Math.PI));
                }
            }
        });
        stage.addActor(pad);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    protected void handleInput() {
        /*if (Gdx.input.justTouched()) {
            int trueCount = 0;
            while (trueCount < MIN_NUM_TILES || trueCount > MAX_NUM_TILES) {    // while the number of floors are less than min or greater than max, keep generating the map until we get something in between
                generateMap();
                trueCount = 0;
                for (int i = 0; i < map.length; i++) {
                    for (int j = 0; j < map[i].length; j++) {
                        if (map[i][j]) {
                            trueCount++;
                        }
                    }
                }
                System.out.println(trueCount);
            }
        }*/
    }

    @Override
    public void update(float dt) {
        handleInput();
        player.update(dt);

        cam.position.x = player.getPosition().x;
        cam.position.y = player.getPosition().y;
        cam.update();
        stage.act();
        world.step(TIME_STEP, 6, 2);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(catBg, cam.position.x-cam.viewportWidth/2, cam.position.y-cam.viewportHeight/2);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                // if this pair of coordinates is false, then it is a wall. render it into our game world
                // but if it is false, it does not necessarily mean it is a wall in the physics world;
                // only walls that are adjacent to floor (i.e. "true" in the map) will have a physics body
                if (!map[i][j]) {
                    sb.draw(wall, i*UNIT_DIM.x, j*UNIT_DIM.y);
                } else {
                    sb.draw(floor, i*UNIT_DIM.x, j*UNIT_DIM.y); // else it is floor, so add texture for floor
                }
            }
            /*// everytime we iterate a row, add extra visual walls "outside" of the map
            // (i.e. indexes less than 0 and greater than the map.length)
            // so the player doesn't just peer into the void
            // the i'th row will add walls for the i'th row and i'th column
            for (int k = 1; k <= 7; k++) {
                sb.draw(wall, i*UNIT_DIM.x, -k*UNIT_DIM.y);
                sb.draw(wall, i*UNIT_DIM.x, (map[i].length+k)*UNIT_DIM.y);
                sb.draw(wall, -k*UNIT_DIM.x, i*UNIT_DIM.y);
                sb.draw(wall, (map.length+k)*UNIT_DIM.x, i*UNIT_DIM.y);
            }*/
        }
        player.render(sb);
        sb.end();
        stage.draw();
    }

    @Override
    public void dispose() {
        catBg.dispose();
        wall.dispose();
        floor.dispose();
        player.dispose();
        stage.dispose();
    }

    // Walker class is used to help generate the map using the Drunkard Walk algorithm with multiple walkers
    protected class Walker {
        public static final int MAX_WALKER_NUM = 10;
        public static final float WALKER_SPAWN_CHANCE = 0.05f;
        public static final float WALKER_DESPAWN_CHANCE = 0.05f;
        public static final float WALKER_CHANGE_DIR_CHANCE = 0.7f;

        private Vector2 pos;            // position of walker in terms of our map indices
        private Vector2 dir;            // each coordinate is either -1 or 0 or 1; -1 means move left for x, or move down for y.

        private Walker(Vector2 pos, Vector2 dir) {
            this.pos = pos;
            this.dir = dir;
        }

        // walk the Walker 1 index in its direction. if it hits a dead-end, change its direction
        // a dead-end is 1 index before the bounds of the array. so [1 or map.length-2][1 or map.length-2]. we reserve the bounds of the array (0 and map.length-1) for walls
        void walk() {
            if (pos.x + dir.x < 1) { // check if the walker will walk out of bounds.
                dir.x = 0;                     // note that if it does, we don't want to simply reverse the direction (since it would be meaningless to re-walk the same path).
                dir.y = -1;                    // so make it walk perpendicular
                walk();                        // re-walk the Walker with the new direction
            } else if (pos.x + dir.x > map.length-2) {
                dir.x = 0;
                dir.y = 1;
                walk();
            } else if (pos.y + dir.y < 1) {
                dir.y = 0;
                dir.x = 1;
                walk();
            } else if (pos.y + dir.y > map[(int) pos.x].length-2) {
                dir.y = 0;
                dir.x = -1;
                walk();
            } else {
                pos.x += dir.x;
                pos.y += dir.y;
            }
        }

        void changeDir() {
            dir = generateNewDirection();
        }

        Vector2 getPos() {
            return pos;
        }
    }

    private Vector2 generateNewDirection() {
        Random rng = new Random();
        Vector2 direction = new Vector2(0, 0);
        if (rng.nextBoolean()) {                                        // if true, the new walker will walk x. if false, it will walk y
            if (rng.nextBoolean()) {                                    // if true, it will walk in +x
                direction.x = 1;
            } else {                                                    // else it will walk -x
                direction.x = -1;
            }
        } else {
            if (rng.nextBoolean()) {
                direction.y = 1;
            } else {
                direction.y = -1;
            }
        }
        return direction;
    }

    // generates the map
    private void generateMap() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = false;                                       // reset the map
            }
        }
        ArrayList<Walker> walkers = new ArrayList<Walker>();
        // get random x, y coordinate to start the walkers at
        Random rng = new Random();
        Vector2 initialCoord = new Vector2(rng.nextInt(map.length-2)+1, rng.nextInt(map.length-2)+1);   // initial coordinate of where our walkers start. this is also the coordinate of where the player will spawn. chooses a coordinate anywhere on the map, except on the maps bounds; i.e. [1-map.length-2][1-map.length-2]
        cam.position.x = initialCoord.x;
        cam.position.y = initialCoord.y;
        map[(int) initialCoord.x][(int) initialCoord.y] = true;
        // init the list with 4 walkers: one for each direction
        walkers.add(new Walker(initialCoord, new Vector2(-1,0)));
        walkers.add(new Walker(initialCoord, new Vector2(1,0)));
        walkers.add(new Walker(initialCoord, new Vector2(0,-1)));
        walkers.add(new Walker(initialCoord, new Vector2(0,1)));

        while (walkers.size() > 0) {
            for (int i = 0; i < walkers.size(); i++) {
                Walker walker = walkers.get(i);
                walker.walk();                                                          // walk the walkers
                map[(int) walker.getPos().x][(int) walker.getPos().y] = true;           // set the walker's position to walkable terrain
                if (walkers.size() < MAX_WALKER_NUM) {
                    // try spawning a walker
                    if (rng.nextFloat() < WALKER_SPAWN_CHANCE) {
                        Vector2 direction = generateNewDirection(); // generate a direction for the new walker
                        walkers.add(new Walker(walker.getPos(), direction));
                    }
                }
                // generate random num to possibly de-spawn walker
                if (rng.nextFloat() < WALKER_DESPAWN_CHANCE) {
                    walkers.remove(walker);
                }
                // generate random num to possibly change the walker's direction
                if (rng.nextFloat() < WALKER_CHANGE_DIR_CHANCE) {
                    walker.changeDir();
                }
            }
        }

        player.setPosition(initialCoord.x * UNIT_DIM.x, initialCoord.y * UNIT_DIM.y);         // set coordinates of the player to the initial coordinate (which is guaranteed to be walkable)
        cam.position.x = player.getPosition().x;
        cam.position.y = player.getPosition().y;
        cam.update();
    }
}
