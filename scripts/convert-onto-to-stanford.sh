#!/bin/bash

for file in $1/*
do
    awk '{print $2};' $file > $file.s
done
