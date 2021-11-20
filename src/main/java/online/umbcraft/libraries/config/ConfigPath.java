package online.umbcraft.libraries.config;

public enum ConfigPath {

    //      is the autocraft dupe enabled?
    AUTOCRAFT_DO(ConfigRoot.AUTOCRAFT, "enabled", "true"),

    //      time the dupe spends enabled vs disabled
    AUTOCRAFT_ON(ConfigRoot.AUTOCRAFT, "time-on", "-1"),
    AUTOCRAFT_OFF(ConfigRoot.AUTOCRAFT, "time-off", "-1"),

    //      should the autocraft dupe act as vanilla as possible
    AUTOCRAFT_VANILLA(ConfigRoot.AUTOCRAFT, "vanilla", "true"),

    //      should the autocraft dupe give more items the more clicks the player has made
    AUTOCRAFT_IPC(ConfigRoot.AUTOCRAFT, "items-by-click", "false"),

    //      what is the multiplier for the items received based on number of clicks
    AUTOCRAFT_MULTIPLIER(ConfigRoot.AUTOCRAFT, "multiplier", "2"),

    //      what is the multiplier for the items received based on number of clicks
    AUTOCRAFT_MAX_ITEMS(ConfigRoot.AUTOCRAFT, "max-items", "64"),



    //      is the donkey dupe enabled?
    DONKEY_DO(ConfigRoot.DONKEY, "enabled", "true"),

    //      time the dupe spends enabled vs disabled
    DONKEY_ON(ConfigRoot.DONKEY, "time-on", "-1"),
    DONKEY_OFF(ConfigRoot.DONKEY, "time-off", "-1"),

    //      should the autocraft dupe work on donkeys/llamas/mules inside boats
    DONKEY_BOATS(ConfigRoot.DONKEY, "boats", "true"),



    //      is the nether portal dupe enabled
    NETHER_DO(ConfigRoot.NETHER_PORTAL, "enabled", "true"),

    //      time the dupe spends enabled vs disabled
    NETHER_ON(ConfigRoot.NETHER_PORTAL, "time-on", "-1"),
    NETHER_OFF(ConfigRoot.NETHER_PORTAL, "time-off", "-1"),

    //      how many ticks of freedom should be given before the items are no longer dupeable
    NETHER_TICKDELAY(ConfigRoot.NETHER_PORTAL, "tick-delay", "3"),



    //      is the donkey dupe enabled?
    ANVIL_DO(ConfigRoot.ANVIL, "enabled", "true"),

    //      time the dupe spends enabled vs disabled
    ANVIL_ON(ConfigRoot.ANVIL, "time-on", "-1"),
    ANVIL_OFF(ConfigRoot.ANVIL, "time-off", "-1"),

    //      should shulker boxes be duped at all?
    NON_STACK_DO_DUPE(ConfigRoot.NON_STACK, "dupe", "true"),

    //      should totems be duped at all?
    NON_STACK_STACKSIZE(ConfigRoot.NON_STACK, "stack-to", "64"),


    //      stack size for duped shulkers
    SHULKERS_DO_DUPE(ConfigRoot.SHULKERS, "dupe", "true"),

    //      stack size for duped totems
    SHULKERS_STACKSIZE(ConfigRoot.SHULKERS, "stack-to", "1"),



    //      stack size for all other unstackable items
    TOTEMS_DO_DUPE(ConfigRoot.TOTEMS, "dupe", "true"),

    //      stack size for all other unstackable items
    TOTEMS_STACKSIZE(ConfigRoot.TOTEMS, "stack-to", "5"),


    //      text to be shown in the placeholder 'goldendupe_{dupe}_status' when dupe is enabled
    PLACEHOLDER_ENABLED_TEXT(ConfigRoot.PLACEHOLDERS, "enabled-text","Enabled"),

    //      text to be shown in the placeholder 'goldendupe_{dupe}_status' when dupe is disabled
    PLACEHOLDER_DISABLED_TEXT(ConfigRoot.PLACEHOLDERS, "disabled-text","Disabled");

    private final String path;
    private final String value;

    ConfigPath(ConfigRoot root, String name, String value) {
        this.path = root.path() + "." + name;
        this.value = value;
    }

    public String value() {
        return value;
    }

    public String path() {
        return path;
    }

}
