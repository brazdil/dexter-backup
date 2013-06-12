TOP := $(dir $(lastword $(MAKEFILE_LIST)))
include $(TOP)/path_config

JAVAC_FLAGS = -source 1.6 -target 1.6
CWD := $(shell cygpath -a -m .)
PROJECET_ROOT := $(shell cygpath -a -m $(TOP)/../)
DEXTER := $(JAVA) -ea -classpath '$(PROJECET_ROOT)/bin/;$(PROJECET_ROOT)/lib/*' uk.ac.cam.db538.dexter.MainConsole $(PROJECET_ROOT)/framework-2.3-lite
DXFLAGS := --no-optimize

## This is for home-made tests
TESTNAME := $(shell basename $(CWD))
MAINCLASS_NAME := TestClass
TESTRESULT := expected.txt
ifeq ($(TESTNAME),src)
  ## This is for AOSP tests.
  TESTNAME := $(shell basename $(abspath $(CWD)/../))
  MAINCLASS_NAME := Main
  TESTRESULT := ../expected.txt
endif 

JAVA_FILES := $(shell find . -name '*.java')
CLASS_FILES := $(JAVA_FILES:.java=.class)

diff: test.out test.apk_new.out
	-@diff --side-by-side -r $^

test.apk_new.apk : test.apk
	$(DEXTER) $<

test.apk: classes.dex
	zip $@ $<

classes.dex: $(CLASS_FILES)
	@rm -f $@ *.apk
	$(DX) --dex $(DXFLAGS) --output=$(CWD)\\$@ $(CWD)
	
%.class: %.java
	$(JAVAC) $(JAVAC_FLAGS) $<
	
%.out: %.apk
	rm -rf $@
	$(APKTOOL) d $< $@/
	
clean:
	@rm -rf *.apk classes.dex *.out
	@find . -name '*.class' -exec rm {} \;

## Unit testing.
run: test.apk_new.apk
	@echo Running test
	@adb push $< /sdcard/ 2>/dev/null
	@adb shell ANDROID_DATA=/sdcard dalvikvm -ea -cp /sdcard/$< $(MAINCLASS_NAME) > output.txt
	@diff -b output.txt $(TESTRESULT) >/dev/null || (echo '***FAILURE***' && diff -y -b output.txt $(TESTRESULT))
	
run0: test.apk
	@echo Running test
	@adb push $< /sdcard/ 2>/dev/null
	@adb shell ANDROID_DATA=/sdcard dalvikvm -ea -cp /sdcard/$< $(MAINCLASS_NAME)
	
## Prepare code for eclipse debugging
debug: test.apk test.out
	@cp $< $(TOP)
	
collect: test.apk
	@cp $< $(TOP)/tests/$(TESTNAME).apk

dump: classes.dex
	$(DX) --dex --verbose-dump  $(CWD)/$<
