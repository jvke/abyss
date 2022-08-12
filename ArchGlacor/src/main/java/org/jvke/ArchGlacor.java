package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

// @TODO: looting is broken
// @TODO: teleporting to wars retreat needs debounced
// @TODO: widget interaction for starting instance doesn't work
public class ArchGlacor extends Plugin {
    static int BANK_ID = 114750;
    static int ALTAR_OF_WAR = 114748;
    static int PORTAL_OF_WAR = 121370;
    static int AQUEDUCT_PORTAL = 121338;
    static int LOBBY_PORTAL = 121341;
    static int ARCH_GLACOR = 28241;

    boolean killFinished = false;
    int instanceAttemptCount = 0;

    public enum PrayerType {
        PROTECT_MAGIC(37, 11, 64),
        PROTECT_RANGED(40, 12, 128),
        PROTECT_MELEE(43, 13, 256),
        RETRIBUTION(46, 14, 512),
        SMITE(52, 16, 2048),
        REDEMPTION(49, 15, 1024),
        MYSTIC_MIGHT(45, 6, 8192),
        RAPID_RESTORE(19, 7, 8),
        PROTECT_FROM_SUMMONING(35, 10, 65536),
        RAPID_HEAL(22, 8, 16),
        PROTECT_ITEM(25, 9, 32),
        OVERCHARGE(45, 5, 32768),
        EAGLE_EYE(44, 4, 4096),
        OVERPOWERING_FORCE(44, 3, 16384),
        INCREDIBLE_REFLEXES(34, 2, 4),
        ULTIMATE_STRENGTH(31, 1, 2),
        STEEL_SKIN(28, 0, 1);

        private int level;
        private int index;
        private int convar;
        PrayerType(int level, int index, int convar) {
            this.level = level;
            this.index = index;
            this.convar = convar;
        }

        public int getRequiredLevel() {
            return level;
        }
        public int getIndex() { return index; }
        public int getConvar() { return convar; }
    }

    private enum States {
        LOOT,
        FIGHT,
        TELEPORT_TO_BANK,
        PRAY_AT_ALTAR,
        BANK,
        TELEPORT_TO_GLACOR,
        ENTER_INSTANCE,
        IDLE
    }

