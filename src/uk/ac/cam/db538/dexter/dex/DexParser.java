package uk.ac.cam.db538.dexter.dex;

import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;
import lombok.val;

import org.ow2.asmdex.ApplicationReader;
import org.ow2.asmdex.ApplicationVisitor;
import org.ow2.asmdex.ClassVisitor;
import org.ow2.asmdex.Opcodes;

public class DexParser {
	
	private static int ASM_API = Opcodes.ASM4;
	
	@Getter
	private ApplicationReader AppReader;  
	
	public DexParser(InputStream dex) throws DexParsingException {
		try {
			AppReader = new ApplicationReader(ASM_API, dex);
		} catch (IOException ex) {
			throw new DexParsingException(ex);
		}
	}
	
	public void parse() {
		AppReader.accept(new ApplicationVisitor(ASM_API) {

			@Override
			public ClassVisitor visitClass(int access, String name,
					String[] signature, String superName,
					String[] interfaces) {
				System.out.println(name);
				return super.visitClass(access, name, signature, superName, interfaces);
			}
			
		}, 0);
	}
}
