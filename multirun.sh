#!/bin/bash
N="$1"
PACKAGE="$2"
ACTIVITY="$3"
APK="$4"

for i in $(seq 1 $N)
do
	echo "TRIAL #$i"

	echo " - uninstalling the package"
	adb uninstall $PACKAGE > /dev/null

	echo " - installing the APK"
	ADB_OUTPUT=`adb install "$APK" 2>/dev/null | tail -n 1`
	if [[ "$ADB_OUTPUT" != Success* ]]
	then
		echo "$ADB_OUTPUT"
		echo "ERROR: couldn't install APK"
		exit;
	fi

	echo " - launching the application"
	adb shell am start -n "$PACKAGE"/."$ACTIVITY"

	echo " - please press ENTER"
	read X
done
