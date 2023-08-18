package net.modfest.fireblanket.client;

import com.mojang.blaze3d.systems.RenderCall;

public class BlamefulRenderCall implements RenderCall {

	private final RenderCall delegate;
	private final Throwable stacktrace;
	
	public BlamefulRenderCall(RenderCall delegate) {
		this.delegate = delegate;
		this.stacktrace = new Throwable("Created here in thread "+Thread.currentThread().getName());
	}
	
	@Override
	public void execute() {
		try {
			delegate.execute();
		} catch (Error | RuntimeException e) {
			e.addSuppressed(stacktrace);
			throw e;
		}
	}
	
}
