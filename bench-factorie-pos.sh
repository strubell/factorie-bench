#!/bin/bash
#
#
#
MEMORY="4g"

#component="forwardposontonotes"
component=$1
testData=$2
numRuns=$3
logname=$component-test

# run the tests
for i in $(seq 1 $numRuns)
do
    cat $testData | nc localhost 3228
done

# parse the log
awk '{if ($1 == "Processed") {tok+=$2/$8; sent+=$5/$8; c+=1;}} END {print tok/c" tokens/sec"; print sent/c" sentences/sec"; print "averaged over "c" trials";}' $logname.log 
