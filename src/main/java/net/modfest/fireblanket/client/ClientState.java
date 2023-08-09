package net.modfest.fireblanket.client;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientState {
    // Block entities that are set to render a mask around them for identification
    public static final Set<Identifier> MASKED_BERS = new HashSet<>();
}
