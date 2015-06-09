package function.genotype.base;

import function.variant.base.Region;
import function.variant.base.Variant;
import function.genotype.trio.TrioManager;
import global.Data;
import utils.CommandValue;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class NonCarrier {

    public int sampleId;
    public int genotype;
    public int coverage;

    public void init(int sid, int cov) {
        sampleId = sid;
        coverage = cov;
        if (coverage == Data.NA) {
            genotype = Data.NA;
        } else {
            genotype = 0;
        }
    }

    public void init(ResultSet rs, int posIndex) throws Exception {
        sampleId = rs.getInt("sample_id");
        String min_coverage = rs.getString("min_coverage");
        CoverageBlockManager.put(sampleId, min_coverage);
        coverage = parseCoverage(min_coverage, posIndex);
        if (coverage == Data.NA) {
            genotype = Data.NA;
        } else {
            genotype = 0;
        }
    }

    private int parseCoverage(String allCov, int posIndex) {
        int cov = Data.NA;

        String[] allCovArray = allCov.split(",");

        int endIndex = 0;

        String oneCovBinStr, oneCovBinLength;
        char covBin;

        for (int i = 0; i < allCovArray.length; i++) {
            oneCovBinStr = allCovArray[i];
            oneCovBinLength = oneCovBinStr.substring(0, oneCovBinStr.length() - 1);

            endIndex += Integer.valueOf(oneCovBinLength);

            if (posIndex <= endIndex) {
                covBin = oneCovBinStr.charAt(oneCovBinStr.length() - 1);
                cov = CoverageBlockManager.getCoverageByBin(covBin);
                break;
            }
        }

        return cov;
    }

    public int getSampleId() {
        return sampleId;
    }

    public int getGenotype() {
        return genotype;
    }

    public int getCoverage() {
        return coverage;
    }

    public boolean isMale() {
        return SampleManager.getTable().get(sampleId).isMale();
    }

    public void checkCoverageFilter(int minCaseCov, int minCtrlCov) {
        Sample sample = SampleManager.getTable().get(sampleId);

        if (sample.isCase()) // --min-case-coverage-call or --min-case-coverage-no-call
        {
            if (!QualityManager.isMinCoverageValid(coverage, minCaseCov)) {
                setMissing();
            }
        } else // --min-ctrl-coverage-call or --min-ctrl-coverage-no-call
        {
            if (!QualityManager.isMinCoverageValid(coverage, minCtrlCov)) {
                setMissing();
            }
        }
    }
    
    public void setMissing() {
        if (CommandValue.isTrioDenovo
                && TrioManager.isParent(sampleId)) {
            // do nothing
        } else {
            genotype = Data.NA;
            coverage = Data.NA;
        }
    }

    /*
     * Outside of pseudoautosomal regions:
     *
     * Chrx: females are treated normally
     *
     * Chrx: males have het removed
     *
     * Chry: females are set to missing
     *
     * Chry: males have het removed
     *
     * Inside of pseudoautosomal region which are treated like autosomes.
     */
    public void checkValidOnXY(Variant var) {
        checkValidOnXY(var.getRegion());
    }
    
    public void checkValidOnXY(Region r) {
        if (genotype != Data.NA) {
            boolean isValid = true;
            
            Sample sample = SampleManager.getTable().get(sampleId);
            
            if (sample.isMale()) {
                if (genotype == 1 // male het chr x or y & outside 
                        && !r.isInsideAutosomalOrPseudoautosomalRegions()) {
                    isValid = false;
                }
            } else {
                if (r.getChrNum() == 24 // female chy & outside
                        && !r.isInsideYPseudoautosomalRegions()) {
                    isValid = false;
                }
            }
            
            if (!isValid) {
                genotype = Data.NA;
                coverage = Data.NA;
            }
        }
    }
}
