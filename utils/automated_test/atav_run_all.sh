. atav_config_paths.sh

rm -rf $ATAV_OUTPUT_PATH/new/*
rm -rf $ATAV_OUTPUT_PATH/old/*

./atav_run_geno.sh

./atav_run_fisher.sh

./atav_run_linear.sh

./atav_run_collapsing_dom.sh

./atav_run_collapsing_rec.sh

./atav_run_collapsing_comphet.sh

./atav_run_list_trio.sh

./atav_run_pedmap.sh

./atav_run_family.sh

./atav_run_coverage_summary.sh

./atav_run_site_coverage_summary.sh

./atav_run_coverage_comparison.sh

./atav_run_site_coverage_comparison.sh

./atav_run_flanking_seq.sh 

./atav_run_anno.sh

./atav_run_sibling_comphet.sh