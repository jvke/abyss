package org.jvke;

import javafx.scene.Scene;
import kraken.plugin.api.*;

import java.util.Arrays;

import static kraken.plugin.api.Rng.i32;

public class CroesusLeech extends Plugin {
    // boss
    private int CROESUS = 28392;

    // cores and mid
    private int FISH_CORE = 28401;
    private int MINE_CORE = -1;
    private int HUNT_CORE = -1;
    private int WC_CORE = 121696;
    private int MID = 28409;

    // attacks
    private int RED_BOMB = 7560;
    private int BLUE_BOMB = 7566;
    private int YELLOW_BOMB = 7562;
    private int GREEN_BOMB = 7564;
    private int STICKY_FUNGUS = 121740;
    private int FAIRY_RING = 121739;
    private int STUNNED = -1;

    // valid tiles
    private int STORAGE_SPOT = 121467;
    private Vector3i reference = null;
    private Vector3i[] validTiles = {};

    enum State {
        MOVE,
        UNSTUCK,
        SIP_RESTORE,
        MID,
        CORE,
        RESET,
        IDLE
    }

    public State getState() {
        if (shouldUnstuck()) {
            return State.UNSTUCK;
        }

        if (shouldMid()) {
            return State.MID;
        }

        if (shouldMove()) {
            return State.MOVE;
        }

        if (shouldSipRestore()) {
            return State.SIP_RESTORE;
        }

        if (shouldCore()) {
            return State.CORE;
        }

        if (shouldReset()) {
            return State.RESET;
        }

        return State.IDLE;
    }

    private boolean shouldMove() {
        Vector3i current = Players.self().getGlobalPosition();
        Effect blue = Effects.closest(eff -> eff.getId() == BLUE_BOMB);
        Effect red = Effects.closest(eff -> eff.getId() == RED_BOMB);
        Effect yellow = Effects.closest(eff -> eff.getId() == YELLOW_BOMB);
        Effect green = Effects.closest(eff -> eff.getId() == GREEN_BOMB);
        SceneObject fairy = SceneObjects.closest(obj -> obj.getId() == FAIRY_RING);

        if (fairy != null && fairy.getGlobalPosition().distance(current) <= 3) {
            return true;
        }

        if (blue != null && blue.getGlobalPosition().distance(current) <= 3) {
            return true;
        }

        if (red != null && red.getGlobalPosition().distance(current) <= 3) {
            return true;
        }

        if (yellow != null && yellow.getGlobalPosition().distance(current) <= 3) {
            return true;
        }

        if (green != null && green.getGlobalPosition().distance(current) <= 3) {
            return true;
        }

        return false;
    }

    private boolean shouldUnstuck() {
        Vector3i current = Players.self().getGlobalPosition();
        SceneObject stickyFungus = SceneObjects.closest(obj -> obj.getId() == STICKY_FUNGUS);

        if (stickyFungus != null && stickyFungus.getGlobalPosition().distance(current) == 0) {
            return true;
        }

        return false;
    }

    private boolean shouldSipRestore() {
        return false;
    }

    private boolean shouldMid() {
        SceneObject mid = SceneObjects.closest(obj -> obj.getId() == MID);

        return mid != null;
    }

    private boolean shouldCore() {
        SceneObject core = SceneObjects.closest(obj -> obj.getId() == FISH_CORE || obj.getId() == MINE_CORE || obj.getId() == HUNT_CORE || obj.getId() == WC_CORE);

        return core != null;
    }

    private boolean shouldReset() {
        return false;
    }

    private void move() {
        Vector3i[] sortedTiles = sortTilesByNearest(validTiles);
        Effect[] effects = Effects.all(eff -> eff.getId() == BLUE_BOMB || eff.getId() == RED_BOMB || eff.getId() == YELLOW_BOMB || eff.getId() == GREEN_BOMB);
        SceneObject fairyRing = SceneObjects.closest(obj -> obj.getId() == FAIRY_RING);
        // find tile that is not within 3 tiles of an effect or fairy ring
        for (Vector3i tile : sortedTiles) {
            boolean valid = true;
            for (Effect effect : effects) {
                if (effect.getGlobalPosition().distance(tile) <= 3) {
                    valid = false;
                    break;
                }
            }
            if (fairyRing != null && fairyRing.getGlobalPosition().distance(tile) <= 3) {
                valid = false;
            }

            if (valid) {
                Move.to(tile);
                return;
            }
        }
    }

    private void sipRestore() {
        Actions.menu(Actions.MENU_EXECUTE_WIDGET, 8, -1, 93716544, 1);
    }

    private void reset() {
        Move.to(new Vector3i(reference.getY(), reference.getX() - 2, reference.getZ()));
    }

    private void mid() {
        SceneObject mid = SceneObjects.closest(obj -> obj.getId() == MID);
        if (mid != null && !Players.self().isAnimationPlaying()) {
            Actions.menu(Actions.MENU_EXECUTE_OBJECT1, mid.getId(), mid.getGlobalPosition().getX(), mid.getGlobalPosition().getY(), -1);
        }
    }

    private void core() {
        SceneObject core = SceneObjects.closest(obj -> obj.getId() == FISH_CORE || obj.getId() == MINE_CORE || obj.getId() == HUNT_CORE || obj.getId() == WC_CORE);
        if (core != null && !Players.self().isAnimationPlaying()) {
            Actions.menu(Actions.MENU_EXECUTE_OBJECT1, core.getId(), core.getGlobalPosition().getX(), core.getGlobalPosition().getY(), -1);
        }
    }

