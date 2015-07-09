package function.genotype.pedmap;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Vector;

/**
 *
 * @author nick
 */
public class PedMapGenerator extends AnalysisBase4CalledVar {

    BufferedWriter bwPed = null;
    BufferedWriter bwMap = null;
    BufferedWriter bwTmpPed = null;

    private static final String PLINK_HOME = "/nfs/goldstein/goldsteinlab/software/sh/plink";
    private static final String CHIP2PCA2_HOME = "/nfs/goldstein/goldsteinlab/software/sh/chip2pca2";

    final String pedFile = CommonCommand.outputPath + "output.ped";
    final String mapFile = CommonCommand.outputPath + "output.map";
    final String tmpPedFile = CommonCommand.outputPath + "output_tmp.ped";

    final String chip2pcaDir = CommonCommand.realOutputPath
            + File.separator + "chip2pca";
    final String crDir = chip2pcaDir + File.separator
            + CommonCommand.outputDirName + "-cr";

    final String outputOpt = chip2pcaDir + File.separator
            + CommonCommand.outputDirName + ".opt";

    HashSet<String> outputIdSet = new HashSet<String>(); // include same pos of variantId and Rs number
    int sampleSize = SampleManager.getListSize();
    int qualifiedVariants = 0;
    Vector<CalledVariant> site;

