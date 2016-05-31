. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/fisher/fisher_dominant.csv $ATAV_OUTPUT_PATH/old/fisher/fisher_dominant.csv > $ATAV_DIFF_PATH/dominant.diff

diff $ATAV_OUTPUT_PATH/new/fisher/fisher_genotypic.csv $ATAV_OUTPUT_PATH/old/fisher/fisher_genotypic.csv > $ATAV_DIFF_PATH/genotypic.diff

diff $ATAV_OUTPUT_PATH/new/fisher/fisher_allelic.csv $ATAV_OUTPUT_PATH/old/fisher/fisher_allelic.csv > $ATAV_DIFF_PATH/allelic.diff

diff $ATAV_OUTPUT_PATH/new/fisher/fisher_recessive.csv $ATAV_OUTPUT_PATH/old/fisher/fisher_recessive.csv > $ATAV_DIFF_PATH/recessive.diff
