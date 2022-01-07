package online.umbcraft.libraries.schedule;

import online.umbcraft.libraries.GoldenDupes;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);

    final private String dupeName;
    final private File timeFile;
    final private GoldenDupes plugin;

    private boolean enabled = true;
    private long lastFlipTime = -1;

    final private long ONE_HOUR = 3600000; // one hour is 3600000 milliseconds
    //final private long ONE_HOUR = 5000; // test ONLY, do not keep

    final private long milisOn;
    final private long milisOff;


    public DupeScheduler(GoldenDupes plugin, String dupeName, int hoursOn, int hoursOff) throws IOException {
        this.dupeName = dupeName;
        this.plugin = plugin;

        milisOn = hoursOn * ONE_HOUR;
        milisOff = hoursOff * ONE_HOUR;


        File timeFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "timestamps");
        timeFolder.mkdirs();

        timeFile = new File(timeFolder.getAbsolutePath(), dupeName + ".dupe");

        if(hoursOn <= 0 || hoursOff <= 0) {
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

    public String milisToHHMMSS(long milis) {
        Duration timeLeft = Duration.ofMillis(milis);
        String hhmmss = String.format("%02d:%02d:%02d",
                timeLeft.toHours(), timeLeft.toMinutesPart(), timeLeft.toSecondsPart());
        return hhmmss;
    }

    public long getMilisLeft() {
        long timePassed = (System.currentTimeMillis() - lastFlipTime);
        long milisCurState = (enabled)? milisOn : milisOff;
        long timeLeft = milisCurState - timePassed;
        return timeLeft;
    }

    public String getTimeLeft() {
        return milisToHHMMSS(getMilisLeft());
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
        lastFlipTime = System.currentTimeMillis();
        plugin.getLogger().log(Level.SEVERE, dupeName+" dupe is now "+((enabled)?"enabled":"disabled"));
    }

    private void initializeState(long oldEpoch, boolean oldState) {

        long[] hours = {milisOff, milisOn};
        int oldState_int = (oldState) ? 1 : 0;

        long curTime = System.currentTimeMillis();
        long epochDiff = curTime - oldEpoch;
        long offset = epochDiff % (milisOn + milisOff);

        lastFlipTime = curTime - offset;

        long delay;

        if (offset < hours[oldState_int]) {
            enabled = oldState;
            delay = hours[oldState_int] - offset;
        } else {
            enabled = !oldState;
            delay = (milisOn + milisOff) - offset;
            lastFlipTime+=hours[oldState_int];
        }

        // flip state for each type of transition (on->off and off->on)
        // two tasks are needed because time between flips might not be equal for both transitions

        // schedule the event with system time
        executor.scheduleAtFixedRate(this::flipState, delay, (milisOn + milisOff), TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::flipState, (delay + ((enabled)? milisOff : milisOn)), (milisOn + milisOff), TimeUnit.MILLISECONDS);

        // schedule the event with bukkit ticks for time (affected by lag)
        //plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::flipState, delay/50,(milisOn + milisOff)/50);
        //plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::flipState, (delay + ((enabled)? milisOff : milisOn))/50,(milisOn + milisOff)/50);


        // for debugging::::
//        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
//            plugin.getLogger().log(Level.SEVERE,dupeName+" status: "+((enabled)?"enabled":"disabled")+"; time left: "+getTimeLeft());
//        }, 0,10);

    }
}
