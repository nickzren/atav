# ATAV Command Line Tool AWS EC2

The instruction of AWS EC2 setup for ATAV command line tool.

## Requirement
* Setup ATAV database on AWS EC2, check [here](https://github.com/nickzren/atav-database/tree/main/ec2) for details. (Load testing data and restore externaldb data)
* Current server required access (TCP) to ATAV database server or test ATAV CLI directly on db server

## Launch Amazon EC2

1. Choose an Amazon Machine Image: Amazon Linux 2 AMI (HVM)
2. Choose an Instance Type: t3.2xlarge
3. Configure Instance Details: default
4. Add Storage: 100GB gp3

## Tool Installation

#### Prepare to compile software on an Amazon Linux instance
```
sudo yum groupinstall "Development Tools"
```

#### Install Maven
```
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven
```

#### Install java 
```
sudo yum install java-1.8.0-openjdk-devel -y
export JAVA_HOME=/usr
```

#### Install miniconda2
```
wget https://repo.anaconda.com/miniconda/Miniconda2-latest-Linux-x86_64.sh
chmod 755 Miniconda2-latest-Linux-x86_64.sh
./Miniconda2-latest-Linux-x86_64.sh
source /home/ec2-user/.bashrc
conda config --add channels bioconda
conda create -n atav-cli python=2.7 seaborn docopt plink samtools tabix
```

#### Install ATAV
```
git clone https://github.com/nickzren/atav.git
mvn clean compile assembly:single -f atav/pom.xml
cp atav/target/atav-trunk-jar-with-dependencies.jar atav/atav_trunk.jar
export PATH=$PATH:atav/lib/
```

#### Install King
```
wget http://people.virginia.edu/~wc9c/KING/Linux-king.tar.gz
tar -xzvf Linux-king.tar.gz -C atav/lib/
```

#### Install FlashPCA
```
wget https://github.com/gabraham/flashpca/releases/download/v2.0/flashpca_x86-64.gz
gunzip -c flashpca_x86-64.gz > atav/lib/flashpca
chmod 755 atav/lib/flashpca
```

## Run ATAV CLI

#### Require setup
```
conda activate atav-cli
export ATAV_HOME=$(pwd)/atav/

# if not running on database server then update database connection settings, Ex. replace 127.0.0.1 with ec2-1-1-1-1.compute-1.amazonaws.com (fake)
vi atav/config/atav.dragen.system.config.properties
```

### Example commands

#### Create output directory
```
mkdir atav_output
export OUTPUT=atav_output
```

#### Quick Start
```
java -jar $ATAV_HOME/atav_trunk.jar --list-var-geno --sample $ATAV_HOME/data/sample/NA12878.tsv --out $OUTPUT/hello_atav
```

#### Create your sample file
Sample file format: Family ID, Individual ID, Paternal ID, Maternal ID, Sex, Phenotype, Sample Type, Capture Kit (tab-delimited)
Sex: 1=male, 2=female; Phenotype: 1=control, 2=case
```
# set sample file as environment variable
export SAMPLE_FILE=PATH_TO_YOUR_SAMPLE_FILE
```

#### Sample Pruning
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

#### Site Coverage Harmonization
```
java -jar $ATAV_HOME/atav_trunk.jar \
--site-coverage-comparison \
--gene-boundary $ATAV_HOME/data/ccds/addjusted.CCDS.genes.index.r20.hg19.txt.gz \
--min-coverage 10 \
--sample $SAMPLE_FILE \
--out $OUTPUT/site_coverage_comparison
```

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

Check the [Wiki](http://redmine.igm.cumc.columbia.edu/projects/atav/wiki) for more details.