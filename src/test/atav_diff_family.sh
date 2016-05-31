. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/family/family_shared.csv $ATAV_OUTPUT_PATH/old/family/family_shared.csv > $ATAV_DIFF_PATH/shared.diff

diff $ATAV_OUTPUT_PATH/new/family/family_notshared.csv $ATAV_OUTPUT_PATH/old/family/family_notshared.csv > $ATAV_DIFF_PATH/notshared.diff

diff $ATAV_OUTPUT_PATH/new/family/family_summary.all.shared.csv $ATAV_OUTPUT_PATH/old/family/family_summary.all.shared.csv > $ATAV_DIFF_PATH/summary.all.shared.diff

diff $ATAV_OUTPUT_PATH/new/family/family_summary.only.shared.csv $ATAV_OUTPUT_PATH/old/family/family_summary.only.shared.csv > $ATAV_DIFF_PATH/summary.only.shared.diff

diff $ATAV_OUTPUT_PATH/new/family/family_variant.carrier.csv $ATAV_OUTPUT_PATH/old/family/family_variant.carrier.csv > $ATAV_DIFF_PATH/variant.carrier.diff