    public static int getPrayerPoints() {
        WidgetGroup group = Widgets.getGroupById(1430);
        if (group != null) {
            Widget container1 = group.getWidget(0);
            if (container1 != null) {
                Widget container2 = container1.getChild(1);
                if (container2 != null) {
                    Widget container3 = container2.getChild(1);
                    if (container3 != null) {
                        Widget container4 = container3.getChild(1);
                        if (container4 != null) {
                            Widget text = container4.getChild(8);
                            if (text != null) {
                                String textString = text.getText();
                                if (textString != null) {
                                    String[] split = textString.split("/");
                                    if (split.length > 0) {
                                        return Integer.parseInt(split[0]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return 0;
    }

    public static int getMaxPrayerPoints() {
        WidgetGroup group = Widgets.getGroupById(1430);
        if (group != null) {
            Widget container1 = group.getWidget(0);
            if (container1 != null) {
                Widget container2 = container1.getChild(1);
                if (container2 != null) {
                    Widget container3 = container2.getChild(1);
                    if (container3 != null) {
                        Widget container4 = container3.getChild(1);
                        if (container4 != null) {
                            Widget text = container4.getChild(8);
                            if (text != null) {
                                String textString = text.getText();
                                if (textString != null) {
                                    String[] split = textString.split("/");
                                    if (split.length > 1) {
                                        return Integer.parseInt(split[1]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return 0;
    }

    public static void togglePrayer(PrayerType prayerType) {
        WidgetGroup prayerMenu = Widgets.getGroupById(1458);
        if (prayerMenu != null) {
            Widget container1 = prayerMenu.getWidget(0);
            if (container1 != null) {
                Widget container2 = container1.getChild(1);
                if (container2 != null) {
                    Widget container3 = container2.getChild(0);
                    if (container3 != null) {
                        Widget container4 = container3.getChild(2);
                        if (container4 != null) {
                            int currentPrayerLevel = Client.getStatById(Client.PRAYER).getCurrent();
                            if (currentPrayerLevel >= prayerType.getRequiredLevel()) {
                                Widget prayer = container4.getChild(prayerType.getIndex());
                                prayer.interact(prayerType.getIndex());
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isPrayerGreaterThanPercent(int percent) {
        double decimal = (double) percent / 100D;
        int max = (int) (getMaxPrayerPoints() * decimal);
        int current = getPrayerPoints();

        if (current <= max) {
            return false;
        }

        return true;
    }

    //checks the convar for the current active prayer.
    public static boolean isPrayerActive(PrayerType prayerType) {
        ConVar prayer = Client.getConVarById(3272);
        if (prayer.getValueInt() == prayerType.getConvar()) {
            return true;
        }

        return false;
    }

    private boolean isInGlacorArea() {
        SceneObject lobbyPortal = SceneObjects.closest(obj -> obj.getId() == LOBBY_PORTAL);
        Player self = Players.self();

        return self.getGlobalPosition().getX() >= 2000 && lobbyPortal != null && !lobbyPortal.hidden();
    }

    private boolean shouldLoot() {
        GroundItem nearest = GroundItems.closest(item -> item != null);

        return !killFinished && isInGlacorArea() && nearest != null;
    }

    private boolean shouldFight() {
        return isInGlacorArea() && !killFinished && !shouldLoot();
    }

    private boolean shouldTeleportToBank() {
        Npc n = Npcs.closest(npc -> npc.getId() == ARCH_GLACOR);

        return (killFinished && isInGlacorArea()) || (isInGlacorArea() && n == null && !isPrayerGreaterThanPercent(70));
    }

    private boolean shouldPrayAtAltar() {
        SceneObject altar = SceneObjects.closest(obj -> obj.getId() == ALTAR_OF_WAR);

        return altar != null && !isPrayerGreaterThanPercent(90);
    }

    private boolean shouldBank() {
        SceneObject bank = SceneObjects.closest(obj -> obj.getId() == BANK_ID);

        return bank != null && !Inventory.isEmpty();
    }

    private boolean shouldTeleportToGlacor() {
        SceneObject portal = SceneObjects.closest(obj -> obj.getId() == PORTAL_OF_WAR);

        return portal != null && !shouldBank() && !shouldPrayAtAltar();
    }

    private boolean shouldEnterInstance() {
        SceneObject portal = SceneObjects.closest(obj -> obj.getId() == AQUEDUCT_PORTAL);

        return !isInGlacorArea() && portal != null && !portal.hidden();
    }

    //    LOOT,
    //    FIGHT,
    //    TELEPORT_TO_BANK,
    //    PRAY_AT_ALTAR,
    //    BANK,
    //    TELEPORT_TO_GLACOR,
    //    ENTER_INSTANCE,
    //    IDLE
    public States getState() {
        if (shouldLoot()) {
            return States.LOOT;
        }

        if (shouldTeleportToBank()) {
            return States.TELEPORT_TO_BANK;
        }

        if (shouldFight()) {
            return States.FIGHT;
        }

        if (shouldPrayAtAltar()) {
            return States.PRAY_AT_ALTAR;
        }

        if (shouldBank()) {
            return States.BANK;
        }

        if (shouldTeleportToGlacor()) {
            return States.TELEPORT_TO_GLACOR;
        }

        if (shouldEnterInstance()) {
            return States.ENTER_INSTANCE;
        }

        return States.IDLE;
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Arch-Glacor");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        if (Bank.isOpen() && !Inventory.isFull()) {
            bank();
        }

        States s = getState();
        Debug.log(s.toString());

        //    LOOT,
        //    FIGHT,
        //    TELEPORT_TO_BANK,
        //    PRAY_AT_ALTAR,
        //    BANK,
        //    TELEPORT_TO_GLACOR,
        //    ENTER_INSTANCE,
        //    IDLE

        switch (s) {
            case ENTER_INSTANCE:
                try {
                    enterInstance();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case LOOT:
                loot();
                break;
            case FIGHT:
                fight();
                break;
            case TELEPORT_TO_BANK:
                teleToBank();
                break;
            case PRAY_AT_ALTAR:
                prayAtAltar();
                break;
            case BANK:
                bank();
                break;
            case TELEPORT_TO_GLACOR:
                teleportToGlacor();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Arch-Glacoring!");
    }

    private void bank() {
        SceneObject bank = SceneObjects.closest(obj -> obj.getId() == BANK_ID);

        if (bank != null && !Bank.isOpen()) {
            bank.interact("Use");
        }

        if (Bank.isOpen()) {
            Input.key(0x31);
        }
    }

    private void loot() {
        if (GroundItems.count() > 0) {
            Debug.log("More than 0 ground items found");

            GroundItem item = GroundItems.closest(i -> i.getId() > 0);

            if (item != null) {
                if (!Players.self().isMoving()) {
                    Vector3i location = new Vector3i(item.getGlobalPosition().getX(),
                            item.getGlobalPosition().getY(), item.getGlobalPosition().getZ());
                    Move.to(location);
                }

                if (!Loot.isOpen()) {
                    Input.key(0x4C);
                }
            }
        } else {
            Debug.log("No ground items found");
        }

        if (Loot.isOpen()) {
            Loot.takeAll();
        }

        if (GroundItems.count() == 0) {
            killFinished = true;
            teleToBank();
        }
    }

    private void fight() {
        instanceAttemptCount = 0;
        if (!Players.self().isUnderAttack()) {
            Npc n = Npcs.closest(npc -> npc.getId() == ARCH_GLACOR);
            if (n != null && n.interact("Attack")) {
                Debug.log("Clicked Fight on glacor");
            } else if (n == null) {
                Input.key(0x33);
            }
        }
    }

    private void teleToBank() {
        if (isInGlacorArea() && !Players.self().isAnimationPlaying()) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 109445294, 1632436225);
        }
    }

    private void prayAtAltar() {
        // reset kill state here
        killFinished = false;

        SceneObject altar = SceneObjects.closest(obj -> obj.getId() == ALTAR_OF_WAR);

        if (altar != null) {
            altar.interact("Pray");
        }
    }

    private void teleportToGlacor() {
        SceneObject portal = SceneObjects.closest(obj -> obj.getId() == PORTAL_OF_WAR);

        if (portal != null) {
            portal.interact("Enter");
        }
    }

    private void enterInstance() throws InterruptedException {
        SceneObject aqueduct = SceneObjects.closest(obj -> obj.getId() == AQUEDUCT_PORTAL);
        WidgetGroup instance = Widgets.getGroupById(1591);
        boolean isOpen = false;

        if (instance != null) {
            Widget c1 = instance.getWidget(0);
            if (c1 != null) {
                Widget c2 = c1.getChild(1);
                if (c2 != null) {
                    Widget c3 = c2.getChild(6);
                    if (c3 != null) {
                        Widget c4 = c3.getChild(6);

                        if (c4 != null) {
                            Widget c5 = c4.getChild(0);

                            if (c5 != null && c5.getText().toLowerCase().equals("start")) {
                                isOpen = true;
                            }
                        }
                    }
                }
            }
        }

        if (isOpen) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 104267836, 65537);
            instanceAttemptCount++;
        } else {
            aqueduct.interact("Enter");
        }

        if (instanceAttemptCount > 1) {
            aqueduct.interact("Enter");
        }
    }
}
