package online.umbcraft.libraries.schedule;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/*
Tracks the status of a dupe (enabled, disabled)
uses a file for persistent tracking that lasts between restarts.
file format is:

"
on@1635013898186
"

where on | off = the state of the dupe at this time
1635013898186 = the (epoch) time at which the dupe was turned on | off (in this case, on)

*/


public class DupeScheduler {

    final private String dupeName;
    final private File timeFile;
    private boolean enabled = true;
    private GoldenDupes plugin;

    //final private long ONE_HOUR = 3600000; // one hour is 3600000 milliseconds
    final private long ONE_HOUR = 5000; // test ONLY, do not keep

    final private long hoursOn;
    final private long hoursOff;


    public DupeScheduler(GoldenDupes plugin, String dupeName, int on, int off) throws IOException {
        this.dupeName = dupeName;
        this.plugin = plugin;

        hoursOn = on * ONE_HOUR;
        hoursOff = off * ONE_HOUR;


        File timeFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "timestamps");
        timeFolder.mkdirs();

        timeFile = new File(timeFolder.getAbsolutePath(), dupeName + ".dupe");

        if(on <= 0 || off <= 0) {
            enabled = true;
            return;
        }

        timeFile.createNewFile();
        StateStamp oldStamp = getSavedTimestamp();

        initializeState(oldStamp.getTime(), oldStamp.getStatus());
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public StateStamp getSavedTimestamp() throws IOException {
        Scanner sc = new Scanner(timeFile);
        StateStamp toReturn;

        if (!sc.hasNextLine()) {
            toReturn = new StateStamp(enabled);
            toReturn.record(timeFile);
        } else {
            String stampString = sc.nextLine();
            toReturn = new StateStamp(stampString);
        }
        return toReturn;
    }

    private void flipState() {
        enabled = !enabled;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            flipState();
        }, ((enabled)?hoursOn:hoursOff)/50);
    }

    private void initializeState(long oldEpoch, boolean oldState) {

        long[] hours = {hoursOff, hoursOn};
        int oldStateI = (oldState) ? 1 : 0;

        long epochDiff = System.currentTimeMillis() - oldEpoch;
        long offset = epochDiff % (hoursOn + hoursOff);

        long delay;

        if (offset < hours[oldStateI]) {
            enabled = oldState;
            delay = hours[oldStateI] - offset;
        } else {
            enabled = !oldState;
            delay = (hoursOn + hoursOff) - offset;
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            flipState();
        }, delay/50);
    }
}
