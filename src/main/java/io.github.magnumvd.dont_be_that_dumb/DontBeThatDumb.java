package io.github.magnumvd.dont_be_that_dumb;

import net.minecraft.entity.passive.AnimalEntity;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DontBeThatDumb implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("DontBeThatDumb QuiltMod");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from DontBeThatDumb!", mod.metadata().name());
	}
}
