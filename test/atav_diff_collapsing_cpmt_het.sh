. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/collapsing_comp_het/collapsing_comp_het_gene.sample.matrix.txt $ATAV_OUTPUT_PATH/old/collapsing_comp_het/collapsing_comp_het_gene.sample.matrix.txt > $ATAV_DIFF_PATH/gene.sample.matrix.txt.diff

diff $ATAV_OUTPUT_PATH/new/collapsing_comp_het/collapsing_comp_het_comphet.csv $ATAV_OUTPUT_PATH/old/collapsing_comp_het/collapsing_comp_het_comphet.csv > $ATAV_DIFF_PATH/genotypes.csv.diff

diff $ATAV_OUTPUT_PATH/new/collapsing_comp_het/collapsing_comp_het_summary.csv $ATAV_OUTPUT_PATH/old/collapsing_comp_het/collapsing_comp_het_summary.csv > $ATAV_DIFF_PATH/summary.csv.diff
