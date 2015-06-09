package function.genotype.statistics;

import utils.FormatManager;

/**
 *
 * @author minhe
 */
public class HWEExact {

    private static double[] func;
    private static int maxN = -1;
    
    private static void logFactorial(int n) {
        if (n > maxN) {
            maxN = n;
            func = new double[n + 1];
            func[0] = 0.0;
            for (int i = 1; i <= n; i++) {
                func[i] = func[i - 1] + Math.log(i);
            }
        }
    }
    
    /**
     * calculates the P-value for this specific state
     *
     * @param Naa
     * @param Nab
     * @param Nbb
     * @return the P-value
     */
    private static double getBaseP(int Naa, int Nab, int Nbb) {
        int N = Naa + Nab + Nbb;
        int Na = 2 * Naa + Nab;
        int Nb = 2 * Nbb + Nab;

        logFactorial(2 * N);
        double p = (Nab * Math.log(2) + func[N] + func[Na] + func[Nb])
                - (func[Naa] + func[Nab] + func[Nbb] + func[2 * N]);
 
//       return Math.exp(p);       
        return Math.exp(FormatManager.roundToDecimals(p));
    }

    public static double getP(int Naa, int Nab, int Nbb) {
        int Na = 2 * Naa + Nab;
        int Nb = 2 * Nbb + Nab;

        double baseP = getBaseP(Naa, Nab, Nbb);
        double p = 0.0;
        int min = Math.min(Naa, Nbb);
        if (Na % 2 == 0) {
            for (int i = 0; i <= Na; i += 2) {
                Nab = i;
                Naa = (Na - Nab) / 2;
                Nbb = (Nb - Nab) / 2;
                if ((Naa >= 0) && (Nbb >= 0)) {
                    double tmpP = getBaseP(Naa, Nab, Nbb);
                    if (tmpP <= baseP) {
                        p += tmpP;
                    }
                }
            }
        } else {
            for (int i = 1; i <= Na; i += 2) {
                Nab = i;
                Naa = (Na - Nab) / 2;
                Nbb = (Nb - Nab) / 2;
                if ((Naa >= 0) && (Nbb >= 0)) {
                    double tmpP = getBaseP(Naa, Nab, Nbb);
                    if (tmpP <= baseP) {
                        p += tmpP;
                    }
                }
            }
        }

        if (p > 1.0) {
            p = 1.0;
        }

        return p;
    }
}
