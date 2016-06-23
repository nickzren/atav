. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/trio_comphet/trio_comphet_comphet.csv $ATAV_OUTPUT_PATH/old/trio_comphet/trio_comphet_comphet.csv > $ATAV_DIFF_PATH/comphet.diff

diff $ATAV_OUTPUT_PATH/new/trio_comphet/trio_comphet_comphet_noflag.csv $ATAV_OUTPUT_PATH/old/trio_comphet/trio_comphet_comphet_noflag.csv > $ATAV_DIFF_PATH/comphet_noflag.diff
