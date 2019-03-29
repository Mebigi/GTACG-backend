#!/bin/bach
#nohup sh blast.sh /home/caiosantiago/Dropbox/dados/streptococcus/patric/ 20 0.0000000001 todosE10.5 & 
#nohup sh blast.sh /home/caiosantiago/dados/xantho15/patric/ 22 0.0000000001 todosE10.10 &


scrFolder=$1;
numThread=$2;
evalue=$3;
name=$4;


#export BATCH_SIZE=16777216
cd $scrFolder;

mkdir blast;

cat */*.faa > $name.faa;
#cat */*.ffn > $name.ffn;

makeblastdb -dbtype prot -in $name.faa -out blast/$name.faa.db
#makeblastdb -dbtype nucl -in todos.ffn -out blast/todos.ffn.db

#blastp -num_threads $numThread -evalue 0.0000000001 -outfmt 6 -query todos.faa -out todos.faa.m8 -db blast/todos.faa.db
#blastn -num_threads $numThread -evalue 0.0000000001 -outfmt 6 -query todos.ffn -out todos.ffn.m8 -db blast/todos.ffn.db

#blastp -num_threads $numThread -evalue $evalue -outfmt 6 -query todos.faa -db blast/todos.faa.db -num_alignments 1000 -out $name.faa.m8
#blastn -num_threads $numThread -evalue $evalue -outfmt 6 -query todos.ffn -db blast/todos.ffn.db -num_alignments 1000 -out $name.ffn.m8

cat all.faa | parallel --block 50k -j$numThread --recstart '\n>' --pipe blastp -evalue $evalue -outfmt 6 -db blast/$name.faa.db -num_alignments 1000 -query - > $name.faa.m8

#cdhit -i todos.faa -o todos.faa.cdhit -T numThread
#cdhit -i todos.ffn -o todos.ffn.cdhit -T numThread


#perl -w extrairCabecalhosDeMultiFasta_faa.pl '*/*.faa' > todos_faa.cabecalhos
#mkdir blast
#cd blast
#cat ../*/*.faa > todos.faa
#formatdb -p T -i todos.faa -n todos_faa
#blastall -p blastp -a 8 -m 8 -e 0.0000000001 -d todos_faa -i todos.faa -o todosXtodos_faa.m8


#mkdir blast;
#makeblastdb -dbtype prot -in todos.ffa -out blast/todos.ffa.db

#blastp -num_threads 4 -evalue 0.0000000001 -outfmt 6 -query todos.ffa -out todos.ffa.m8 -db blast/todos.ffa.db
	
