package net.modfest.fireblanket.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigParsers {
	public static final ParseFunc<List<String>> STRING_LIST = in -> {
		return Arrays.asList(in.split(","));
	};

	public static final ParseFunc<String> STRING = in -> in;

	public static final ParseFunc<Integer> INTEGER = in -> {
		try {
			return Integer.parseInt(in);
		} catch (Exception e) {
			throw new ParseException("Unknown integer value: " + in);
		}
	};

	public static final ParseFunc<Boolean> BOOLEAN = in -> {
		if ("yes".equals(in) || "true".equals(in)) {
			return true;
		}

		if ("no".equals(in) || "false".equals(in)) {
			return false;
		}

		throw new ParseException("Unknown boolean value: " + in);
	};
}
