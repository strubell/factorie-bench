#!/bin/bash
# First argument is file to convert

SUFF=".txt"
file=$1

awk -vORS=" " '{print $1};' ../data/conll2003/eng.testa | sed 's/-DOCSTART-//g' | sed 's/  /\n/g' > $file$SUFF
