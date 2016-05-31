. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/collapsing_dom/collapsing_dom_genotypes.csv $ATAV_OUTPUT_PATH/old/collapsing_dom/collapsing_dom_genotypes.csv > $ATAV_DIFF_PATH/genotypes.csv.diff

diff $ATAV_OUTPUT_PATH/new/collapsing_dom/collapsing_dom_summary.csv $ATAV_OUTPUT_PATH/old/collapsing_dom/collapsing_dom_summary.csv > $ATAV_DIFF_PATH/summary.csv.diff
