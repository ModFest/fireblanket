package dev.gegy.roles.api;

import java.util.stream.Stream;

/**
 * @author Ampflower
 **/
@Deprecated
public interface RoleReader extends Iterable<Role> {
	Stream<Role> stream();
}
