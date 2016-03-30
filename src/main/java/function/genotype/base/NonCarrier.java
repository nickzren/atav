package function.genotype.base;

import function.genotype.trio.TrioCommand;
import function.variant.base.Region;
import function.genotype.trio.TrioManager;
import global.Data;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class NonCarrier {

    public int sampleId;
    public int genotype;
    public int coverage;

    public NonCarrier() {
    }

    public NonCarrier(int sid, int cov) {
        sampleId = sid;
        coverage = cov;
        if (coverage == Data.NA) {
            genotype = Data.NA;
        } else {
            genotype = 0;
        }
    }

    public NonCarrier(ResultSet rs, int posIndex) throws Exception {
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

    private int parseCoverage(String allCov, int pos) {
        int cov = Data.NA;

        String[] allCovBin = allCov.split(",");

        int covBinPos = 0;

        for (int i = 0; i < allCovBin.length; i++) {
            covBinPos += Integer.valueOf(allCovBin[i].substring(0, allCovBin[i].length() - 1));

            if (pos <= covBinPos) {
                cov = CoverageBlockManager.getCoverageByBin(allCovBin[i].charAt(allCovBin[i].length() - 1));
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

    protected void applyCoverageFilter(int minCaseCov, int minCtrlCov) {
        Sample sample = SampleManager.getMap().get(sampleId);

        if (sample.isCase()) // --min-case-coverage-call or --min-case-coverage-no-call
        {
            if (!GenotypeLevelFilterCommand.isMinCoverageValid(coverage, minCaseCov)) {
                setMissing();
            }
        } else // --min-ctrl-coverage-call or --min-ctrl-coverage-no-call
        {
            if (!GenotypeLevelFilterCommand.isMinCoverageValid(coverage, minCtrlCov)) {
                setMissing();
            }
        }
    }

    /*
     * Outside of pseudoautosomal regions:
     *
     * ChrX: females are treated normally
     *
     * ChrX: males have het removed
     *
     * ChrY: females are set to missing
     *
     * ChrY: males have het removed
     *
     * Inside of pseudoautosomal region which are treated like autosomes.
     */
    protected void checkValidOnXY(Region r) {
        if (genotype != Data.NA) {
            boolean isValid = true;

            Sample sample = SampleManager.getMap().get(sampleId);

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

    public void applyFilters(Region region) {
        // min coverage filter
        applyCoverageFilter(GenotypeLevelFilterCommand.minCaseCoverageNoCall,
                GenotypeLevelFilterCommand.minCtrlCoverageNoCall);

        // default pseudoautosomal region filter
        checkValidOnXY(region);
    }

    private void setMissing() {
        if (TrioCommand.isTrioDenovo
                && TrioManager.isParent(sampleId)) {
            // do nothing
        } else {
            genotype = Data.NA;
            coverage = Data.NA;
        }
    }

    public boolean isValid() {
        if (genotype != Data.NA) {
            return true;
        }

        return false;
    }
}