    @Override
    public void initOutput() {
        try {
            bwPed = new BufferedWriter(new FileWriter(pedFile));
            bwMap = new BufferedWriter(new FileWriter(mapFile));
            bwTmpPed = new BufferedWriter(new FileWriter(tmpPedFile));
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
        site = new Vector<CalledVariant>();
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
        try {
            bwPed.flush();
            bwPed.close();
            bwMap.flush();
            bwMap.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (PedMapCommand.isEigenstrat) {
            doEigesntrat();
        }
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
        // just to process the last site
        if (PedMapCommand.isCombineMultiAlleles && !site.isEmpty() && !site.get(0).isIndel()) {
            processSite();
        }

        output();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            Output output = new Output(calledVar);
            output.countSampleGenoCov();
            output.calculate();

            if (output.isValid()) {
                doOutput(calledVar);
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void doOutput(CalledVariant calledVar) {
        try {
            if (calledVar.isIndel() || !PedMapCommand.isCombineMultiAlleles) {
                String rs = calledVar.getRsNumber();
                String varIdStr = calledVar.getVariantIdStr();
                String outputId;

                if (rs.equals("NA") || outputIdSet.contains(rs)
                        || PedMapCommand.isVariantIdOnly) {
                    outputId = varIdStr;
                } else {
                    outputId = rs;
                }

                if (!outputIdSet.contains(outputId)) {
                    outputIdSet.add(outputId);
                    qualifiedVariants++;

                    if (!outputIdSet.contains(varIdStr)) {
                        outputIdSet.add(varIdStr);
                    }

                    String chrStr = calledVar.getRegion().getChrStr();

                    if (calledVar.getRegion().isInsideXPseudoautosomalRegions()) {
                        chrStr = "XY";
                    }

                    bwMap.write(chrStr + "\t"
                            + outputId + "\t"
                            + "0\t"
                            + calledVar.getRegion().getStartPosition() + "\n");

                    outputTempGeno(calledVar);
                }
            } else {
                if (!(site.isEmpty()
                        || (site.firstElement().getRegion().getChrNum() == calledVar.getRegion().getChrNum()
                        && site.firstElement().getRegion().getStartPosition() == calledVar.getRegion().getStartPosition()))) {
                    processSite();
                }
                site.add(calledVar);
            }

        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private String getOutputID() {
        String rs = site.get(0).getRsNumber();
        String outputId = "";
        if (rs.equals("NA") || PedMapCommand.isVariantIdOnly) {
            outputId = site.get(0).getVariantIdStr();
            if (site.size() > 1) {
                for (int i = 1; i < site.size(); i++) {
                    if (!outputId.contains(site.get(i).getAllele())) {
                        outputId = outputId + "_" + site.get(i).getAllele();
                    }
                }
            }
        } else {
            outputId = rs;
        }
        return outputId;
    }

    void processSite() {
        try {
            if (site.isEmpty()) {
                return;
            }

            qualifiedVariants++;
            CalledVariant calledVar = site.get(0);

            String chrStr = site.get(0).getRegion().getChrStr();

            if (site.get(0).getRegion().isInsideXPseudoautosomalRegions()) {
                chrStr = "XY";
            }

            bwMap.write(chrStr + "\t"
                    + getOutputID() + "\t"
                    + "0\t"
                    + calledVar.getRegion().getStartPosition() + "\n");

            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < sampleSize; s++) {
                switch (calledVar.getGenotype(SampleManager.getList().get(s).getIndex())) {
                    case 2:
                        sb.append(calledVar.getAllele()).append(calledVar.getAllele());
                        break;
                    case 1:
                        sb.append(calledVar.getRefAllele()).append(calledVar.getAllele());
                        break;
                    case 0:
                        sb.append(calledVar.getRefAllele()).append(calledVar.getRefAllele());
                        break;
                    default:
                        sb.append("00");
                }
            }

            if (site.size() > 1) {
                int geno;
                for (int s = 0; s < sampleSize; s++) {
                    for (int i = 1; i < site.size(); i++) {
                        geno = site.get(i).getGenotype(SampleManager.getList().get(s).getIndex());
                        switch (geno) {
                            case 2:
                                //just replace the previous record
                                sb.replace(s * 2, (s + 1) * 2, site.get(i).getAllele() + site.get(i).getAllele());
                                break;
                            case 1:
                                //a bit more complex here, have to deal with case where one sample has two different non-ref alleles 
                                String allele = sb.substring(s * 2 + 1, (s + 1) * 2);
                                if (!allele.equalsIgnoreCase(site.get(i).getAllele())) {
                                    int pos = allele.equalsIgnoreCase(site.get(0).getRefAllele()) ? 1 : 0;
                                    sb.replace(s * 2 + pos, s * 2 + 1 + pos, site.get(i).getAllele());
                                }
                                break;
                            case 0:
                                if (sb.substring(s * 2, (s + 1) * 2).equals("00")) {
                                    sb.replace(s * 2, (s + 1) * 2, site.get(i).getRefAllele() + site.get(i).getRefAllele());
                                }
                                break;
                            default:
                            //do nothing   
                        }
                    }
                }
            }
            bwTmpPed.write(sb.toString());
            bwTmpPed.newLine();

            site.clear();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void output() {
        try {
            LogManager.writeAndPrint("Output the data to ped file now...");

            bwTmpPed.flush();
            bwTmpPed.close();
            File tmpFile = new File(tmpPedFile);
            RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");

            long rowLen = 2 * sampleSize + 1L;

            for (int s = 0; s < sampleSize; s++) {
                Sample sample = SampleManager.getList().get(s);

                String name = sample.getName();

                if (PedMapCommand.isEigenstrat) {
                    name = String.valueOf(sample.getPrepId());
                }

                int pheno = (int) sample.getPheno() + 1;

                bwPed.write(sample.getFamilyId() + " "
                        + name + " "
                        + sample.getPaternalId() + " "
                        + sample.getMaternalId() + " "
                        + sample.getSex() + " "
                        + pheno);

                for (int i = 0; i < qualifiedVariants; i++) {
                    for (int j = 0; j < 2; j++) {
                        long pos = i * rowLen + 2 * s + j;
                        raf.seek(pos);
                        byte allele = raf.readByte();
                        bwPed.write(" " + String.valueOf((char) allele));
                    }
                }

                bwPed.newLine();
            }

            tmpFile.delete();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void outputTempGeno(CalledVariant calledVar) throws Exception {
        int geno;
        for (int s = 0; s < sampleSize; s++) {
            geno = calledVar.getGenotype(SampleManager.getList().get(s).getIndex());
            if (geno == 2) {
                if (calledVar.isSnv()) {
                    bwTmpPed.write(calledVar.getAllele() + calledVar.getAllele());
                } else {
                    if (calledVar.isDel()) {
                        bwTmpPed.write("DD");
                    } else {
                        bwTmpPed.write("II");
                    }
                }
            } else if (geno == 1) {
                if (calledVar.isSnv()) {
                    bwTmpPed.write(calledVar.getRefAllele() + calledVar.getAllele());
                } else {
                    bwTmpPed.write("ID");
                }
            } else if (geno == 0) {
                if (calledVar.isSnv()) {
                    bwTmpPed.write(calledVar.getRefAllele() + calledVar.getRefAllele());
                } else {
                    if (calledVar.isDel()) {
                        bwTmpPed.write("II");
                    } else {
                        bwTmpPed.write("DD");
                    }
                }
            } else if (geno == Data.NA) {
                bwTmpPed.write("00");
            } else {
                bwTmpPed.write("00");
                LogManager.writeAndPrint("Invalid genotype: " + geno
                        + " (Variant ID: " + calledVar.getVariantIdStr() + ")");
            }
        }

        bwTmpPed.newLine();
    }

    public void doEigesntrat() {
        initDir(chip2pcaDir);

        File dir = initDir(crDir);

        String cmd = "cp " + Data.EXAMPLE_OPT_PATH + " " + outputOpt;

        ThirdPartyToolManager.systemCall(new String[]{cmd});

        cmd = PLINK_HOME + " --file " + CommonCommand.outputPath + "output --recode12 "
                + "--out " + crDir + File.separator + dir.getName();

        ThirdPartyToolManager.systemCall((new String[]{cmd}));

        cmd = PLINK_HOME + " --file " + CommonCommand.outputPath + "output --make-bed "
                + "--out " + crDir + File.separator + dir.getName();

        ThirdPartyToolManager.systemCall((new String[]{cmd}));

        cmd = "cd " + chip2pcaDir + "; "
                + CHIP2PCA2_HOME + " " + CommonCommand.outputDirName + " snppca";

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    private File initDir(String path) {
        File dir = new File(path);

        if (dir.exists()) {
            purgeDirectory(dir);
        } else {
            dir.mkdir();
        }

        return dir;
    }

    private void purgeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                purgeDirectory(file);
            }

            file.delete();
        }
    }

    @Override
    public String toString() {
        return "It is generating ped/map files...";
    }
}
