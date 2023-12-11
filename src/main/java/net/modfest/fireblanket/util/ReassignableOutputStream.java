package net.modfest.fireblanket.util;

import java.io.IOException;
import java.io.OutputStream;

public class ReassignableOutputStream extends OutputStream {

	private OutputStream delegate = OutputStream.nullOutputStream();
	
	public void setDelegate(OutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
	
}
