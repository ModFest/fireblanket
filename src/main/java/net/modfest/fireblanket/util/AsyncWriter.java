package net.modfest.fireblanket.util;

import com.mojang.logging.LogUtils;
import org.apache.commons.io.function.IOSupplier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Asynchronous writer with minimal internal synchronization.
 *
 * @author Ampflower
 **/
public final class AsyncWriter extends Writer implements Thread.UncaughtExceptionHandler {
	private static final Logger logger = LogUtils.getLogger();

	private static final VarHandle exception;
	private static final IOException closedSentinel = new IOException("closed");

	private static final Set<AsyncWriter> knownWriters = Collections.synchronizedSet(new HashSet<>());

	static {
		closedSentinel.setStackTrace(new StackTraceElement[0]);
		try {
			final MethodHandles.Lookup lookup = MethodHandles.lookup();
			exception = lookup.findVarHandle(AsyncWriter.class, "$exception", IOException.class);
		} catch (ReflectiveOperationException e) {
			logger.error("Cannot load VarHandles", e);
			throw new LinkageError("Cannot load VarHandles", e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// logger may have been shut down by this point, manually use stderr
			final PrintStream err = new PrintStream(new FileOutputStream(FileDescriptor.err));

			for (final AsyncWriter writer : knownWriters) {
				try {
					writer.stop(err);
				} catch (Throwable e) {
					e.printStackTrace(err);
				}
			}
		}));
	}

	private final Worker thread;
	private final IOSupplier<Writer> supplier;
	// push-back support for logging reasons
	// Perhaps the LinkedBlocQueue or a similar data structure would make more sense here.
	// Realistically, the queue only needs to support pushback for error support, not be double-linked.
	private final Deque<Message> messages = new ConcurrentLinkedDeque<>();
	@SuppressWarnings("unused")
	private volatile IOException $exception;

	private AsyncWriter(final IOSupplier<Writer> supplier) {
		this.supplier = supplier;
		this.thread = new Worker();
		this.thread.start();

		// Shutdown hook
		knownWriters.add(this);
	}

	public static AsyncWriter of(final Writer writer) throws IOException {
		// Test if closed or broken
		writer.flush();

		return new AsyncWriter(() -> writer);
	}

	public static AsyncWriter bufferedWriter(final Path path) throws IOException {
		// Make sure we can actually create the thing.
		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}

		if (!Files.isWritable(path)) {
			throw new IOException("unwritable: " + path);
		}

		return new AsyncWriter(() -> Files.newBufferedWriter(
			path,
			StandardCharsets.UTF_8,
			StandardOpenOption.CREATE,
			StandardOpenOption.APPEND
		));
	}

	private IOException exception() {
		return (IOException) exception.getAcquire(this);
	}

	private void check() throws IOException {
		final IOException e = this.exception();
		if (e != null) {
			throw new IOException(e);
		}
	}

	private void write(Message message) throws IOException {
		check();
		if (!this.messages.offer(message)) {
			throw new IOException();
		}
		LockSupport.unpark(this.thread);
	}


	private void write(Buffer buffer) throws IOException {
		this.write(new Message(buffer));
	}

	@Override
	public void write(@NotNull final char[] cbuf, final int off, final int len) throws IOException {
		this.write(new Chars(cbuf, off, len));
	}

	@Override
	public void write(final int c) throws IOException {
		this.write(new Chars(new char[]{(char) c}, 0, 1));
	}

	@Override
	public void write(@NotNull final String str) throws IOException {
		this.write(new Str(str));
	}

	@Override
	public void write(@NotNull final char[] cbuf) throws IOException {
		this.write(new Chars(cbuf));
	}

	@Override
	public void write(@NotNull final String str, final int off, final int len) throws IOException {
		this.write(new Str(str, off, len));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote Flush is handled asynchronously. It is only guaranteed
	 * 	that the call order from the same thread is consistent and that
	 * 	a flush will be executed with the current contents.
	 */
	@Override
	public void flush() throws IOException {
		this.write(new Message(Request.FLUSH));
	}

	@Override
	public void close() throws IOException {
		final IOException e = (IOException) exception.compareAndExchange(this, null, closedSentinel);
		if (e == closedSentinel) {
			return;
		}
		if (e != null) {
			throw new IOException("Failed close stream", e);
		}

		// Normally, the internal write function is sufficient.
		// However, we intrude an exception right before,
		// not allowing the internal write function to complete normally.
		if (!this.messages.offer(new Message(Request.CLOSE))) {
			throw new IOException("Deque rejected close message");
		}

		LockSupport.unpark(this.thread);

		try {
			// TODO: Figure out if this is actually necessary
			//  or if it can complete immediately?
			this.thread.join();
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}

		// Because if we join, we can at least throw the finished result if it isn't the sentinel.
		final IOException result = exception();
		if (result != null && result != closedSentinel) {
			throw new IOException(result);
		}
	}

	/**
	 * Attempts to safely shut down and stop the writer and its thread.
	 */
	private void stop(final PrintStream err) throws Throwable {
		Throwable thrown = null;
		try {
			this.close();
		} catch (Throwable t) {
			thrown = t;
		}

		// drive the message queue if the thread spontaneously died
		if (!this.thread.isAlive() && !this.messages.isEmpty()) {
			err.println("Thread missing");
			try {
				//noinspection CallToThreadRun - we can't start a dead thread
				this.thread.run();
			} catch (Throwable t) {
				err.println("Uncaught exception from writer");
				t.printStackTrace(err);

				if (thrown != null) {
					thrown.addSuppressed(t);
				}

				this.uncaughtException(this.thread, t);
			}
		} else {
			try {
				this.thread.join(1000);
			} catch (InterruptedException e) {
				err.println("Thread failed to join");
				e.printStackTrace(err);

				if (thrown != null) {
					thrown.addSuppressed(e);
				}
			}
		}

		if (thrown != null) {
			throw thrown;
		}
	}

	// Over-engineered async exception handling
	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		if (this.thread != t) {
			logger.warn("We don't own {}, but it threw an exception; please don't use {} for arbitrary threads", t, this, e);
			return;
		}

		final IOException toSet = new IOException("Uncaught exception", e);

		IOException witness = (IOException) exception.compareAndExchange(this, null, toSet);

		// If it is the closed sentinel, replace it.
		if (witness == closedSentinel) {
			witness = (IOException) exception.compareAndExchange(this, closedSentinel, toSet);
		}

		// note: don't modify the sentinel
		if (witness != null && witness != closedSentinel) {
			witness.addSuppressed(toSet);
		}

		logger.error("{} of {} threw an uncaught exception", t, this, e);

		Message message;
		while ((message = this.messages.poll()) != null) {
			logger.error("Unfinished write: {}", message);
		}

		knownWriters.remove(this);
	}

	private class Worker extends Thread {
		private Writer writer;

		private Worker() {
			super("AsyncWorker @ " + AsyncWriter.this);
			this.setUncaughtExceptionHandler(AsyncWriter.this);
		}

		@Override
		public void run() {
			while (exception() == null || !messages.isEmpty()) {
				try {
					if (processMessages()) {
						break;
					}

					// Flush after the queue has finished
					this.writer.flush();
				} catch (IOException e) {
					try (final Writer ignored = this.writer) {
						uncaughtException(this, e);
					} catch (IOException ioClose) {
						throw new IOError(ioClose);
					}
					break;
				}

				// Periodically check every 15 seconds, in case there was a bizarre race condition
				LockSupport.parkNanos(messages, TimeUnit.SECONDS.toNanos(15));

				// We frankly don't care if we get interrupted.
				// This only exists to prevent loops from weird implementations.
				if (Thread.interrupted()) {
					logger.trace("Why did you wake me up?");
				}
			}

			logger.info("Exited.");
			knownWriters.remove(AsyncWriter.this);
		}

		private Writer writer() throws IOException {
			if (this.writer == null) {
				this.writer = supplier.get();
			}
			return this.writer;
		}

		private boolean processMessages() throws IOException {
			IOException io = null;
			int fault = 0;
			Writer writer = writer();
			Message message;

			while ((message = messages.poll()) != null) {
				try {
					if (message.buf() != null) {
						message.buf().writeTo(writer);
					}

					message.request().action(writer);

					if (message.request() == Request.CLOSE) {
						if (!messages.isEmpty()) {
							logger.warn("Unwritten messages: {}", message);
						}
						return true;
					}
				} catch (IOException e) {
					// Retry
					messages.push(message);

					// Make a suppression chain
					if (io != null) {
						e.addSuppressed(io);
					}

					if (fault++ > 3) {
						throw e;
					}

					io = e;

					// Try a new writer
					this.writer = supplier.get();
					if (this.writer == writer) {
						// Non-recoverable error; rethrow.
						throw e;
					}
					writer = this.writer;
				} catch (Throwable t) {
					// Log failed messages
					messages.push(message);

					// Suppress io if one was thrown,
					// we probably had memory corruption if we get here
					if (io != null) {
						t.addSuppressed(io);
					}

					// Non-recoverable error; rethrow.
					throw t;
				}
			}

			if (io != null) {
				logger.warn("{} from {} threw an exception", writer, this, io);
			}

			return false;
		}
	}

	private enum Request {
		WRITE {
			@Override
			void action(final Writer writer) throws IOException {
				// no-op
				logger.info("WRITE {}", writer);
			}
		},
		FLUSH {
			@Override
			void action(final Writer writer) throws IOException {
				writer.flush();
				logger.info("FLUSH {}", writer);
			}
		},
		CLOSE {
			@Override
			void action(final Writer writer) throws IOException {
				// Force-flush
				try (writer) {
					writer.flush();
				}
				logger.info("CLOSE {}", writer);
			}
		},
		;

		abstract void action(Writer writer) throws IOException;
	}

	private record Message(Buffer buf, Request request) {
		private Message(Buffer buf) {
			this(buf, Request.WRITE);
		}

		private Message(Request request) {
			this(null, request);
		}
	}

	private sealed interface Buffer {
		char[] chars();

		int offset();

		int length();

		void writeTo(Writer writer) throws IOException;
	}

	private record Chars(char[] chars, int offset, int length) implements Buffer {
		private Chars(char[] chars) {
			this(chars, 0, chars.length);
		}

		@Override
		public void writeTo(final Writer writer) throws IOException {
			writer.write(chars, offset, length);
		}

		@Override
		public String toString() {
			if (chars == null) {
				return "NULL BUFFER: [" + offset + ":" + length + "]";
			}

			if (offset > 0 && length > 0 && offset + length <= chars.length) {
				return String.valueOf(chars, offset, length);
			}

			return "INVALID BUFFER: " + String.valueOf(chars) + "[" + offset + ":" + length + "]";
		}
	}

	private record Str(String str, int offset, int length) implements Buffer {
		private Str(String str) {
			this(str, 0, str.length());
		}

		@Override
		public char[] chars() {
			return str.toCharArray();
		}

		@Override
		public void writeTo(final Writer writer) throws IOException {
			writer.write(str, offset, length);
		}

		@Override
		public String toString() {
			if (str == null) {
				return "NULL BUFFER: [" + offset + ":" + length + "]";
			}

			if (offset > 0 && length > 0 && offset + length <= str.length()) {
				return str.substring(offset, offset + length);
			}

			return "INVALID BUFFER: " + str + "[" + offset + ":" + length + "]";
		}
	}
}
