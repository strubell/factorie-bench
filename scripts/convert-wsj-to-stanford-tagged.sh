#!/bin/bash

SUFF=".stag"

for file in $1/*.dep.2
do
    awk -vORS=" " '{print $2"_"$4};' $file | sed 's/ _ /\n/g' > $file$SUFF
done
