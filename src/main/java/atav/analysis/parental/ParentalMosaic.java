package atav.analysis.parental;

import atav.analysis.base.AnalysisBase4CalledVar;
import atav.analysis.base.CalledVariant;
import atav.analysis.base.Sample;
import atav.global.Data;
import atav.manager.utils.CommandValue;
import atav.manager.utils.ErrorManager;
import atav.manager.utils.FormatManager;
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

                        if (output.isQualifiedGeno(childGeno) &&
                                isChildValid(childGeno, child, calledVar)) {
                            int fatherGeno = calledVar.getGenotype(family.getFather().getIndex());

                            if (output.isQualifiedGeno(fatherGeno)) {
                                doOutput(output.getString(child, childGeno, family.getFather(), fatherGeno));
                            }

                            int motherGeno = calledVar.getGenotype(family.getMother().getIndex());

                            if (output.isQualifiedGeno(motherGeno)) {
                                doOutput(output.getString(child, childGeno, family.getMother(), motherGeno));
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
