package online.umbcraft.libraries.schedule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StateStamp {

    final private long time;
    final private boolean status;

    public StateStamp(boolean status) {
        time = System.currentTimeMillis();
        this.status = status;
    }

    public StateStamp(long time, boolean status) {
        this.time = time;
        this.status = status;
    }

    public StateStamp(String stamp) {
        String[] stampComponents = stamp.split("@");
        time = Long.parseLong(stampComponents[1]);
        status = stampComponents[0].equals("on");
    }

    public void record(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(((status) ? "on" : "off") + "@" + time);
        fw.close();
    }

    public long getTime() {
        return time;
    }

    public boolean getStatus() {
        return status;
    }
}
