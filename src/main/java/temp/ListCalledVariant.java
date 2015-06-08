/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package temp;

import function.base.AnalysisBase;
import function.genotype.base.Carrier;
import function.variant.base.Region;
import function.variant.base.Variant;
import global.Data;
import function.genotype.base.CarrierBlockManager;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import utils.CommandValue;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author qwang
 */
public class ListCalledVariant extends AnalysisBase {

    protected ResultSet rset;
    protected Region region;
    protected int totalNumOfRegionList;
    protected int analyzedRecords;
    protected int VariantId;
    protected boolean isIndel;

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    protected void clearData() throws SQLException {
        rset.close();
    }

    @Override
    public void processDatabaseData() throws Exception {
        totalNumOfRegionList = RegionManager.getRegionSize();
        for (int r = 0; r < totalNumOfRegionList; r++) {
            for (String varType : Data.VARIANT_TYPE) {
                if (VariantManager.isVariantTypeValid(r, varType)) {
                    isIndel = varType.equals("indel");
                    analyzedRecords = 0;
                    region = RegionManager.getRegion(r, varType);
                    rset = getVariants(varType, region);
                    while (rset.next()) {
                        countVariant();
                        VariantId = rset.getInt(varType + "_id");
                        int position = rset.getInt("seq_region_pos");
                        
                        //HashMap<Integer, Carrier> carrierMap = CarrierBlockManager.getCarrierMap(varType, position, region.getChrStr(), VariantId);
                        HashMap<Integer, Carrier> carrierMap = getCarrierMap(varType, VariantId);
                        //InsertRowIntoDataBase(VariantId, varType, region.getChrStr());
                        if (carrierMap.isEmpty()) {
                            
                        }
                    }
                    clearData();
                }
            }
        }
    }

    private static HashMap<Integer, Carrier> getCarrierMap(String varType, int varID) {
        String sqlCarrier = "SELECT * "
                + "FROM called_" + varType + " va,"
                + Data.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE va." + varType + "_id = " + varID
                + " AND va.sample_id = t.id";

        HashMap<Integer, Carrier> carrierMap = new HashMap<Integer, Carrier>();
        try {
            ResultSet rs = DBManager.executeQuery(sqlCarrier);
            while (rs.next()) {
                Carrier carrier = new Carrier();
                carrier.init(rs);
                carrier.checkCoverageFilter(CommandValue.minCaseCoverageCall,
                        CommandValue.minCtrlCoverageCall);
                carrier.checkQualityFilter();
                //carrier.checkValidOnXY(var);
                carrierMap.put(carrier.getSampleId(), carrier);
            }
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
        return carrierMap;
    }
    
    private void InsertRowIntoDataBase(int vid, String vtype, String chr) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT IGNORE INTO called_").append(vtype);
        sb.append("_chr").append(chr);
        sb.append(" SELECT * FROM called_").append(vtype);
        sb.append(" WHERE ").append(vtype).append("_id = ").append(vid);
        try {
            DBManager.executeUpdate(sb.toString());
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    protected static ResultSet getVariants(String varType, Region region) throws SQLException {
        System.out.print("It is collecting " + varType.toUpperCase() + "s..." + "                    \r");
        String sqlCode = "SELECT v." + varType + "_id, v.seq_region_id, v.seq_region_pos FROM "
                + varType + " AS v ";
        sqlCode = RegionManager.addRegionToSQL(region, sqlCode, varType.equals("indel"));
        return DBManager.executeReadOnlyQuery(sqlCode);
    }

    @Override
    public void initOutput() {
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }
}
