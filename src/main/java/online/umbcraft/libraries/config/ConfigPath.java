package online.umbcraft.libraries.config;

public enum ConfigPath {

//  1.12 autocraft dupe root path
    AUTOCRAFT("autocraft-dupe"),

//      is the autocraft dupe enabled?
        AUTOCRAFT_DO(AUTOCRAFT.path() + ".enabled"),

//      should the autocraft dupe act as vanilla as possible
        AUTOCRAFT_VANILLA(AUTOCRAFT.path() + ".vanilla"),

//      should the autocraft dupe give more items the more clicks the player has made
        AUTOCRAFT_IPC(AUTOCRAFT.path() + ".items-by-click"),

//      what is the multiplier for the items received based on number of clicks
        AUTOCRAFT_MULTIPLIER(AUTOCRAFT.path() + ".multiplier"),

//      what is the multiplier for the items received based on number of clicks
    AUTOCRAFT_MAX_ITEMS(AUTOCRAFT.path() + ".max-items"),



    //  1.15 donkey dupe root path
DONKEY("autocraft-dupe"),

    //      is the donkey dupe enabled?
    DONKEY_DO(DONKEY.path() + ".enabled"),

    //      should the autocraft dupe work on donkeys/llamas/mules inside boats
    DONKEY_BOATS(DONKEY.path() + ".boats"),



//  item limits root path
    NON_STACK("non-stackables"),

//      should shulker boxes be duped at all?
        NON_STACK_DO_DUPE(NON_STACK.path() + ".dupe"),

//      should totems be duped at all?
        NON_STACK_STACKSIZE(NON_STACK.path() + ".stack-to"),


//  shulker settings root path
    SHULKERS("shulkers"),

//      stack size for duped shulkers
        SHULKERS_DO_DUPE(SHULKERS.path() + ".dupe"),

//      stack size for duped totems
        SHULKERS_STACKSIZE(SHULKERS.path() + ".stack-to"),


//  totem settings root path
    TOTEMS("totems"),

//      stack size for all other unstackable items
        TOTEMS_DO_DUPE(TOTEMS.path() + ".dupe"),

//      stack size for all other unstackable items
        TOTEMS_STACKSIZE(TOTEMS.path() + ".stack-to");


    private final String path;

    ConfigPath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

}
