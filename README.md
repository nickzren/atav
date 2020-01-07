# ATAV

ATAV (Analysis Tool for Annotated Variants) is a command line tool that is designed to detect complex disease-associated rare genetic variants by performing association analysis on annotated variants derived from whole-genome or whole-exome sequencing data which are all stored in our centralized database.

The database stored variants, variant calls, coverage depths, and sample meta data for high-throughput sequencing samples. The database is implemented as a Percona MySQL fully normalized schema. The system consists of a master server and a set of slave servers. The primarily ingest method for incrementally adding samples to the system is via the data pipeline which annotates a single-sample VCF file, parses the VCF while inserting novel variants into the variant library on the master server, and loading all parsed data files into master database, it will then replicate to slave databases.

The slave servers host single-sample and cohort-level analysis queries, primarily as implemented in the ATAV.

Any questions, please contact Nick Ren (z.ren@columbia.edu)

For more details, please visit:
http://redmine.igm.cumc.columbia.edu/projects/atav/wiki
