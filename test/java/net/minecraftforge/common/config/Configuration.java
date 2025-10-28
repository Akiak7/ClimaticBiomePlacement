package net.minecraftforge.common.config;

public class Configuration {
    public Configuration() {}

    public Configuration(java.io.File file) {}

    public void load() {}

    public void save() {}

    public boolean getBoolean(String name, String category, boolean defaultValue, String comment) {
        return defaultValue;
    }

    public String getString(String name, String category, String defaultValue, String comment) {
        return defaultValue;
    }

    public int getInt(String name, String category, int defaultValue, int minValue, int maxValue, String comment) {
        if(defaultValue < minValue) {
            return minValue;
        }
        if(defaultValue > maxValue) {
            return maxValue;
        }
        return defaultValue;
    }
}
