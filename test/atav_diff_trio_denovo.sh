. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/trio_denovo/trio_denovo_denovoandhom.csv $ATAV_OUTPUT_PATH/old/trio_denovo/trio_denovo_denovoandhom.csv > $ATAV_DIFF_PATH/denovoandhom.diff

diff $ATAV_OUTPUT_PATH/new/trio_denovo/trio_denovo_denovoandhom_noflag.csv $ATAV_OUTPUT_PATH/old/trio_denovo/trio_denovo_denovoandhom_noflag.csv > $ATAV_DIFF_PATH/denovoandhom_noflag.diff
