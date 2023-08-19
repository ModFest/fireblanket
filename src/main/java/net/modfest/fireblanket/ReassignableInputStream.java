package net.modfest.fireblanket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReassignableInputStream extends InputStream {

	private InputStream delegate;
	
	public void setDelegate(InputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return delegate.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	@Override
	public int available() throws IOException {
		return delegate.available();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		delegate.reset();
	}

	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return delegate.transferTo(out);
	}
	

}
