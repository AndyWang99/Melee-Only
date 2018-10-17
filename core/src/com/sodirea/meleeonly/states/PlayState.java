package com.sodirea.meleeonly.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.sodirea.meleeonly.MeleeOnly;

import java.util.ArrayList;
import java.util.Random;

import static com.sodirea.meleeonly.states.PlayState.Walker.MAX_WALKER_NUM;
import static com.sodirea.meleeonly.states.PlayState.Walker.WALKER_CHANGE_DIR_CHANCE;
import static com.sodirea.meleeonly.states.PlayState.Walker.WALKER_DESPAWN_CHANCE;
import static com.sodirea.meleeonly.states.PlayState.Walker.WALKER_SPAWN_CHANCE;

public class PlayState extends State {

    public static final Vector2 MAP_DIM = new Vector2(5000, 5000);      // max dimensions of our game world in px. this means that map.length * UNIT_DIM = MAP_DIM always!!!!
    public static final Vector2 UNIT_DIM = new Vector2(100, 100);         // each unit is 50px x 50px, so our world is composed of 100 x 100 units
    public static final int MIN_NUM_TILES = 400;
    public static final int MAX_NUM_TILES = 700;

    private boolean[][] map;                                                  // if true, then that pair of indices is walkable. false means it is a wall
    private Texture wall;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        // cam.setToOrtho(false, MeleeOnly.WIDTH, MeleeOnly.HEIGHT);
        cam.setToOrtho(false, 5000, 5000);

        map = new boolean[50][50];                                           // each pair of indices represent one unit in our game world
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
        wall = new Texture("wall.png");
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched()) {
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
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                // if this pair of coordinates is false, then it is a wall. render it into our game world
                // but if it is false, it does not necessarily mean it is a wall in the physics world;
                // only walls that are adjacent to floor (i.e. "true" in the map) will have a physics body
                if (!map[i][j]) {
                    //sb.draw(wall, i*UNIT_DIM.x, j*UNIT_DIM.y);
                    // check if there is an adjacent floor (true in our map) to this wall (false in our map)
                    if (i > 0 && map[i-1][j]
                        || i < map.length-1 && map[i+1][j]
                        || j > 0 && map[i][j-1]
                        || j < map[i].length-1 && map[i][j+1]) {
                        sb.draw(wall, i*UNIT_DIM.x, j*UNIT_DIM.y); // if there is, then add a physics body to our world for this wall
                    }
                }
            }
        }
        sb.end();
    }

    @Override
    public void dispose() {
        wall.dispose();
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
        void walk() {
            if (pos.x + dir.x < 0) { // check if the walker will walk out of bounds.
                dir.x = 0;                     // note that if it does, we don't want to simply reverse the direction (since it would be meaningless to re-walk the same path).
                dir.y = -1;                    // so make it walk perpendicular
                walk();                        // re-walk the Walker with the new direction
            } else if (pos.x + dir.x > map.length-1) {
                dir.x = 0;
                dir.y = 1;
                walk();
            } else if (pos.y + dir.y < 0) {
                dir.y = 0;
                dir.x = 1;
                walk();
            } else if (pos.y + dir.y > map[(int) pos.x].length-1) {
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
        Vector2 initialCoord = new Vector2(rng.nextInt(map.length), rng.nextInt(map.length));
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
                        Vector2 direction = generateNewDirection(); // gerenate a direction for the new walker
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
            // TODO: make a new physics body for all walls that are adjacent to floor
        }
    }
}
