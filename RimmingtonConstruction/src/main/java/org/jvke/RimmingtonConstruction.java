package org.jvke;

import javafx.scene.Scene;
import kraken.plugin.api.*;

import java.util.Arrays;

import static kraken.plugin.api.Rng.i32;

public class RimmingtonConstruction extends Plugin {
    static int[] OAKS = { 38731, 38732 };
    static int PLANK_MAKER = 18560;
    static int ENTRANCE = 15478;
    static int EXIT = 13405;
    static int LARDER_SPACE = 15403;
    static int TABLE_SPACE = 15405;
    static int LARDER = 13566;
    static int TABLE = 13578;

    private enum States {
        CHOP,
        PLANK,
        ENTER,
        EXIT,
        BUILD_LARDER,
        REMOVE_LARDER,
        BUILD_TABLE,
        REMOVE_TABLE,
        IDLE
    }

    private boolean isInPoh() {
        return SceneObjects.closest(o -> o.getId() == EXIT) != null;
    }

    private boolean shouldChop() {
        return !isInPoh() && !Inventory.isFull();
    }

    private boolean shouldPlank() {
        return !isInPoh() && Inventory.isFull() && Inventory.contains(item -> item.getName().equals("Oak logs"));
    }

    private boolean shouldEnter() {
        return !isInPoh() && Inventory.isFull() && !Inventory.contains(item -> item.getName().equals("Oak logs"));
    }

    private boolean shouldExit() {
        return isInPoh() && (Inventory.count(item -> item.getName().equals("Oak plank")) < 3);
    }

    private boolean shouldBuildLarder() {
        SceneObject larderSpace = SceneObjects.closest(o -> o.getId() == LARDER_SPACE);

        return isInPoh() && larderSpace != null && Inventory.count(item -> item.getName().equals("Oak plank")) >= 8;
    }

    private boolean shouldRemoveLarder() {
        SceneObject larder = SceneObjects.closest(o -> o.getId() == LARDER);

        return larder != null;
    }

    private boolean shouldBuildTable() {
        SceneObject tableSpace = SceneObjects.closest(o -> o.getId() == TABLE_SPACE);
        int count = Inventory.count(item -> item.getName().equals("Oak plank"));

        return isInPoh() && tableSpace != null && count >= 3 && count < 8;
    }

    private boolean shouldRemoveTable() {
        SceneObject table = SceneObjects.closest(o -> o.getId() == TABLE);

        return table != null;
    }
    
    public States getState() {
        if (shouldChop()) {
            return States.CHOP;    
        }

        if (shouldPlank()) {
            return States.PLANK;
        }

        if (shouldEnter()) {
            return States.ENTER;
        }

        if (shouldExit()) {
            return States.EXIT;
        }

        if (shouldRemoveLarder()) {
            return States.REMOVE_LARDER;
        }

        if (shouldRemoveTable()) {
            return States.REMOVE_TABLE;
        }

        if (shouldBuildLarder()) {
            return States.BUILD_LARDER;
        }

        if (shouldBuildTable()) {
            return States.BUILD_TABLE;
        }
        
        return States.IDLE;
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("RimmingtonConstruction");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        States s = getState();

        Debug.log(s.toString());

        switch (s) {
            case CHOP:
                chop();
                break;
            case PLANK:
                plank();
                break;  
            case ENTER:
                enter();
                break;
            case REMOVE_LARDER:
                removeLarder();
                break;
            case REMOVE_TABLE:
                removeTable();
                break;
            case BUILD_LARDER:
                buildLarder();
                break;
            case BUILD_TABLE:
                buildTable();
                break;
            case EXIT:
                exit();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("RimmingtonConstruction!");
    }
    
    private void chop() {
        SceneObject oak = SceneObjects.closest(o -> Array.contains(OAKS, o.getId()));

        if (oak != null && !Players.self().isAnimationPlaying()) {
            oak.interact("Chop down");
        }
    }

    private void plank() {
        Npc plankMaker = Npcs.closest(npc -> npc.getId() == PLANK_MAKER);
        int attempts = 0;
        boolean isOpen = false;

        WidgetGroup instance = Widgets.getGroupById(403);

        if (instance != null) {
            Widget c1 = instance.getWidget(0);

            if (c1 != null) {
                Widget c2 = c1.getChild(2);

                if (c2 != null) {
                    Widget c3 = c2.getChild(14);

                    if (c3 != null) {
                        isOpen = c3.getText().toLowerCase().contains("convert");
                    }
                }
            }
        }

        if (isOpen) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 26411079, 1919221761);
            attempts++;
        } else if (plankMaker != null) {
            plankMaker.interact("Buy plank");
        }

        if (attempts > 1) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 26411079, 1919221761);
        }
    }

    private void enter() {
        SceneObject entrance = SceneObjects.closest(o -> o.getId() == ENTRANCE);

        if (entrance != null) {
            entrance.interact("Enter building mode"); 
        }
    }

    private void exit() {
        SceneObject exit = SceneObjects.closest(o -> o.getId() == EXIT);

        if (exit != null) {
            exit.interact("Enter");
        }
    }

    private void buildLarder() {
        SceneObject space = SceneObjects.closest(o -> o.getId() == LARDER_SPACE);
        int attempts = 0;
        boolean isOpen = false;

        WidgetGroup instance = Widgets.getGroupById(1306);

        if (instance != null) {
            Widget c1 = instance.getWidget(0);

            if (c1 != null) {
                Widget c2 = c1.getChild(2);

                if (c2 != null) {
                    Widget c3 = c2.getChild(14);

                    if (c3 != null) {
                        isOpen = c3.getText().toLowerCase().contains("build");
                    }
                }
            }
        }

        if (isOpen) {
//            Actions.menu(Actions.MENU_EXECUTE_SELECTABLE_WIDGET, 0, 4, 85590029, 116981761);
            Input.key(0x32);
            attempts++;
        } else {
            space.interact("Build");
        }

        if (attempts > 1) {
            Input.key(0x32);
        }
    }

    private void removeLarder() {
        SceneObject larder = SceneObjects.closest(o -> o.getId() == LARDER);

        if (larder != null) {
            larder.interact("Remove");
        }
    }

    private void buildTable() {
        SceneObject space = SceneObjects.closest(o -> o.getId() == TABLE_SPACE);

        int attempts = 0;
        boolean isOpen = false;

        WidgetGroup instance = Widgets.getGroupById(1306);

        if (instance != null) {
            Widget c1 = instance.getWidget(0);

            if (c1 != null) {
                Widget c2 = c1.getChild(2);

                if (c2 != null) {
                    Widget c3 = c2.getChild(14);

                    if (c3 != null) {
                        isOpen = c3.getText().toLowerCase().contains("build");
                    }
                }
            }
        }

        if (isOpen) {
//            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 0, 4, 85590029, 33554433);
            Input.key(0x32);
            attempts++;
        } else {
            space.interact("Build");
        }

        if (attempts > 1) {
//            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 0, 4, 85590029, 33554433);
            Input.key(0x32);
        }
    }

    private void removeTable() {
        SceneObject table = SceneObjects.closest(o -> o.getId() == TABLE);

        if (table != null) {
            table.interact("Remove");
        }
    }
}
