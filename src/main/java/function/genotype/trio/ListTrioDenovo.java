package function.genotype.trio;

import function.annotation.base.GeneManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.FormatManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class ListTrioDenovo extends AnalysisBase4CalledVar {

    BufferedWriter bwDenovo = null;
    BufferedWriter bwDenovoNoFlag = null;
    final String denovoFilePath = CommonCommand.outputPath + "denovoandhom.csv";
    final String denovoNoFlagFilePath = CommonCommand.outputPath + "denovoandhom_noflag.csv";

    @Override
    public void initOutput() {
        try {
            bwDenovo = new BufferedWriter(new FileWriter(denovoFilePath));
            bwDenovo.write(TrioManager.getTitle4Denovo());
            bwDenovo.newLine();

            if (TrioCommand.isIncludeNoFlag) {
                bwDenovoNoFlag = new BufferedWriter(new FileWriter(denovoNoFlagFilePath));
                bwDenovoNoFlag.write(TrioManager.getTitle4Denovo());
                bwDenovoNoFlag.newLine();
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
            bwDenovo.flush();
            bwDenovo.close();

            if (TrioCommand.isIncludeNoFlag) {
                bwDenovoNoFlag.flush();
                bwDenovoNoFlag.close();
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (TrioCommand.isRunTier) {
            ThirdPartyToolManager.runTrioDenovoTier(denovoFilePath);
        }
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

                    int geno = output.getCalledVariant().getGenotype(trio.getChild().getIndex());

                    if (output.isQualifiedGeno(geno)) {

                        output.initDenovoFlag(trio.getChild());

                        doOutput(output);
                    }
                }

                output.addParentGeno(trio);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(DenovoOutput output) throws Exception {
        if (!output.denovoFlag.equals("no flag") && !output.denovoFlag.equals("unknown")) {
            doOutput(bwDenovo, output);
        } else if (TrioCommand.isIncludeNoFlag) {
            doOutput(bwDenovoNoFlag, output);
        }
    }

    private void doOutput(BufferedWriter bw, DenovoOutput output) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(output.child.getFamilyId()).append(",");
        sb.append(output.child.getName()).append(",");
        sb.append(output.motherName).append(",");
        sb.append(output.fatherName).append(",");
        sb.append("'").append(output.getCalledVariant().getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output.getCalledVariant().getGeneName()))).append(",");
        sb.append(output.denovoFlag).append(",");
        sb.append(output.toString());
        bw.write(sb.toString());
        bw.newLine();
    }

    @Override
    public String toString() {
        return "It is running a list trio denovo function...";
    }
}
