package net.modfest.fireblanket.config;

@FunctionalInterface
public interface ParseFunc<T> {
	T parse(String input);
}
