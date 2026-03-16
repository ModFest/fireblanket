package net.modfest.fireblanket.util;

import java.io.IOException;

/**
 * @author Ampflower
 */
public interface IOUnaryOperation<T> {
	T apply(T t) throws IOException;
}
