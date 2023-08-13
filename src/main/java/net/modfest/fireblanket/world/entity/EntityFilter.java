package net.modfest.fireblanket.world.entity;

import java.util.regex.Pattern;

public record EntityFilter(Pattern pattern, int trackingRangeChunks, int tickRate, boolean forceNoVelcityUpdate) {
}
