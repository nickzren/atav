use strict;
use warnings;

use lib '/nfs/goldstein/goldsteinlab/software/bioperl-1.2.3';
use lib '/nfs/goldstein/goldsteinlab/software/ensembl_73/modules';
use lib '/nfs/goldstein/goldsteinlab/software/ensembl-variation_73/modules';

use Bio::EnsEMBL::Registry;

#database info
my $core_db_host = "10.73.50.33";
my $db_user = "atav";
my $db_pw = "13qeadzc";

#connect to databases
my $reg = 'Bio::EnsEMBL::Registry';
$reg->load_registry_from_db(
     -host => "$core_db_host",
     -user => "$db_user",
     -pass => "$db_pw",
     -db_version => '73'
 );

my $slice_adaptor = $reg->get_adaptor('human', 'core', 'slice');

my @vals = @ARGV;

# Check if the command line options are valid. 
# The command line option has to be of this form: 
#	--variant callFlankingSeqs.txt --width 300 --out ~/Desktop/
#	 These can be in any order.
sub getValues{

	if (scalar(@vals) != 6){
		die "Error: Some arguments missing.\n";
	}
	my %values = @vals;	
	
	#check to see that --variant and --width and --out values are present.
	if (not(exists $values{"--variant"})) {
    		die "Error: Argument --file is misspelled or missing.\n";
	}
	if (not(exists $values{"--width"})) {
    		die "Error: Argument --width is misspelled or missing.\n";
	}
	if (not(exists $values{"--out"})) {
    		die "Error: Argument --out is misspelled or missing.\n";
	}
	
	#now check that the values assigned to the keys are of the right format.
	if (not(-f $values{"--variant"})){
		die "Error: The input object " . $values{"--variant"} . " is not a file.\n";
	}
	if (not(-d $values{"--out"})){
		#my $rindex = rindex($values{"--out"}, "/");
		#my $old_dir = substr($values{"--out"}, 0, $rindex);
		#if (not(-d $old_dir)){
		#	die "Error: The parent directory ". $old_dir ." does not exist";
		#}
		#my $new_dir = substr($values{"--out"}, $rindex+1, length($values{"--out"})-length($old_dir));
		#mkdir $values{"--out"};
	}	
	if ($values{"--width"} !~ /^[+]?\d+$/){ #returns false if real int.
		die "Error: The width argument must be non-negative integer.\n";
	}
	return (%values);
}


my %values = getValues(); # values is a hash container for the command line options
system "dos2unix -n --newfile $values{\"--variant\"} $values{\"--out\"}variant"; # if the inputFile is created in dos, convert it to the unix version.
open INPUT,"<", $values{"--out"}."variant" or die $!; 
my $outputFile = $values{"--out"}."flanking_seq_base.csv"; 
my $offset = $values{"--width"};


# Given the variant information, output the (variant and) flanking sequences.
# Currently, only supports the SNV and INDEL type of variants (RS variant types not supported). 
sub get_flankingSeqs{
	my @var = @_;
	my $chromosome = $var[0];
	my $pos = $var[1];

	my $output = "";
	my $slice_left = $slice_adaptor->fetch_by_region('chromosome', $chromosome, $pos-$offset, $pos-1);
	my $slice_right = $slice_adaptor->fetch_by_region('chromosome', $chromosome, $pos+1, $pos+$offset); 
	my $left_seq = $slice_left->seq;
	my $right_seq = $slice_right->seq;
	my $ref_allele = $slice_adaptor->fetch_by_region('chromosome', $chromosome, $pos, $pos);
	my $ref_allele_seq = $ref_allele->seq;
		$output= $left_seq . "," . $right_seq . "\n";

	return $output;
}


open OUTPUT, ">", $outputFile or die $!;
my $output_header = "Variant ID,Left Sequence,Right Sequence\n";
print OUTPUT $output_header;

while(my $line = <INPUT>)
{
	chomp ($line);
	my @var = split("-", $line);
	my $output = get_flankingSeqs(@var);
	print OUTPUT $line.",".$output;
}
close INPUT or die $!;
close OUTPUT or die $!;
