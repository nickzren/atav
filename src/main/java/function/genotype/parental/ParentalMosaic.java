package function.genotype.parental;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import utils.CommandValue;
import utils.ErrorManager;
import utils.FormatManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author nick
 */
public class ParentalMosaic extends AnalysisBase4CalledVar {

    BufferedWriter bwOutput = null;

    final String outputFilePath = CommandValue.outputPath + "parental.mosaic.csv";

    @Override
    public void initOutput() {
        try {
            bwOutput = new BufferedWriter(new FileWriter(outputFilePath));
            bwOutput.write(ParentalOutput.title);
            bwOutput.newLine();
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
            bwOutput.flush();
            bwOutput.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        FamilyManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            ParentalOutput output = new ParentalOutput(calledVar);
            output.countSampleGenoCov();
            output.calculate();

            if (output.isValid()) {

                for (Family family : FamilyManager.getList()) {

                    for (Sample child : family.getChildList()) {

                        int childGeno = calledVar.getGenotype(child.getIndex());

                        if (output.isQualifiedGeno(childGeno)
                                && isChildValid(childGeno, child, calledVar)) {

                            for (Sample parent : family.getParentList()) {
                                
                                int parentGeno = calledVar.getGenotype(parent.getIndex());

                                if (output.isQualifiedGeno(parentGeno)) {
                                    doOutput(output.getString(child, childGeno, parent, parentGeno));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private boolean isChildValid(int childGeno, Sample child, CalledVariant calledVar) {
        // --proband-qd
        float probandQD = calledVar.getQualByDepthQD(child.getId());

        if (!isProbandQdValid(probandQD)) {
            return false;
        }

        // --proband-het-percent-alt-read
        if (childGeno == 1) {
            int readsAlt = calledVar.getReadsAlt(child.getId());
            int gatkFilteredCoverage = calledVar.getGatkFilteredCoverage(child.getId());

            double percAltRead = FormatManager.devide(readsAlt, gatkFilteredCoverage);

            if (!isProbandHetPercentAltReadValid(percAltRead)) {
                return false;
            }
        }

        // --proband-binomial
        float binomial = Data.NA; // need to calculate real value here

        if (!isProbandBinomialValid(binomial)) {
            return false;
        }

        return true;
    }

    private boolean isProbandQdValid(float value) {
        if (CommandValue.probandQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.probandQD) {
                return true;
            }
        }

        return false;
    }

    private boolean isProbandHetPercentAltReadValid(double value) {
        if (CommandValue.probandHetPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= CommandValue.probandHetPercentAltRead[0]
                    && value <= CommandValue.probandHetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    private boolean isProbandBinomialValid(float value) {
        if (CommandValue.probandBinomial == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommandValue.probandBinomial) {
            return true;
        }

        return false;
    }

    private void doOutput(String str) throws IOException {
        bwOutput.write(str);
        bwOutput.newLine();
    }

    @Override
    public String toString() {
        return "It is running a parental mosaic function...";
    }
}
