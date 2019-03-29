library("ape")
library("seqinr")
library("rentrez")
library(rjson)

downloadAsy <- function(idAsy) {
	ncbi_asy <- entrez_link(dbfrom="assembly", id=idAsy, db="all")
	if(length(ncbi_asy$links$assembly_nuccore) == 0) {
		search = entrez_search(db="assembly", term=idAsy);
		if(search$count > 0) {
			idAsy = search$ids;
			ncbi_asy <- entrez_link(dbfrom="assembly", id=idAsy, db="all");
		}
	}
	#print("#1")
	#for(idSeq in ncbi_asy$links$assembly_nuccore_refseq) {
	for(idSeq in ncbi_asy$links$assembly_nuccore_insdc) {
	#print("#1.1")
		nuc <- entrez_summary(db="nuccore", id=idSeq)
		org <- gsub("\\ ", "_", trimws(paste(nuc$organism, nuc$strain, set="")))
		access <- nuc$accessionversion

		nucFile <- toJSON(nuc)
	
		print(org)
		print(access)
		#print(idSeq)
		dir.create(org, showWarnings = FALSE)
		#write(nucFile, paste(org, "/", access, ".summary", sep=""), sep="")
		continuar = TRUE
		while(continuar) {
			tryCatch({
				ncbi_seqs <- entrez_fetch(db="nucleotide", id=idSeq, rettype="fasta")
				write(ncbi_seqs, paste(org, "/", access, ".fna", sep=""), sep="")
				#print("ok")
				continuar = FALSE
			}, error = function(error) {
				print("erro")
			})
		}
		Sys.sleep(2)

		#continuar = TRUE
		#while(continuar) {
		#	tryCatch({
		#		ncbi_seqs <- entrez_fetch(db="nucleotide", id=idSeq, rettype="fasta_cds_na")
		#		write(ncbi_seqs, paste(org, "/", access, ".ffn", sep=""), sep="")
		#		print("ok")
		#		continuar = FALSE
		#	}, error = function(error) {
		#		print("erro")
		#	})
		#}
		#Sys.sleep(2)

		#continuar = TRUE
		#while(continuar) {
		#	tryCatch({
		#		print("ok1")
		#		ncbi_seqs <- entrez_fetch(db="nucleotide", id=idSeq, rettype="fasta_cds_aa")
		#		print("ok2")
		#		write(ncbi_seqs, paste(org, "/", access, ".faa", sep=""), sep="")
		#		print("ok")
		#		continuar = FALSE
		#	}, error = function(error) {
		#		print("erro")
		#	})
		#}
		#Sys.sleep(2)

		#continuar = TRUE
		#while(continuar) {
		#	tryCatch({
		#		ncbi_seqs <- entrez_fetch(db="nucleotide", id=idSeq, rettype="gbwithparts")
		#		write(ncbi_seqs, paste(org, "/", access, ".gbwithparts", sep=""), sep="")
		#		print("ok")
		#		continuar = FALSE
		#	}, error = function(error) {
		#		print("erro")
		#	})
		#}
		#Sys.sleep(2)
	}
}

