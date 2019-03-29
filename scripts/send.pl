use strict;
use warnings;

use LWP::UserAgent; 
use HTTP::Request::Common;
use CGI;

my $url = 'http://ggdc.dsmz.de/submitjob.php';
my @files = glob( '/home/caio/Dropbox/teste/*' );
#my @ids;
my $i = 0;
my $j = 0;
my $k = 0;

#for ($i=0; $i < scalar @files; $i++) {
#	my $tmp = $files[$i];
#	$tmp =~ s/.fna//;
#	$tmp =~ s/\/home\/caio\/Dropbox\/teste\///;
#	$ids[$i] = $tmp;
#}

#my @ids = ('NC_003902', 'NC_003919', 'NC_006834', 'NC_007086', 'NC_007508', 'NC_007705', 'NC_010688', 'NC_010717', 'NC_013722', 'NC_016010', 'NC_017267', 'NC_017271', 'NC_020800', 'NC_020815', 'NC_022541');
#my @ids = ('CP009018.1', 'CP009019.1', 'CP009014.1', 'CP009015.1', 'CP009016.1', 'CP009011.1', 'CP009012.1', 'CP009013.1', 'CP009008.1', 'CP009009.1', 'CP009010.1', 'CP009005.1', 'CP009006.1', 'CP009007.1', 'CP009002.1', 'CP009003.1', 'CP009004.1', 'CP008999.1', 'CP009000.1', 'CP009001.1', 'CP008996.1', 'CP008997.1', 'CP008998.1', 'CP008993.1', 'CP008994.1', 'CP008995.1', 'CP008987.1', 'CP008988.1', 'CP008989.1', 'CP008990.1', 'CP008991.1', 'CP008992.1', 'FO681494.1', 'FO681495.1', 'FO681496.1', 'FO681497.1', 'AE013598.1', 'AP008229.1', 'CP007166.1', 'CP000967.2', 'CP003057.2', 'CP011955.1', 'CP011956.1', 'CP011957.1', 'CP011962.1', 'CP011963.1', 'CP011958.1', 'CP011959.1', 'CP007221.1', 'CP011960.1', 'CP011961.1', 'CP007810.1', 'CP010409.1', 'CP010410.1', 'CP008714.1', 'AE003849.1', 'AE003850.3', 'AE003851.1', 'CP000941.1', 'CP001011.1', 'CP001012.1', 'CP006739.1', 'CP006740.1', 'CP002165.1', 'CP002166.1', 'CP006696.1', 'CP006697.1', 'AE009442.1', 'AE009443.1');
my @ids = ('AE004092.2', 'AP012491.2', 'CP003901.1', 'CP007537.1', 'LN831034.1', 'AP014572.1', 'AP014585.1', 'CP008776.1', 'NC_007297.2', 'CP012045.1', 'NC_008022.1', 'AP014596.1', 'BA000034.2', 'NC_004070.1', 'NZ_CP007041.1', 'CP014138.1', 'NC_008024.1', 'NC_009332.1', 'CP000003.1', 'CP011414.1', 'NZ_CP011415.1', 'AFRY01000001.1', 'CP009612.1', 'CP000259.1', 'NC_008023.1', 'CP006366.1', 'NC_003485.1', 'CP008695.1', 'CP014139.1', 'CP011535.2', 'NC_007296', 'CP011069.1', 'CP011068.1', 'CP007241.1', 'CP007024.1', 'CP000829.1', 'NZ_CP013672.1', 'NC_017596.1', 'CP003121.1', 'NC_017040.1', 'CP014278.2', 'CP015238.1', 'NZ_CP008926.1', 'CP007561.1', 'CP007562.1', 'NZ_CP007240.1', 'CP007023.1', 'CP007560.1', 'AP017629.1', 'NZ_HG316453.1', 'NZ_CP013838.1', 'CP013839.1', 'NZ_CP013840.1 ', 'CP010450.1', 'CP010449.1');

#for ($i=0; $i < scalar @ids -1; $i++) {
#	for (my $j=$i+1; $j < scalar @ids; $j++) {
#		print "$i\t$j\t$ids[$i]\t$ids[$j]\n";
#	}
#}

my $bloco = 1;
my $str = "";

for (my $i=31; $i < scalar @ids -1; $i++) {
#for ($i=0; $i < 1; $i++) {
	for ($k=$i+1; $k < scalar @ids; $k=$k+50) {
		print "###### ".$bloco . "\n";
		$bloco = $bloco + 1;
		$str = "";
		for ($j=$k; ($j < scalar @ids && $j < $k+50); $j++) {
			$str = $str . $ids[$j];
			if($j < $k+49) {
				$str = $str . "\r\n";
			}
		}
		my $ua      = LWP::UserAgent->new();

		my $request =  POST $url,
			Content_Type => 'form-data',
			Content      => [
				blastVariant => 'GBDP2_BLASTPLUS',
				calculateConfidenceInterval => '0',
				targetName => $ids[$i],
				targetGenome => '',
				refGenbank => $str,
				multipleRefGenomes => '',
				email => 'caio.rns@gmail.com' ];

		my $content = $ua->request($request)->as_string();

		my $cgi = CGI->new();
		print $cgi->header(), $content;
		sleep(15*60);
	}
}


