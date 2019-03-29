#!/bin/bash

export PATH=$PATH:$HOME/prokka-1.11/bin
export PATH=$PATH:$HOME/Download/signalp-4.1
prokka --setupdb

numThreads=$1
srcFolder=$2
destFolder=$3

cd "$srcFolder";

for i in *; do
	#echo "##### $i";
	if [ -d "${i}" ]
	then
		folder=$(basename "$i");
		#echo "##### $folder";
		for j in $i/*.fna; do
			file=$(basename "$j" .fna);
			folderFinal=$destFolder$folder;
			if [ -d "${folderFinal}" ]
			then
				echo "Existe $folderFinal";
			else
				#echo "Nao existe $folderFinal";
				#echo $file;
				prokka --force --outdir $destFolder$folder --locustag $file --prefix $file --kingdom Bacteria --gcode 11 --cpus $numThreads $j;
				cp $j $destFolder$folder;
			fi

		done;
	fi
done;
