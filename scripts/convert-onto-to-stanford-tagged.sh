#!/bin/bash

SUFF=".stag"

for file in $1/*.pmd
do
    awk -vORS=" " '{print $2"_"$5};' $file | sed 's/ _ /'$'\n/g' > $file$SUFF
done
