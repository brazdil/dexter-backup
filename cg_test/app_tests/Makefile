CORPUS_DIR := /bigdisc/android-apps/benign
TOP :=/home/rubin/workspace/dexter/cg_test
include $(TOP)/path_config

JAVAC_FLAGS := -source 1.5 -target 1.5

ifeq ($(shell uname -o), Cygwin)
    CWD := $(shell cygpath -a -m .)
    PROJECET_ROOT := $(shell cygpath -a -m $(TOP)/../)
else
    CWD := $(shell realpath .)
    PROJECET_ROOT := $(shell realpath  $(TOP)/../)
endif

DEXTER := $(JAVA) -ea -classpath '$(PROJECET_ROOT)/bin/:$(PROJECET_ROOT)/lib/*' uk.ac.cam.db538.dexter.MainTest $(PROJECET_ROOT)/framework-2.3

APP_FILES := $(notdir $(wildcard $(CORPUS_DIR)/*.apk))
OUT_FILES := $(APP_FILES:.apk=.out)

all: $(OUT_FILES)

%.out:
	-timeout 10m $(DEXTER) $(CORPUS_DIR)/$(@:.out=.apk) $(@:.out=.apk) 2>$(@:.out=.err) | tee $@ 
	-[ ! -s $(@:.out=.err) ] && rm -rf $(@:.out=.err)
	-cat $(@:.out=.err)
clean: 
	rm -f *.apk *.out *.err
	
