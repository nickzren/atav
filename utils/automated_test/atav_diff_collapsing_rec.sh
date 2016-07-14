. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/collapsing_rec/collapsing_rec_gene.sample.matrix.txt $ATAV_OUTPUT_PATH/old/collapsing_rec/collapsing_rec_gene.sample.matrix.txt > $ATAV_DIFF_PATH/gene.sample.matrix.txt.diff

diff $ATAV_OUTPUT_PATH/new/collapsing_rec/collapsing_rec_genotypes.csv $ATAV_OUTPUT_PATH/old/collapsing_rec/collapsing_rec_genotypes.csv > $ATAV_DIFF_PATH/genotypes.csv.diff

diff $ATAV_OUTPUT_PATH/new/collapsing_rec/collapsing_rec_missing.variant.txt $ATAV_OUTPUT_PATH/old/collapsing_rec/collapsing_rec_missing.variant.txt > $ATAV_DIFF_PATH/missing.variant.txt.diff

diff $ATAV_OUTPUT_PATH/new/collapsing_rec/collapsing_rec_qualified.variant.txt $ATAV_OUTPUT_PATH/old/collapsing_rec/collapsing_rec_qualified.variant.txt > $ATAV_DIFF_PATH/qualified.variant.txt.diff

diff $ATAV_OUTPUT_PATH/new/collapsing_rec/collapsing_rec_summary.csv $ATAV_OUTPUT_PATH/old/collapsing_rec/collapsing_rec_summary.csv > $ATAV_DIFF_PATH/summary.csv.diff
