library(igraph)
#------Carrega o arquivo numa tabela e então num data frame. Depois utiliza para criar um grafo
#geraCladograma <-function(caminho,saida) {
#calculaCoeficiente <-function(arquivo) {
#------Muda a área de Trabalho
	setwd("/tmp") 
	#grafo <- data.frame(grafo<-read.table("coautoriasInter/coautorias_espacos.csv",header = TRUE, fill = TRUE))
	#grafo <- data.frame(grafo<-read.table(arquivo))
	arquivo = "arestas.csv"
	grafo <- data.frame(grafo<-read.table(arquivo))
	g <- graph.data.frame(grafo, directed=FALSE)
	x <- transitivity(g,type="global")
	sink(file="saida.txt", append=TRUE)
	cat(x, fill= TRUE, labels=arquivo)

	sink()
#	dev.off();
#}

#calculaCoeficiente("TodosContraTodos_arestas_90.0_90.0.csv");

