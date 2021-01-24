# ATAV Collapsing Analysis Workflow

Commands to run a collapsing analysis in ATAV.

## Requirement
* [ATAV CLI](https://github.com/nickzren/atav/blob/master/doc/AWS_EC2_SETUP.md) and [ATAV Database](https://github.com/nickzren/atav-database/tree/main/ec2) setup on AWS EC2.

## Starting Steps

#### Initial setup
```
conda activate atav-cli
export ATAV_HOME=$(pwd)/atav/

# if not running on database server then update database connection settings, Ex. replace 127.0.0.1 with ec2-1-1-1-1.compute-1.amazonaws.com (fake)
vi $ATAV_HOME/config/atav.dragen.system.config.properties

# create output directory
mkdir atav_output
export OUTPUT=atav_output
```

#### Create your sample file
After selecting the cases and controls to be used in the analysis, create a ped file following this format: <br>
Sample file format: Family ID, Individual ID, Paternal ID, Maternal ID, Sex, Phenotype, Sample Type, Capture Kit (tab-delimited)
1. Family ID: specify a family ID or use the same value as Individual ID to indicate this sample
 is being used as a non family sample
2. Individual ID: sample name
3. Paternal ID: sample name or 0 if not using.
4. Maternal ID: sample name or 0 indicate not available
5. Sex: 1 for male, 2 for female
6. Phenotype: 1 for control, 2 for case
7. Sample Type: Exome or genome
8. Capture Kit: The sequencing capture kit.
```
# set sample file as environment variable
export SAMPLE_FILE=PATH_TO_YOUR_SAMPLE_FILE
```

#### Cohort Pruning
We remove all related samples from our cohort.<br>
The "--ped-map" option creates the relevant ped file.<br>
"--kinship" triggers KING to find relationships and then [a python script](https://github.com/igm-team/atav/blob/11f304bf337689ba454467bcb109018f2d4ee311/lib/run_kinship.py) to remove related individuals.<br>
Finally, "--flashpca" runs PCA to help with ancestry pruning later.
```
java -jar $ATAV_HOME/atav_trunk.jar
--ped-map \
--kinship \
--flashpca \
--min-covered-case-percentage 95 --min-covered-ctrl-percentage 95 --min-coverage 10 \
--variant atav/data/variant/informative_snvs.ld_pruned.txt.gz \
--sample $SAMPLE_FILE \
--out $OUTPUT/pedmap
```

#### Ancestry Pruning
Users should follow the instructions in [the IGM's script to create ancestry clusters ](https://github.com/gpovysil/LouvainCMH/blob/master/lclust_Flash.R) to finalize their sample files for each ancestry cluster.

#### Site Coverage Harmonization
"--site-coverage-comparison" corrects differential coverage between the case and control cohorts by removing any sites that have greater than 11 percent difference in coverage between cases and controls.
```
java -jar $ATAV_HOME/atav_trunk.jar \
--site-coverage-comparison \
--gene-boundary $ATAV_HOME/data/ccds/addjusted.CCDS.genes.index.r20.hg19.txt.gz \
--min-coverage 10 \
--sample $SAMPLE_FILE \
--out $OUTPUT/site_coverage_comparison
```
The outputs from this command will be used in collapsing later. Set the following variables
```
GENE_BOUNDARIES=$PROJECT/Coverage/*_Coverage_site.clean.txt
COVERAGE_SUMMARY=$PROJECT/Coverage/*_Coverage_coverage.summary.csv
```

#### Unfiltered Collapsing
Collapsing models place filters upon the variants selected. In order to quickly rerun collapsing models later, we first run an unfiltered model to get all relevant variants. We later can run any specific models of interest.

#### Synonymous Collapsing Analysis
```
java -jar $ATAV_HOME/atav_trunk.jar \
--collapsing-dom \
--mann-whitney-test \
--gene-boundary $ATAV_HOME/data/ccds/addjusted.CCDS.genes.index.r20.hg19.txt.gz \
--include-rvis \
--include-known-var \
--effect LOW:start_retained,LOW:stop_retained_variant,LOW:synonymous_variant \
--exclude-artifacts \
--filter pass,likely,intermediate \
--exclude-evs-qc-failed \
--ccds-only \
--min-coverage 10 \
--qd 5 --qual 50 --mq 40 --gq 20 --snv-sor 3 --indel-sor 10 --snv-fs 60 --indel-fs 200 --rprs -3 --mqrs -10 --het-percent-alt-read 0.3-1 \
--include-qc-missing \
--max-qc-fail-sample 0 \
--min-exac-vqslod-snv 5000 --min-exac-vqslod-indel 5000 --gnomad-exome-af 0 --gnomad-exome-rf-tp-probability-snv 0 --gnomad-exome-rf-tp-probability-indel 0 --gnomad-exome-pop global --exac-pop global --exac-af 0 \
--loo-af 0.0005 \
--sample $SAMPLE_FILE \
--out $OUTPUT/dominantSynonymous/ 
```
