#!/bin/bash

set -e 

target=$1
if [ -z "$target" ] ; then
	target=collect
fi

if [ "$target" = "compile" ] ; then
	target=test.apk_new.apk
fi

target_if_fail=$2
if [ -z "$target_if_fail" ] ; then
	target_if_fail=debug
fi

for i in aosp-tests/*; do
    if [ -d "$i" -a -r "$i" -a -r "${i}/info.txt" -a -r "${i}/src/" ]; then
		testname=`basename $i |  sed 's/\///g'`
		echo '>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'
		echo '========' $testname
		echo '>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'
		if [ ! -e $i/src/Makefile ] ; then
			echo 'include ../../../Makefile' > $i/src/Makefile
		fi
		
		if [ "$target" = "collect" ] ; then
			make -C $i/src/ test.apk
			cp $i/src/test.apk tests/$testname.apk
		else
			set +e
			make -C $i/src/ $target
			if [ ! "$?" = "0" ] ; then 
				echo Copying to debug..
				make -C $i/src/ $target_if_fail
				#exit 1
			fi
			set -e
		fi
	fi
done
