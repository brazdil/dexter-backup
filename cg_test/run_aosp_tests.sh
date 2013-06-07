#!/bin/bash

set -e 

target=$1
if [ -z "$target" ] ; then
	target=collect
fi

if [ "$target" = "run" ] ; then
	target=test.apk_new.apk
fi


for i in aosp-tests/*; do
    if [ -d "$i" -a -r "$i" -a -r "${i}/info.txt" -a -r "${i}/src/" ]; then
		testname=`basename $i |  sed 's/\///g'`
		echo '>>>>>>>>>>>>' $testname
		if [ ! -e $i/src/Makefile ] ; then
			echo 'include ../../../Makefile' > $i/src/Makefile
		fi
		
		if [ "$target" = "collect" ] ; then
			make -C $i/src/ clean test.apk
			cp $i/src/test.apk tests/$testname.apk
		else
			make -C $i/src/ clean $target
		fi
	fi
done
