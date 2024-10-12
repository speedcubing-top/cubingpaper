//Taco - Limit-the-length-of-buffered-bytes-to-read
package net.techcable.tacospigot;

import org.bukkit.Bukkit;

public class CompatHacks {
    private CompatHacks() {}
    public static boolean hasProtocolSupport() {
        return Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");
    }
}