package utils;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author nick
 */
public class RunTimeManager {
    private static final Stopwatch stopwatch = Stopwatch.createUnstarted();
    
    public static void start(){
        stopwatch.start();
    }
    
    public static void stop(){
        stopwatch.stop();
    }
    
    public static long getElapsedTime(){
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }
}
