JAVAC = javac
JAVAC_FLAGS = -source 1.6 -target 1.6
DX = /cygdrive/d/android-sdks/build-tools/17.0.0/dx.bat
CWD := $(shell cygpath -a -w . | sed 's/\\/\\\\/g')

test.apk: classes.dex
	zip $@ $^

classes.dex: TestClass.class
	$(DX) --dex --verbose --output=$(CWD)\\$@ $(CWD)
	
%.class: %.java
	$(JAVAC) $(JAVAC_FLAGS) $<
	
clean:
	rm -f *.apk classes.dex *.class
	