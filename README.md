# GTACG-backend
Backend application for the GTACG project (Gene Tags Assessment by Comparative Genomics)

Dependencies

In order to run the programs in the GTACG, the following dependencies must be satisfied:

	R		sudo apt install r-base
	Blast 	   	sudo apt install ncbi-blast+
	Parallel	sudo apt install parallel
	JRE		sudo apt install openjdk-8-jre
	Muscle		sudo apt install muscle
	Clustalo	sudo apt install clustalo
	PhyML		sudo apt install phyml
	FastTree	sudo apt install fasttree
	PHYLIP	sudo apt install phylip
	MMseqs2	https://github.com/soedinglab/MMseqs2
	Clann		http://chriscreevey.github.io/clann/
	

As well as the following R libraries:
	ape
	phangorn

Execution

The GTACG is developed in Java and can be run using a JAR package. You can donwload it using the following command line:

	$ wget http://143.107.58.250/GTACG.jar

A proper execution of the GTACG pipeline requires at least genomic files (in fasta format) and coding sequences (CDS, faa, gbf, gb, gff) files. Additionally, you can define phenotypes groups. The following command lines can be used to download files with examples of these formats:

	$ wget http://143.107.58.250/xantho15.tar.gz
	$ tar -xzvf xantho15.tar.gz

The next step is to produce a file containing the local alignments of all CDSs against all CDS. A previous step to this is to list all CDS, there are many ways to do this, and one of them is native in the framework through the following command line:

	$ java -jar GTACG.jar ExportAllSequences -dic xantho15/xantho15.gff.dic -dicFormat gff -out all.faa

Both Blast and MMseqs2 are equally competent for the generation of local alignments. You should choose one or another way to accomplish this task (replacing the word THREADS with the number of threads you want to use):

	$ mkdir blast;
	$ makeblastdb -dbtype prot -in all.faa -out blast/all.faa.db
	$ cat all.faa | parallel --block 50k -jTHREADS --recstart '\n>' --pipe blastp -evalue 0.0000000001 -outfmt 6 -db blast/all.faa.db -num_alignments 1000 -query - > all.faa.m8
	$ gzip all.faa.m8 # Os resultados podem ser utilizados sem alterações ou compactados

	$ mkdir mmseqs2
	$ mmseqs createdb all.faa mmseqs2/all.db
	$ mmseqs easy-search all.faa mmseqs2/all.db all.faa.mmseqs2.m8 tmp
	$ gzip all.faa.mmseqs2.m8

Then it is necessary to clustering the sequences, producing homologous families. This requires choosing a minimum percentage of coverage using MultilayerClustering) or exploring a range of values (using ExploratoryClustering). As follows:

	$ java -jar GTACG.jar ExploratoryClustering -dic xantho15/xantho15.gff.dic -dicFormat gff -m8 all.faa.m8.gz -start 20 -end 100 -threads 4
	$ java -jar GTACG.jar MultilayerClustering -dic xantho15/xantho15.gff.dic -dicFormat gff -m8 all.faa.m8.gz -minPercLenAlign 45 -out 45.rests -threads 4

With the result of the clustering of the sequences it is necessary to create a relationship graph among the CDS:

	$ java -jar GTACG.jar ExportGraph -dic xantho15/xantho15.gff.dic -dicFormat gff -m8 all.faa.m8.gz -rests 45.rests -out xantho15.graph -heads xantho15.heads
gzip xantho15.graph

The next step is to produce all the multiple alignments and phylogenies of each homologous and orthologous families:

	$ java -jar GTACG.jar ExportTreeFiles -dic xantho15/xantho15.gff.dic -dicFormat gff -graph xantho15.graph.gz -heads xantho15.heads -align clustalO -tree fastTree -threads 4 -out trees45

Finally, the files that are part of the website are produced.

	$ java -jar GTACG.jar ExportReport -dic xantho15/xantho15.gff.dic -dicFormat gff -graph xantho15.graph.gz -heads xantho15.heads -trees trees45 -groups xantho15/species.group -out data -threads 4
	$ wget http://143.107.58.250/report.tar.gz
	$ tar -xzvf report.tar.gz
	$ mv data/ report/

Now, the report folder is ready to be hosted on a server or be viewed locally. For a local viewing you need to run a program to hosts this data:

	$ http-server report -p 3000 --cors

You can interact with the system in http://localhost:3000/index.htm
