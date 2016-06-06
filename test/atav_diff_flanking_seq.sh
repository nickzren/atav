. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/flanking_seq/flanking_seq_baseflankingseq.csv $ATAV_OUTPUT_PATH/old/flanking_seq/flanking_seq_baseflankingseq.csv > $ATAV_DIFF_PATH/baseflankingseq.diff

diff $ATAV_OUTPUT_PATH/new/flanking_seq/flanking_seq_updateflankingseq.csv $ATAV_OUTPUT_PATH/old/flanking_seq/flanking_seq_updateflankingseq.csv > $ATAV_DIFF_PATH/updateflankingseq.diff