    private void unstuck() {
        SceneObject stickyFungus = SceneObjects.closest(obj -> obj.getId() == STICKY_FUNGUS);
        if (stickyFungus != null && !Players.self().isAnimationPlaying()) {
            Actions.menu(Actions.MENU_EXECUTE_OBJECT1, stickyFungus.getId(), stickyFungus.getGlobalPosition().getX(), stickyFungus.getGlobalPosition().getY(), -1);
        }
    }

    private Vector3i[] sortTilesByNearest(Vector3i[] tiles) {
        Vector3i current = Players.self().getGlobalPosition();
        Vector3i[] sorted = new Vector3i[tiles.length];

        for (int i = 0; i < tiles.length; i++) {
            int min = Integer.MAX_VALUE;
            int index = 0;
            for (int j = 0; j < tiles.length; j++) {
                int dist = current.distance(tiles[j]);
                if (dist < min) {
                    min = dist;
                    index = j;
                }
            }
            sorted[i] = tiles[index];
            tiles[index] = null;
        }

        return sorted;
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("CroesusLeech");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        SceneObject storage = SceneObjects.closest(o -> o.getId() == STORAGE_SPOT);

        if (storage != null) {
            if (reference == null || storage.getGlobalPosition().distance(reference) > 0) {
                reference = storage.getGlobalPosition();
                int[][] f = {{ 0, -2 }, { 0, -3 }, { 0, -4 }, { -1, -4 }, { -1, -5 }, { -2, -5 }, { -3, -5 }, { -3, -6 }, { -2, -6 }, { -2, -7 }, { -3, -7 }, { -3, -8 }, { -3, -9 }, { -3, -10 }, { -3, -11 }, { -3, -12 }, { -3, -13 }, { -3, -14 }, { -3, -15 }, { -2, -13 }, { -2, -12 }, { -2, -11 }, { -2, -10 }, { -2, -9 }, { -2, -8 }, { -2, -7 }, { -2, -6 }, { -2, -5 }, { -2, -4 }, { -1, -13 }, { -1, -12 }, { -1, -11 }, { -1, -10 }, { -1, -9 }, { -1, -8 }, { -1, -7 }, { 0, -15 }, { 0, -14 }, { 0, -13 }, { 0, -12 }, { 0, -11 }, { 0, -10 }, { 0, -9 }, { 0, -8 }, { 1, -9 }, { 1, -10 }, { 1, -11 }, { 1, -12 }, { 1, -13 }, { 1, -14 }, { 1, -15 }, { 2, -14 }, { 2, -13 }, { 2, -12 }, { 2, -11 }, { 2, -10 }, { 2, -9 }, { 2, -8 }, { 3, -7 }, { 3, -8 }, { 3, -9 }, { 3, -10 }, { 3, -11 }, { 3, -12 }, { 3, -13 }, { 3, -14 }, { 3, -15 }, { 4, -16 }, { 4, -15 }, { 4, -14 }, { 4, -13 }, { 4, -12 }, { 4, -11 }, { 4, -10 }, { 4, -9 }, { 4, -8 }, { 4, -7 }, { 4, -6 }, { 4, -5 }, { 4, -4 }, { 4, -3 }, { 4, -2 }, { 5, -2 }, { 5, -3 }, { 5, -4 }, { 5, -5 }, { 5, -6 }, { 5, -7 }, { 5, -8 }, { 5, -9 }, { 5, -10 }, { 5, -11 }, { 5, -12 }, { 5, -13 }, { 5, -14 }, { 5, -15 }, { 5, -16 }, { 6, -15 }, { 6, -14 }, { 6, -13 }, { 6, -12 }, { 6, -11 }, { 6, -10 }, { 6, -9 }, { 6, -8 }, { 6, -7 }, { 6, -6 }, { 6, -5 }, { 6, -4 }, { 6, -3 }, { 6, -2 }, { 7, -2 }, { 7, -3 }, { 7, -4 }, { 7, -5 }, { 7, -6 }, { 7, -7 }, { 7, -8 }, { 7, -9 }, { 7, -10 }, { 7, -11 }, { 7, -12 }, { 7, -13 }, { 7, -14 }, { 7, -15 }, { 8, -14 }, { 8, -13 }, { 8, -12 }, { 8, -11 }, { 8, -10 }, { 8, -9 }, { 8, -8 }, { 8, -7 }, { 8, -6 }, { 8, -5 }, { 8, -4 } };

                int baseX = reference.getX();
                int baseY = reference.getY();
                int baseZ = reference.getZ();
                validTiles = new Vector3i[f.length];

                for (int i = 0; i < f.length; i++) {
                    validTiles[i] = new Vector3i(baseX + f[i][0], baseY + f[i][1], baseZ);
                }
            }
        }

        State s = getState();
        Debug.log(s.toString());

        switch (s) {
            case SIP_RESTORE:
                sipRestore();
                break;
            case MOVE:
                move();
                break;
            case UNSTUCK:
                unstuck();
                break;
            case MID:
                mid();
                break;
            case CORE:
                core();
                break;
            case RESET:
                reset();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(600, 6000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Croesus!");
    }
}
