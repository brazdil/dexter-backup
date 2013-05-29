include path_config

JAVAC_FLAGS = -source 1.6 -target 1.6
CWD := $(shell cygpath -a -w . | sed 's/\\/\\\\/g')

diff: test.out test.apk_new.out
	-@diff --side-by-side -r $^

test.apk_new.apk : test.apk
	$(JAVA) -classpath "../bin/;../lib/*" uk.ac.cam.db538.dexter.MainConsole ../framework-2.3-lite $<

test.apk: classes.dex
	zip $@ $<

classes.dex: TestClass.class
	@rm -f $@ *.apk
	$(DX) --dex --verbose --output=$(CWD)\\$@ $(CWD)
	
%.class: %.java
	$(JAVAC) $(JAVAC_FLAGS) $<
	
%.out: %.apk
	rm -rf $@
	$(APKTOOL) d $< $@/
	
clean:
	rm -rf *.apk classes.dex *.class *.out

