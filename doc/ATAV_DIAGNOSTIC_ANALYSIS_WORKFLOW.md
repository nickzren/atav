# ATAV Diagnostic Analysis Workflow
### Commands to run a diagnostic analysis in ATAV.


#### Prepare sample files
A sample script to make the files is this [preprocess script](https://github.com/igm-team/Diagnostic/blob/master/diagnostic_analysis_pipeline/preprocess.py).<br>
Alternatively, they can be made manually. The necessary files are:
1. allSamplesWithControls.ped<br>
&nbsp;&nbsp;&nbsp;This file contains all cases and controls. Used for a non-trio-style variant search.
2. triosWithControls.ped<br>
&nbsp;&nbsp;&nbsp;Just the trios, along with controls. Used for trio-specific searches.
3. allSamples.ped<br>
&nbsp;&nbsp;&nbsp;All individuals in the study, without controls. Used for coverage.

They should be in this format (tab-delimited):<br>
Family ID, Individual ID, Paternal ID, Maternal ID, Sex, Phenotype, Sample Type, Capture Kit
1. Family ID: specify a family ID or use the same value as Individual ID to indicate this sample
 is being used as a non family sample
2. Individual ID: sample name
3. Paternal ID: either the father's sample ID, or 0 if run as non-trio.
4. Maternal ID: either the mother's sample ID, or 0 if run as non-trio.
5. Sex: 1 for male, 2 for female.
6. Phenotype: 1 for control, 2 for case.
7. Sample Type: Exome or genome.
8. Capture Kit: The sequencing capture kit.

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

With those files, you can now run the following 4 ATAV commands: 
```
CODING_AND_SPLICE_EFFECTS=HIGH:exon_loss_variant,HIGH:frameshift_variant,HIGH:rare_amino_acid_variant,HIGH:stop_gained,HIGH:start_lost,HIGH:stop_lost,HIGH:splice_acceptor_variant,HIGH:splice_donor_variant,HIGH:gene_fusion,HIGH:bidirectional_gene_fusion,MODERATE:3_prime_UTR_truncation+exon_loss_variant,MODERATE:5_prime_UTR_truncation+exon_loss_variant,MODERATE:coding_sequence_variant,MODERATE:disruptive_inframe_deletion,MODERATE:disruptive_inframe_insertion,MODERATE:conservative_inframe_deletion,MODERATE:conservative_inframe_insertion,MODERATE:missense_variant+splice_region_variant,MODERATE:missense_variant,MODERATE:splice_region_variant,LOW:5_prime_UTR_premature_start_codon_gain_variant,LOW:initiator_codon_variant,LOW:initiator_codon_variant+non_canonical_start_codon,LOW:splice_region_variant+synonymous_variant,LOW:splice_region_variant,LOW:start_retained,LOW:stop_retained_variant,LOW:synonymous_variant,

java -jar $ATAV_HOME/atav_trunk.jar --email --list-var-geno --effect $CODING_AND_SPLICE_EFFECTS --ctrl-af 0.01 --filter pass,likely,intermediate --evs-maf 0.01 --exac-af 0.01 --exac-pop global,afr,amr,eas,sas,fin,nfe --min-ad-alt 3 --qual 30 --qd 2 --gq 20 --exclude-evs-qc-failed --exclude-artifacts --include-qc-missing --include-known-var --include-evs --include-exac --include-gnomad-genome --include-gnomad-exome --include-gerp --include-rvis --include-sub-rvis --include-mgi --include-trap --include-denovo-db --disable-check-on-sex-chr --case-only --include-discovehr --include-mtr --include-limbr --include-revel --flag-repeat-region --sample allSamplesWithControls.ped --out allNonTrio

java -jar $ATAV_HOME/atav_trunk.jar --email --list-trio --effect $CODING_AND_SPLICE_EFFECTS --ctrl-af 0.01 --filter pass,likely,intermediate --evs-maf 0.01 --exac-af 0.01 --exac-pop global,afr,amr,eas,sas,fin,nfe --min-ad-alt 3 --qual 30 --qd 1 --gq 20 --exclude-evs-qc-failed --exclude-artifacts --include-qc-missing --include-known-var --include-rvis --include-sub-rvis --include-gerp --include-mgi --include-trap --include-denovo-db --disable-check-on-sex-chr --include-gnomad-genome --include-gnomad-exome --include-discovehr --include-mtr --include-limbr --include-revel --flag-repeat-region --sample triosWithControls.ped --out trio

java -jar $ATAV_HOME/atav_trunk.jar --email --effect $CODING_AND_SPLICE_EFFECTS --ctrl-af 0.0005 --evs-maf 0.0005 --exac-af 0.0005 --exac-pop global,afr,amr,eas,sas,fin,nfe --filter pass,likely,intermediate --exclude-evs-qc-failed --exclude-artifacts --qual 30 --mq 40 --rprs -4 --mqrs -8 --child-qd 2 --child-het-percent-alt-read 0.30-0.80 --min-child-binomial 0.1 --max-parent-binomial 0.00001 --include-qc-missing --include-known-var --include-evs --include-exac --include-gnomad-genome --include-gnomad-exome --include-gerp --include-rvis --include-sub-rvis --include-mgi --include-trap --include-denovo-db --flag-repeat-region --disable-check-on-sex-chr --list-parental-mosaic --sample triosWithControls.ped --out ptlMcsm

java -jar $ATAV_HOME/atav_trunk.jar --email --coverage-summary --sample allSamples.ped --gene-boundaries /nfs/goldstein/goldsteinlab/Bioinformatics/scripts/CCDS_public_releases/addjusted.CCDS.genes.index.r14.txt --min-coverage 10 --percent-region-covered .9 --out coverageOutputDirectory
```

The ATAV output can then be parsed and filtered to produce a final variant report.<br>
Our current pipeline is [here](https://github.com/igm-team/generateVariantReportDragen), and our newly edited one is [here](https://github.com/igm-team/Diagnostic/tree/master/diagnostic_analysis_pipeline).
