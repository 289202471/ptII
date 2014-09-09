package org.terraswarm.gdp;
import com.sun.jna.Pointer;
import com.sun.jna.Union;
/**
 * <i>native declaration : /usr/include/bits/pthreadtypes.h:57</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class pthread_condattr_t extends Union {
	/** C type : char[4] */
	public byte[] __size = new byte[4];
	public int __align;
	public pthread_condattr_t() {
		super();
	}
	public pthread_condattr_t(int __align) {
		super();
		this.__align = __align;
		setType(Integer.TYPE);
	}
	/** @param __size C type : char[4] */
	public pthread_condattr_t(byte __size[]) {
		super();
		if ((__size.length != this.__size.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.__size = __size;
		setType(byte[].class);
	}
	public pthread_condattr_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends pthread_condattr_t implements com.sun.jna.Structure.ByReference {
		
	};
	public static class ByValue extends pthread_condattr_t implements com.sun.jna.Structure.ByValue {
		
	};
}
