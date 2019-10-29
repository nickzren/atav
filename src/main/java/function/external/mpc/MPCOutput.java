package function.external.mpc;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class MPCOutput {
    
    float mpc;

    public static String getTitle() {
        return "Variant ID,"
                + MPCManager.getTitle();
    }

    public MPCOutput(String id) throws Exception {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        mpc = MPCManager.getScore(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }
    
    public boolean isValid() {
        return MPCCommand.isMPCValid(mpc);
    }
    
    @Override
    public String toString() {
        return FormatManager.getFloat(mpc);
    }
}
