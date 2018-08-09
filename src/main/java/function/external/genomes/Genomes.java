package function.external.genomes;

import global.Data;
import java.sql.ResultSet;
import java.util.StringJoiner;
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

    private float[] af;

    public Genomes(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = ref.length() == alt.length();

        initAF();
    }

    public Genomes(boolean isIndel, ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");

            af = new float[GenomesManager.GENOMES_POP.length];

            for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
                af[i] = rs.getFloat(GenomesManager.GENOMES_POP[i] + "_af");
            }

            isSnv = !isIndel;
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initAF() {
        af = new float[GenomesManager.GENOMES_POP.length];

        try {
            String sql = GenomesManager.getSql4AF(isSnv, chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
                    af[i] = rs.getFloat(GenomesManager.GENOMES_POP[i] + "_af");
                }
            } else {
                for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
                    af[i] = Data.FLOAT_NA;
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private float getMaxAF() {
        float value = Data.FLOAT_NA;

        for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
            if (af[i] != Data.FLOAT_NA
                    && GenomesCommand.genomesPop.contains(GenomesManager.GENOMES_POP[i])) {
                value = Math.max(value, af[i]);
            }
        }

        return value;
    }

    public boolean isValid() {
        return GenomesCommand.isMaxGenomesAFValid(getMaxAF());
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        for (int i = 0; i < GenomesManager.GENOMES_POP.length; i++) {
            sj.add(FormatManager.getFloat(af[i]));
        }

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
