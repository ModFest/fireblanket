package net.modfest.fireblanket.config;

public record ConfigSpec<T>(String name, String prettyName, String description, String defaultValue, ParseFunc<T> parser) {
}
