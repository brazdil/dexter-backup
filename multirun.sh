#!/bin/bash
N="$1"
PACKAGE="$2"
ACTIVITY="$3"
APK_ORIGINAL="$4"
APK_INSTRUMENTED="$APK_ORIGINAL.dexter"

for i in $(seq 1 $N)
do
	echo "TRIAL #$i"

	echo " - uninstalling the package"
	adb uninstall $PACKAGE > /dev/null

	echo " - installing the original APK"
	ADB_OUTPUT=`adb install "$APK_ORIGINAL" 2>/dev/null | tail -n 1`
	if [[ "$ADB_OUTPUT" != Success* ]]
	then
		echo "$ADB_OUTPUT"
		echo "ERROR: couldn't install APK"
		exit;
	fi

	echo " - clearing the system log"
	adb logcat -c

	echo " - launching the application"
	adb shell am start -n "$PACKAGE"/."$ACTIVITY"

#	echo " - please press ENTER"
#	read X

	echo " - waiting 5 seconds..."
	sleep 5

	echo " - saving logcat"
	adb logcat -d > "multirun-$i-original.logcat"

	echo " - uninstalling the package"
	adb uninstall $PACKAGE > /dev/null

	echo " - installing the instrumented APK"
	ADB_OUTPUT=`adb install "$APK_INSTRUMENTED" 2>/dev/null | tail -n 1`
	if [[ "$ADB_OUTPUT" != Success* ]]
	then
		echo "$ADB_OUTPUT"
		echo "ERROR: couldn't install APK"
		exit;
	fi

	echo " - clearing the system log"
	adb logcat -c

	echo " - launching the application"
	adb shell am start -n "$PACKAGE"/."$ACTIVITY"

#	echo " - please press ENTER"
#	read X

	echo " - waiting 5 seconds..."
	sleep 5

	echo " - saving logcat"
	adb logcat -d > "multirun-$i-instrumented.logcat"
done
