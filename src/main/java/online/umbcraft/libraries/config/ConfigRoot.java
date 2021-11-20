package online.umbcraft.libraries.config;

public enum ConfigRoot {

    //  1.12 autocraft dupe root path
    AUTOCRAFT("autocraft-dupe"),

    //  1.15 donkey dupe root path
    DONKEY("donkey-dupe"),

    // 1.8 nether portal dupe root path
    NETHER_PORTAL("nether-portal"),

    // 1.17 anvil dupe root path
    ANVIL("anvil-dupe"),


    //  item limits root path
    NON_STACK("non-stackables"),

    //  shulker settings root path
    SHULKERS("shulkers"),

    //  totem settings root path
    TOTEMS("totems"),

    // placeholder settings root path
    PLACEHOLDERS("placeholders");

    private final String path;

    ConfigRoot(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
