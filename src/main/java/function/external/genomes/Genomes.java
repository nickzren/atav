package function.external.genomes;

import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 * 1000 Genomes Project data object
 *
 * @author nick
 */
public class Genomes {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private float[] maf;

    public Genomes(String id) {
        initBasic(id);

        initMaf();
    }

    private void initBasic(String id) {
        String[] tmp = id.split("-");
        chr = tmp[0];
        pos = Integer.valueOf(tmp[1]);
        ref = tmp[2];
        alt = tmp[3];

        isSnv = true;

        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }
    }

    private void initMaf() {
        maf = new float[GenomesManager.GENOMES_POP.length];

        try {
            String sql = GenomesManager.getSql4Maf(isSnv, chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
                    maf[i] = rs.getFloat(GenomesManager.GENOMES_POP[i] + "_maf");
                }
            } else {
                for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
                    maf[i] = Data.NA;
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    private float getMaxMaf() {
        float value = Data.NA;

        for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
            if (maf[i] != Data.NA
                    && GenomesCommand.genomesPop.contains(GenomesManager.GENOMES_POP[i])) {
                value = Math.max(value, maf[i]);
            }
        }

        return value;
    }
    
    public boolean isValid() {
        return GenomesCommand.isMaxGenomesMafValid(getMaxMaf());
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
            sb.append(FormatManager.getDouble(maf[i])).append(",");
        }

        return sb.toString();
    }
}
