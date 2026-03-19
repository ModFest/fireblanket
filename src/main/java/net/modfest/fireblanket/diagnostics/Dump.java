package net.modfest.fireblanket.diagnostics;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.io.HexDump;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * {@link ByteBuf} hex dumpers.
 *
 * @author Ampflower
 * @see net.modfest.fireblanket.mixin.diagnostics.net.MixinPacketDecoder
 * @see net.modfest.fireblanket.mixin.diagnostics.net.MixinPacketEncoder
 * @see net.modfest.fireblanket.mixin.diagnostics.net.MixinCompressionDecoder
 **/
public final class Dump {
	private static final Logger logger = LogUtils.getLogger();

	public static void buf(final ByteBuf buf, final String format, final Object... args) {
		try (final var output = new ByteArrayOutputStream(buf.readableBytes())) {
			final ByteBuf slice = buf.slice();
			final byte[] bytes = new byte[slice.readableBytes()];
			slice.readBytes(bytes);

			HexDump.dump(bytes, 0, output, 0);

			logger.warn("{} ~ Dumped\n{}", String.format(format, args), output);
		} catch (Exception e) {
			logger.warn("{} ~ Failed to dump buf: {}", String.format(format, args), buf, e);
		}
	}

	public static void crashedBuf(final ByteBuf buf, final Throwable t, final String format, final Object... args) {
		logger.warn("!@!@!@ CRASH: \"{}\"", tryToString(buf), t);

		buf(buf, format, args);
	}

	private static String tryToString(ByteBuf buf) {
		try {
			return buf.toString(StandardCharsets.US_ASCII);
		} catch (Exception e) {
			logger.warn("Failed to dump buf: {}", buf, e);
		}
		return "!@!@!@ Bad buf: " + buf;
	}
}
