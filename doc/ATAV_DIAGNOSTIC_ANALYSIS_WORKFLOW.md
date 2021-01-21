# ATAV Collapsing Analysis Workflow

The instruction of ATAV diagnostic analysis workflow. It has been developed to filter for and prioritize pathogenic variation/mutation in patient samples. Analysis can be done on trios or non-trios depending on the context.

## Requirement
* [ATAV CLI](AWS_EC2_SETUP.md) and [ATAV Database](https://github.com/nickzren/atav-database/tree/main/ec2) setup on AWS EC2.

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
Sample file format: Family ID, Individual ID, Paternal ID, Maternal ID, Sex, Phenotype, Sample Type, Capture Kit (tab-delimited)
1. Family ID: specify a family ID or use the same value as Individual ID to indicate this sample
 is being used as a non family sample
2. Individual ID: sample name
3. Paternal ID: sample name or 0 indicate not available
4. Maternal ID: sample name or 0 indicate not available
5. Sex: 1=male,2=female
6. Phenotype: 1=control, 2=case
7. Sample Type: exome or genome
8. Capture Kit: Genome_v1
```
# set sample file as environment variable
export SAMPLE_FILE=PATH_TO_YOUR_SAMPLE_FILE
```

#### Sample Pruning
To run atav generate ped map file function, then atav will run king and flashpca downstream to remove related samples and ethnicity outliers from your input sample list.
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

#### Trio Analysis
```
java -jar $ATAV_HOME/atav_trunk.jar \
--list-trio \
--effect $ATAV_HOME/data/effect/codingandsplice.txt \
--ctrl-af 0.01 \
--filter pass,likely,intermediate \
--evs-maf 0.01 \
--exac-af 0.01 \
--exac-pop global,afr,amr,eas,sas,fin,nfe \
--min-coverage 10 --qual 30 --qd 1 --gq 20 \
--exclude-evs-qc-failed --exclude-artifacts --include-qc-missing --include-known-var --include-rvis --include-sub-rvis --include-gerp --include-mgi --include-denovo-db --disable-check-on-sex-chr --include-gnomad-genome --include-gnomad-exome --include-discovehr --include-mtr \
--sample $SAMPLE_FILE \
--out $OUTPUT/trio
```
