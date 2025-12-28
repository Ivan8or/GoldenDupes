package online.umbcraft.libraries.utils;

public enum ConfigPath {

    //      is the autocraft dupe enabled?
    AUTOCRAFT_DO("autocraft-dupe", "enabled"),

    //      should the autocraft dupe act as vanilla as possible
    AUTOCRAFT_VANILLA("autocraft-dupe", "vanilla"),

    //      should the autocraft dupe give more items the more clicks the player has made
    AUTOCRAFT_IPC("autocraft-dupe", "items-by-click"),

    //      what is the multiplier for the items received based on number of clicks
    AUTOCRAFT_MULTIPLIER("autocraft-dupe", "multiplier"),

    //      what is the multiplier for the items received based on number of clicks
    AUTOCRAFT_MAX_ITEMS("autocraft-dupe", "max-items"),


    //      is the donkey dupe enabled?
    DONKEY_DO("donkey-dupe", "enabled"),

    //      should the autocraft dupe work on donkeys/llamas/mules inside boats
    DONKEY_BOATS("donkey-dupe", "boats"),


    //      is the nether portal dupe enabled
    NETHER_DO("nether-portal", "enabled"),

    //      how many ticks of freedom should be given before the items are no longer dupeable
    NETHER_TICKDELAY("nether-portal", "tick-delay"),


    //      is the anvil dupe enabled?
    ANVIL_DO("anvil-dupe", "enabled"),

    ANVIL_FULLINV("anvil-dupe", "full-inventory"),

    //      is the piston dupe enabled?
    PISTON_DO("piston-dupe", "enabled"),

    PISTON_TICKDELAY("piston-dupe", "tick-delay"),

    PISTON_NONPLAYER("piston-dupe", "non-players"),



    //      should shulker boxes be duped at all?
    NON_STACK_DO_DUPE("non-stackables", "dupe"),

    //      should totems be duped at all?
    NON_STACK_STACKSIZE("non-stackables", "stack-to"),


    //      stack size for duped shulkers
    SHULKERS_DO_DUPE("shulkers", "dupe"),

    //      stack size for duped totems
    SHULKERS_STACKSIZE("shulkers", "stack-to"),


    //      stack size for all other unstackable items
    TOTEMS_DO_DUPE("totems", "dupe"),

    //      stack size for all other unstackable items
    TOTEMS_STACKSIZE("totems", "stack-to"),

    // custom item dupe rules root path
    DUPE_RULES("dupe-rules");

    private final String path;

    ConfigPath(String root, String name) {
        this.path = root + "." + name;
    }

    ConfigPath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

}
