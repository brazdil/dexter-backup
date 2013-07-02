#!/bin/sh

java -ea -cp build/jar/dexter.jar:"lib/*" uk.ac.cam.db538.dexter.MainConsole $@
