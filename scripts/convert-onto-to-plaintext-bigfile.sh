#!/bin/bash
# First argument is directory to read files from
# Second argument is name of file to be greated, which will have the suffix SUFF
# Make sure the specified output file doesn't already exist!

SUFF=".stag"

if [ -f $2$SUFF ];
then
    echo "Specified output file '$2$SUFF' already exists. Exiting."
    exit
fi

for file in $1/*.pmd
do
    awk -vORS=" " '{print $2};' $file | sed 's/\[\]/\n/g' >> $2$SUFF
done
