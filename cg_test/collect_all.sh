#!/bin/bash

for testcase in `ls -d */` ; do
	if [ -e $testcase/Makefile ] ; then
		make -C $testcase/ clean test.apk
		cp $testcase/test.apk tests/`echo "$testcase" | sed 's/\///g'`.apk
	fi
done
