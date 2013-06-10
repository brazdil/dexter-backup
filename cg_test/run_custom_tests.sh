#!/bin/bash

for testcase in `ls -d */` ; do
	if [ -e $testcase/Makefile ] ; then
		make -C $testcase/ "$@"
	fi
done
