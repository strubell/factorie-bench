#!/bin/bash
# First argument is directory to read files from
# Second argument is name of file to be greated, which will have the suffix SUFF
# Make sure the specified output file doesn't already exist!

SUFF=".txt"
file=$1

awk -vORS=" " '{print $2};' $file | sed 's/  /\n/g' > $file$SUFF
