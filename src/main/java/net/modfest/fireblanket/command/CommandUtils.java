package net.modfest.fireblanket.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.text.Text;

public final class CommandUtils {
	public static final DynamicCommandExceptionType GENERIC_EXCEPTION = new DynamicCommandExceptionType(message -> (Text)message);
}
