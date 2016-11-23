. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/list_trio/list_trio_denovoandhom.csv $ATAV_OUTPUT_PATH/old/list_trio/list_trio_denovoandhom.csv > $ATAV_DIFF_PATH/denovoandhom.diff

diff $ATAV_OUTPUT_PATH/new/list_trio/list_trio_comphet.csv $ATAV_OUTPUT_PATH/old/list_trio/list_trio_comphet.csv > $ATAV_DIFF_PATH/comphet.diff