package com.sodirea.meleeonly.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Stack;

public class GameStateManager {

    private Stack<State> states;

    // creates a GameStateManager object, which uses a stack to keep track of active states, and display them as required
    public GameStateManager() {
        states = new Stack<State>();
    }

    public void push(State state) {
        states.push(state);
    }

    public void pop() {
        states.pop().dispose();
    }

    public void set(State state) {
        states.pop().dispose();
        states.push(state);
    }

    public State peek() {
        return states.peek();
    }

    // updates the top-most state in our stack
    public void update(float dt) {
        states.peek().update(dt);
    }

    // renders the top-most state in our stack
    public void render(SpriteBatch sb) {
        states.peek().render(sb);
    }

}
