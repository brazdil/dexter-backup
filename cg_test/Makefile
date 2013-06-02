TOP := $(dir $(lastword $(MAKEFILE_LIST)))
include $(TOP)/path_config

JAVAC_FLAGS = -source 1.6 -target 1.6
CWD := $(shell cygpath -a -m .)
PROJECET_ROOT := $(shell cygpath -a -m $(TOP)/../)

MAINCLASS_NAME := TestClass

diff: test.out test.apk_new.out
	-@diff --side-by-side -r $^

test.apk_new.apk : test.apk
	$(JAVA) -ea -classpath "$(PROJECET_ROOT)/bin/;$(PROJECET_ROOT)/lib/*" uk.ac.cam.db538.dexter.MainConsole $(PROJECET_ROOT)/framework-2.3-lite $<

test.apk: classes.dex
	zip $@ $<

classes.dex: $(MAINCLASS_NAME).class
	@rm -f $@ *.apk
	$(DX) --dex --verbose --output=$(CWD)\\$@ $(CWD)
	
%.class: %.java
	$(JAVAC) $(JAVAC_FLAGS) $<
	
%.out: %.apk
	rm -rf $@
	$(APKTOOL) d $< $@/
	
clean:
	rm -rf *.apk classes.dex *.class *.out

## Unit testing.
run: test.apk_new.apk
	@echo Running test
	@adb push $< /sdcard/
	@adb shell ANDROID_DATA=/sdcard dalvikvm -ea -cp /sdcard/$< $(MAINCLASS_NAME)
	
run0: test.apk
	@echo Running test
	@adb push $< /sdcard/ 2>/dev/null
	@adb shell ANDROID_DATA=/sdcard dalvikvm -ea -cp /sdcard/$< $(MAINCLASS_NAME)
	
## Prepare code for eclipse debugging
debug: test.apk test.out
	@cp $< $(TOP)
	
dump: classes.dex
	$(DX) --dex --verbose-dump  $(CWD)/$<