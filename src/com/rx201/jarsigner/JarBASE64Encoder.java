package com.rx201.jarsigner;

import java.io.IOException;
import java.io.OutputStream;

import sun.misc.BASE64Encoder;

/**
 * This is a BASE64Encoder that does not insert a default newline at the end of
 * every output line. This is necessary because java.util.jar does its own
 * line management (see Manifest.make72Safe()). Inserting additional new lines
 * can cause line-wrapping problems (see CR 6219522).
 */
class JarBASE64Encoder extends BASE64Encoder {
    /**
     * Encode the suffix that ends every output line.
     */
    protected void encodeLineSuffix(OutputStream aStream) throws IOException { }
}