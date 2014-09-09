package org.terraswarm.gdp;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
/**
 * Description of data base entry for a single service.<br>
 * <i>native declaration : /usr/include/netdb.h:213</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class protoent extends Structure {
	/**
	 * Official protocol name.<br>
	 * C type : char*
	 */
	public Pointer p_name;
	/**
	 * Alias list.<br>
	 * C type : char**
	 */
	public PointerByReference p_aliases;
	/** Protocol number. */
	public int p_proto;
	public protoent() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("p_name", "p_aliases", "p_proto");
	}
	/**
	 * @param p_name Official protocol name.<br>
	 * C type : char*<br>
	 * @param p_aliases Alias list.<br>
	 * C type : char**<br>
	 * @param p_proto Protocol number.
	 */
	public protoent(Pointer p_name, PointerByReference p_aliases, int p_proto) {
		super();
		this.p_name = p_name;
		this.p_aliases = p_aliases;
		this.p_proto = p_proto;
	}
	public protoent(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends protoent implements Structure.ByReference {
		
	};
	public static class ByValue extends protoent implements Structure.ByValue {
		
	};
}
