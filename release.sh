#!/bin/bash

if [[ $# -ne 2 ]] ; then
    echo 'Usage: ./release.sh target_version next_version'
    exit 1
fi

git checkout master || { echo 'Master checkout failed' ; exit 2; }
git pull || { echo 'Master pull failed' ; exit 2; }
mvn versions:set -DnewVersion=$1 || { echo 'Version set failed' ; exit 2; }
find . -name "*.versionsBackup" -type f -delete
git commit -am "[Release] Release $1"
git tag $1
mvn clean install source:jar deploy || { echo 'Maven command failed' ; exit 2; }
mvn versions:set -DnewVersion=$2 || { echo 'Version set failed' ; exit 2; }
find . -name "*.versionsBackup" -type f -delete
git commit -am "[Release] Back to SNAPSHOT"
git push origin master
git push origin $1
