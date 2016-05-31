. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/linear/linear_dominant.csv $ATAV_OUTPUT_PATH/old/linear/linear_dominant.csv > $ATAV_DIFF_PATH/dominant.diff

diff $ATAV_OUTPUT_PATH/new/linear/linear_additive.csv $ATAV_OUTPUT_PATH/old/linear/linear_additive.csv > $ATAV_DIFF_PATH/additive.diff

diff $ATAV_OUTPUT_PATH/new/linear/linear_allelic.csv $ATAV_OUTPUT_PATH/old/linear/linear_allelic.csv > $ATAV_DIFF_PATH/allelic.diff

diff $ATAV_OUTPUT_PATH/new/linear/linear_recessive.csv $ATAV_OUTPUT_PATH/old/linear/linear_recessive.csv > $ATAV_DIFF_PATH/recessive.diff
