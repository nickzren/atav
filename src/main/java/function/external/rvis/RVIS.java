package function.external.rvis;

import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class RVIS {

    public float rvisEVS; // 0.1%RVIS%[EVS]
    public String edgeCaseEVS; // EdgeCase[EVS]
    public float oEratioPercentileEVS; // OEratio%tile[EVS]
    public float genicConstraintEVS; // GenicConstraint[EVS]
    public float anypopnRVISPercentileExAC; // 0.05%_anypopn_RVIS%tile[ExAC]
    public float oEratioPercentileExAC; // OEratio%tile[ExAC]
    public float genicConstraintMisZPercentileExAC; // GenicConstraint_mis-z%tile[ExAC]

    public RVIS(CSVRecord record) {
        rvisEVS = FormatManager.getFloat(record, RvisManager.RVIS_EVS_HEADER);
        edgeCaseEVS = record.get(RvisManager.EDGE_CASE_EVS_HEADER);
        oEratioPercentileEVS = FormatManager.getFloat(record, RvisManager.OERATIO_PERCENTILE_EVS_HEADER);
        genicConstraintEVS = FormatManager.getFloat(record, RvisManager.GENIC_CONSTRAINT_EVS_HEADER);
        anypopnRVISPercentileExAC = FormatManager.getFloat(record, RvisManager.ANYPOPN_RVIS_PERCENTILE_EXAC_HEADER);
        oEratioPercentileExAC = FormatManager.getFloat(record, RvisManager.OERATIO_PERCENTILE_EXAC_HEADER);
        genicConstraintMisZPercentileExAC = FormatManager.getFloat(record, RvisManager.GENIC_CONSTRAINT_MIS_Z_PERCENTILE_EXAC_HEADER);
    }

    public boolean isHotZone() {
        return (edgeCaseEVS.equals("N") && (rvisEVS <= 25 || anypopnRVISPercentileExAC <= 25))
                || (edgeCaseEVS.equals("Y") && (oEratioPercentileEVS <= 25 || oEratioPercentileExAC <= 25))
                || genicConstraintEVS <= 25
                || genicConstraintMisZPercentileExAC <= 25;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getFloat(rvisEVS));
        sj.add(edgeCaseEVS);
        sj.add(FormatManager.getFloat(oEratioPercentileEVS));
        sj.add(FormatManager.getFloat(genicConstraintEVS));
        sj.add(FormatManager.getFloat(anypopnRVISPercentileExAC));
        sj.add(FormatManager.getFloat(oEratioPercentileExAC));
        sj.add(FormatManager.getFloat(genicConstraintMisZPercentileExAC));

        return sj.toString();
    }
}
