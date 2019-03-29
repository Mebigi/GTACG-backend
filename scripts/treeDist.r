#!/usr/bin/env Rscript
args = commandArgs(trailingOnly=TRUE)

library(ape)
library(phangorn)
t1 = read.tree(text=args[1])
t2 = read.tree(text=args[2])

rooted = TRUE;
if(args[3] == "false") {
	t1 = unroot(t1);
	t2 = unroot(t2);
	rooted = FALSE;
}

ti = intersect(t1$tip.label, t2$tip.label)

td1 = setdiff(t1$tip.label,ti)
for(x in td1) {
	t1 = drop.tip(t1, x)
}

td2 = setdiff(t2$tip.label,ti)
for(x in td2) {
	t2 = drop.tip(t2, x)
}

#dist = wRF.dist(t1, t2, rooted=rooted)
#dist = path.dist(t1, t2, use.weight=TRUE)
dist = path.dist(t1, t2)
cat(dist)

