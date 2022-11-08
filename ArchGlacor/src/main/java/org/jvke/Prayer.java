package org.jvke;

import abyss.plugin.api.variables.VariableManager;
import kraken.plugin.api.*;

public class Prayer extends Plugin {
    static int ARCH_GLACOR = 28241;
    private enum Overheads {
        MAGE,
        RANGE,
        MELEE
    }

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

    public static boolean isPrayerGreaterThanPercent(int percent) {
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
        ConVar prayer = VariableManager.getConVarById(3272);

        if (prayer.getValueInt() == prayerType.getConvar()) {
            return true;
        }

        return false;
    }

    public static Overheads getCorrectOverhead() {
        Npc glacor = Npcs.closest(n -> n.getId() == ARCH_GLACOR);

        if (glacor != null) {
            if (glacor.getAnimationId() == 28242) {
                return Overheads.MELEE;
            } else if (glacor.getAnimationId() == 28243) {
                return Overheads.RANGE;
            } else {
                return Overheads.MAGE;
            }
        }

        return null;
    }

    public static PrayerType getCorrectPrayer() {
        Overheads overhead = getCorrectOverhead();

        if (overhead != null) {
            switch (overhead) {
                case MAGE:
                    return PrayerType.PROTECT_MAGIC;
                case RANGE:
                    return PrayerType.PROTECT_RANGED;
                case MELEE:
                    return PrayerType.PROTECT_MELEE;
            }
        }

        return null;
    }



    public static void toggleCorrectPrayer() {
        PrayerType prayer = getCorrectPrayer();

        if (prayer != null) {
            if (!isPrayerActive(prayer)) {
                togglePrayer(prayer);
            }
        }
    }

    public static boolean shouldToggleCorrectPrayer() {
        PrayerType prayer = getCorrectPrayer();

        if (prayer != null) {
            if (!isPrayerActive(prayer)) {
                return true;
            }
        }

        return false;
    }
}
