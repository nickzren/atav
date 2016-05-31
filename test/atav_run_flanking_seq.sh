. atav_config_paths.sh

atav_beta.sh --variant $ATAV_DATA_PATH/variant/CHGV_trios_dnmArtifactProneSites.txt --out $ATAV_OUTPUT_PATH/new/flanking_seq --list-flanking-seq --width 500

atav.sh --variant $ATAV_DATA_PATH/variant/CHGV_trios_dnmArtifactProneSites.txt --out $ATAV_OUTPUT_PATH/old/flanking_seq --list-flanking-seq --width 500
