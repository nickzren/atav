package function.nondb.ppi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommandValue;
import utils.ErrorManager;

/**
 *
 * @author quanli
 */
public class PPI {

    BufferedWriter bwPPI = null;
    final String ppiFilePath = CommandValue.outputPath + "ppi.csv";
    final String ppiSystemFile = "update_to_real_path";

    public void run() {
        try {
            initOutput();
            
            // function code ...
            
            bwPPI.write("column1,column2");
            bwPPI.newLine();

            closeOutput();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public void initOutput() {
        try {
            bwPPI = new BufferedWriter(new FileWriter(ppiFilePath));
            bwPPI.write(Output.title);
            bwPPI.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public void closeOutput() {
        try {
            bwPPI.flush();
            bwPPI.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public String toString() {
        return "It is running PPI function...";
    }
}
