# ATAV Collapsing Analysis Workflow

Commands to run a collapsing analysis in ATAV.

### Directory Structure
Our standard folder layout has the following structure at the end:
```
atav_output # Parent directory for this analysis.
├──Pruning # The pedmap output. Contains kinship pruning and principal component information.
├──Coverage_MaxPercentDiff_07_Cluster0 # Site harmonization.
└──Collapsing # The main analysis results.
   ├──Unfiltered # An all-encompassing variant search, that can be parsed more easily later.
   ├──dominantRareEnsemble # An example collapsing model.
   └── ... # Other collapsing models.
```

### Requirements
* [ATAV CLI](https://github.com/nickzren/atav/blob/master/doc/AWS_EC2_SETUP.md) and [ATAV Database](https://github.com/nickzren/atav-database/tree/main/ec2) set up on AWS EC2.

#### Initial setup
```
conda activate atav-cli
export ATAV_HOME=$(pwd)/atav/

# If you're not running on database server, update the database connection settings.
# For example, replace 127.0.0.1 with your server address (should look like
# ec2-1-1-1-1.compute-1.amazonaws.com) in this file:
# vi $ATAV_HOME/config/atav.dragen.system.config.properties

# Create an output directory; rename to your specific project.
mkdir atav_output
export PROJECT=atav_output
```

#### Prepare sample file
After selecting the cases and controls to be used in the analysis, create a ped file following this format (tab-delimited):<br>
Family ID, Individual ID, Paternal ID, Maternal ID, Sex, Phenotype, Sample Type, Capture Kit
1. Family ID: specify a family ID or use the same value as Individual ID to indicate this sample
 is being used as a non family sample
2. Individual ID: sample name
3. Paternal ID: put a 0 here, because all of the commands below do not use family information.
4. Maternal ID: put a 0 here, because all of the commands below do not use family information.
5. Sex: 1 for male, 2 for female
6. Phenotype: 1 for control, 2 for case
7. Sample Type: Exome or genome
8. Capture Kit: The sequencing capture kit.

Save the path to this file for the next command:
```
export INITIAL_SAMPLE_FILE=PATH_TO_YOUR_SAMPLE_FILE
```

#### Cohort Pruning
This step is run on the full cohort, before clustering.<br>
The `--ped-map` option creates the full ped file, which is required for the next steps.<br>
`--kinship` triggers KING to find relationships and then [a python script](https://github.com/igm-team/atav/blob/11f304bf337689ba454467bcb109018f2d4ee311/lib/run_kinship.py) to remove related individuals.<br>
Finally, `--flashpca` runs PCA to be used in the following step for ancestry clustering.
```
java -jar $ATAV_HOME/atav_trunk.jar
--ped-map \
--kinship \
--flashpca \
--min-covered-case-percentage 95 --min-covered-ctrl-percentage 95 --min-coverage 10 \
--variant atav/data/variant/informative_snvs.ld_pruned.txt.gz \
--sample $INITIAL_SAMPLE_FILE \
--out $PROJECT/Pruning
```

The final output is the \*flashpca_pruned_sample_file text file. This should be inspected,<br>
along with the PCA plots, to ensure that cases and controls are well-matched.<br>
If the cases primarily have the same ancestry, for example, then perhaps consider selecting <br>
the cluster of cases and controls that corresponds to that group.<br>
(ATAV has clustering scripts that automate this that are currently in production and will be released soon.)<br>
The path to the file should be saved as a variable. For example:
```
PRUNED_SAMPLES=$PROJECT/Pruning/*Pruning_flashpca_pruned_sample_file.txt
```

#### Site Coverage Harmonization
`--site-coverage-comparison` corrects differential coverage between the case and control cohorts by removing any sites that have greater than 11 percent difference in coverage between cases and controls.<br>
It starts with sites in the CCDS, and then filters them to produce a final gene boundaries file to be used in collapsing.<br>
First, save the coverage folder as a variable:
```
COVERAGE_FOLDER=$PROJECT/Coverage_MaxPercentDiff_07
```
```
java -jar $ATAV_HOME/atav_trunk.jar \
--site-coverage-comparison \
--gene-boundary $ATAV_HOME/data/ccds/addjusted.CCDS.genes.index.r20.hg19.txt.gz \
--min-coverage 10 \
--sample $PRUNED_SAMPLES \
--site-max-percent-cov-difference 0.07 \
--out $COVERAGE_FOLDER
```
The outputs from this command will be used in collapsing later. Set the following variables
```
GENE_BOUNDARIES=$COVERAGE_FOLDER/*_Coverage_site.clean.txt
COVERAGE_SUMMARY=$COVERAGE_FOLDER/*_Coverage_coverage.summary.csv
```

### Variables for Variant Searches

These will simplify the later commands.
```
CODING_SPLICE="HIGH:exon_loss_variant,HIGH:frameshift_variant,HIGH:rare_amino_acid_variant,HIGH:stop_gained,HIGH:start_lost,HIGH:stop_lost,HIGH:splice_acceptor_variant,HIGH:splice_donor_variant,HIGH:gene_fusion,HIGH:bidirectional_gene_fusion,MODERATE:3_prime_UTR_truncation+exon_loss_variant,MODERATE:5_prime_UTR_truncation+exon_loss_variant,MODERATE:coding_sequence_variant,MODERATE:disruptive_inframe_deletion,MODERATE:disruptive_inframe_insertion,MODERATE:conservative_inframe_deletion,MODERATE:conservative_inframe_insertion,MODERATE:missense_variant+splice_region_variant,MODERATE:missense_variant,MODERATE:splice_region_variant,LOW:5_prime_UTR_premature_start_codon_gain_variant,LOW:initiator_codon_variant,LOW:initiator_codon_variant+non_canonical_start_codon,LOW:splice_region_variant+synonymous_variant,LOW:splice_region_variant,LOW:start_retained,LOW:stop_retained_variant,LOW:synonymous_variant"

FUNCTIONAL_EFFECTS="HIGH:exon_loss_variant,HIGH:frameshift_variant,HIGH:rare_amino_acid_variant,HIGH:stop_gained,HIGH:start_lost,HIGH:stop_lost,HIGH:splice_acceptor_variant,HIGH:splice_donor_variant,HIGH:gene_fusion,HIGH:bidirectional_gene_fusion,MODERATE:3_prime_UTR_truncation+exon_loss_variant,MODERATE:5_prime_UTR_truncation+exon_loss_variant,MODERATE:coding_sequence_variant,MODERATE:disruptive_inframe_deletion,MODERATE:disruptive_inframe_insertion,MODERATE:conservative_inframe_deletion,MODERATE:conservative_inframe_insertion,MODERATE:missense_variant+splice_region_variant,MODERATE:missense_variant,LOW:5_prime_UTR_premature_start_codon_gain_variant,LOW:initiator_codon_variant,LOW:initiator_codon_variant+non_canonical_start_codon"

LOF_EFFECTS="HIGH:exon_loss_variant,HIGH:frameshift_variant,HIGH:rare_amino_acid_variant,HIGH:stop_gained,HIGH:stop_lost,HIGH:start_lost,HIGH:gene_fusion,HIGH:bidirectional_gene_fusion,HIGH:splice_acceptor_variant,HIGH:splice_donor_variant"

SYN_EFFECTS="LOW:start_retained,LOW:stop_retained_variant,LOW:synonymous_variant"

MISSENSE_ONLY="MODERATE:missense_variant+splice_region_variant,MODERATE:missense_variant"

INCLUDES="--include-gnomad-genome --include-gnomad-exome --include-exac --include-mtr --include-gerp --include-rvis --include-sub-rvis --include-limbr --include-revel --include-trap --include-discovehr --include-known-var --include-primate-ai --include-ccr --include-loftee --include-gnomad-gene-metrics --include-pext --include-mpc --flag-repeat-region --include-syn-rvis --include-genome-asia --include-iranome --include-gme --include-top-med" 

QC="--exclude-artifacts --filter pass,likely,intermediate --exclude-evs-qc-failed --ccds-only --min-coverage 10 --include-qc-missing --qd 5 --qual 50 --mq 40 --gq 20 --snv-fs 60 --indel-fs 200 --snv-sor 3 --indel-sor 10 --rprs -3 --mqrs -10 --het-percent-alt-read 0.3-1"
```

#### Unfiltered Collapsing
Collapsing models place filters upon the variants selected. In order to quickly rerun collapsing models later, we first run an unfiltered model to retrieve all relevant variants. This is the time-limiting step. From the output variants, we later can quickly run any specific models of interest.

```
COLLAPSING_FOLDER=$PROJECT/Collapsing
java -jar $ATAV_HOME/atav_trunk.jar --collapsing-dom --mann-whitney-test --gene-boundaries $GENE_BOUNDARIES --read-coverage-summary $COVERAGE_SUMMARY $INCLUDES $QC --effect $CODING_SPLICE --gnomad-genome-pop global --gnomad-genome-af 0.01 --gnomad-exome-pop global --gnomad-exome-af 0.01 --exac-pop global --exac-af 0.01 --loo-af 0.01 --sample $PRUNED_SAMPLES --out $COLLAPSING_FOLDER/Unfiltered
```

#### Individual Collapsing Models
The Unfiltered search outputs a "\_genotypes.csv" file. We set a variable to save that path, and can use it as an argument in our individual collapsing models.<br>
Below are a few recommended models; the [options in the ATAV wiki](https://redmine.igm.cumc.columbia.edu/projects/atav/wiki/Variant_Level_Filter_Options) can be used to construct others.

```
UNFILTERED_GENOTYPES_FILE=$COLLAPSING_FOLDER/Unfiltered/*genotypes.csv
## Dominant Syn
java -jar $ATAV_HOME/atav_trunk.jar --collapsing-lite --mann-whitney-test --genotype $UNFILTERED_GENOTYPES_FILE --effect $SYN_EFFECTS --min-exac-vqslod-snv 5000 --min-exac-vqslod-indel 5000 --gnomad-genome-rf-tp-probability-snv 0.01 --gnomad-genome-rf-tp-probability-indel 0.02 --gnomad-exome-rf-tp-probability-snv 0.01 --gnomad-exome-rf-tp-probability-indel 0.02 --gnomad-genome-pop global --gnomad-genome-af 0 --gnomad-exome-pop global --gnomad-exome-af 0 --exac-pop global --exac-af 0 --loo-af 0.0005 --max-qc-fail-sample 0 --sample $PRUNED_SAMPLES --out $COLLAPSING_FOLDER/dominantSynonymous/
sleep 2

### Dominant Ultra-rare
java -jar $ATAV_HOME/atav_trunk.jar --collapsing-lite --mann-whitney-test --genotype $UNFILTERED_GENOTYPES_FILE --effect $FUNCTIONAL_EFFECTS --polyphen probably --min-exac-vqslod-snv 5000 --min-exac-vqslod-indel 5000 --gnomad-genome-rf-tp-probability-snv 0.01 --gnomad-genome-rf-tp-probability-indel 0.02 --gnomad-exome-rf-tp-probability-snv 0.01 --gnomad-exome-rf-tp-probability-indel 0.02 --gnomad-genome-pop global --gnomad-genome-af 0 --gnomad-exome-pop global --gnomad-exome-af 0 --exac-pop global --exac-af 0 --loo-af 0.0005 --max-qc-fail-sample 0 --sample $PRUNED_SAMPLES --out $COLLAPSING_FOLDER/dominantUltraRare_Polyphen/
sleep 2

### Dominant PTV with LOFTEE
java -jar $ATAV_HOME/atav_trunk.jar --collapsing-lite --mann-whitney-test --genotype $UNFILTERED_GENOTYPES_FILE --effect $LOF_EFFECTS --exclude-false-loftee --min-exac-vqslod-snv -2.632 --min-exac-vqslod-indel 1.262 --gnomad-genome-rf-tp-probability-snv 0.01 --gnomad-genome-rf-tp-probability-indel 0.02 --gnomad-exome-rf-tp-probability-snv 0.01 --gnomad-exome-rf-tp-probability-indel 0.02 --gnomad-genome-pop global --gnomad-genome-af 0.001 --gnomad-exome-pop afr,amr,asj,eas,sas,fin,nfe --gnomad-exome-af 0.001 --exac-pop afr,amr,nfe,fin,eas,sas --exac-af 0.001 --loo-af 0.001 --max-qc-fail-sample 2 --sample $PRUNED_SAMPLES --out $COLLAPSING_FOLDER/dominantPTV_LOFTEE/
sleep 2

### Dominant Ultra-rare Ensemble
java -jar $ATAV_HOME/atav_trunk.jar --collapsing-lite --mann-whitney-test --genotype $UNFILTERED_GENOTYPES_FILE --effect $FUNCTIONAL_EFFECTS --polyphen probably --min-primate-ai 0.8 --min-revel-score 0.5 --ensemble-missense --min-exac-vqslod-snv 5000 --min-exac-vqslod-indel 5000 --gnomad-genome-rf-tp-probability-snv 0.01 --gnomad-genome-rf-tp-probability-indel 0.02 --gnomad-exome-rf-tp-probability-snv 0.01 --gnomad-exome-rf-tp-probability-indel 0.02 --gnomad-genome-pop global --gnomad-genome-af 0 --gnomad-exome-pop global --gnomad-exome-af 0 --exac-pop global --exac-af 0 --loo-af 0.0005 --max-qc-fail-sample 0 --sample $PRUNED_SAMPLES --out $COLLAPSING_FOLDER/dominantUltraRareEnsemble/
sleep 2

### Dominant Rare Ensemble
java -jar $ATAV_HOME/atav_trunk.jar --collapsing-lite --mann-whitney-test --genotype $UNFILTERED_GENOTYPES_FILE --effect $FUNCTIONAL_EFFECTS --polyphen probably --min-primate-ai 0.8 --min-revel-score 0.5 --ensemble-missense --min-exac-vqslod-snv -2.632 --min-exac-vqslod-indel 1.262 --gnomad-genome-rf-tp-probability-snv 0.01 --gnomad-genome-rf-tp-probability-indel 0.02 --gnomad-exome-rf-tp-probability-snv 0.01 --gnomad-exome-rf-tp-probability-indel 0.02 --gnomad-genome-pop global --gnomad-genome-af 0.0005 --gnomad-exome-pop afr,amr,asj,eas,sas,fin,nfe --gnomad-exome-af 0.0005 --exac-pop afr,amr,nfe,fin,eas,sas --exac-af 0.0005 --loo-af 0.001 --max-qc-fail-sample 2 --sample $PRUNED_SAMPLES --out $COLLAPSING_FOLDER/dominantRareEnsemble/
sleep 2

### Dominant Flexible
java -jar $ATAV_HOME/atav_trunk.jar --collapsing-lite --mann-whitney-test --genotype $UNFILTERED_GENOTYPES_FILE --effect $FUNCTIONAL_EFFECTS --min-exac-vqslod-snv -2.632 --min-exac-vqslod-indel 1.262 --gnomad-genome-rf-tp-probability-snv 0.01 --gnomad-genome-rf-tp-probability-indel 0.02 --gnomad-exome-rf-tp-probability-snv 0.01 --gnomad-exome-rf-tp-probability-indel 0.02 --gnomad-genome-pop global --gnomad-genome-af 0.001 --gnomad-exome-pop afr,amr,asj,eas,sas,fin,nfe --gnomad-exome-af 0.001 --exac-pop afr,amr,nfe,fin,eas,sas --exac-af 0.001 --loo-af 0.001 --max-qc-fail-sample 2 --sample $PRUNED_SAMPLES --out $COLLAPSING_FOLDER/dominantFlexible_MAF0.1_NoFilter/
sleep 2
```
