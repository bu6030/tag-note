#!/bin/bash

# Simple script to download and extract Maven

echo "Downloading Maven..."
curl -O https://downloads.apache.org/maven/maven-3/3.9.0/binaries/apache-maven-3.9.0-bin.tar.gz

echo "Extracting Maven..."
tar -xzf apache-maven-3.9.0-bin.tar.gz

echo "Maven downloaded and extracted to apache-maven-3.9.0 directory"
echo "To use it, run: export PATH=\$PATH:\$(pwd)/apache-maven-3.9.0/bin"