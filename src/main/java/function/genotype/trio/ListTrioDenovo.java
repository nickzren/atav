package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListTrioDenovo extends AnalysisBase4CalledVar {

    BufferedWriter bwDetails = null;
    BufferedWriter bwDetails_noflag = null;
    final String flagFilePath = CommonCommand.outputPath + "denovoandhom.csv";
    final String noFlagFilePath = CommonCommand.outputPath + "denovoandhom_noflag.csv";

    @Override
    public void initOutput() {
        try {
            bwDetails = new BufferedWriter(new FileWriter(flagFilePath));
            bwDetails.write(DenovoOutput.title);
            bwDetails.newLine();

            if (TrioCommand.isIncludeNoflag) {
                bwDetails_noflag = new BufferedWriter(new FileWriter(noFlagFilePath));
                bwDetails_noflag.write(DenovoOutput.title);
                bwDetails_noflag.newLine();
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
        try {
            bwDetails.flush();
            bwDetails.close();
            
            if (TrioCommand.isIncludeNoflag) {
                bwDetails_noflag.flush();
                bwDetails_noflag.close();
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        TrioManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            DenovoOutput output = new DenovoOutput(calledVar);

            output.countSampleGeno();

            for (Trio trio : TrioManager.getList()) {
                output.initTrioFamilyData(trio);

                output.deleteParentGeno(trio);

                output.calculate();

                if (output.isValid()) {

                    int geno = output.getCalledVariant().getGenotype(trio.getChildIndex());

                    if (output.isQualifiedGeno(geno)) {

                        output.initFlag(trio.getChildId());

                        output.initAvgCov();

                        output.initGenoZygo(trio.getChildIndex());

                        doOutput(output, trio);
                    }
                }

                output.addParentGeno(trio);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(DenovoOutput output, Trio trio) throws Exception {
        if (!output.flag.equals("no flag") && !output.flag.equals("unknown")) {
            bwDetails.write(output.getString(trio));
            bwDetails.newLine();
        } else if (TrioCommand.isIncludeNoflag) {
            bwDetails_noflag.write(output.getString(trio));
            bwDetails_noflag.newLine();
        }
    }

    @Override
    public String toString() {
        return "It is running a list trio denovo function...";
    }
}
