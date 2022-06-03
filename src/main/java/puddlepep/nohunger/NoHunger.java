package puddlepep.nohunger;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo
//	* make beds unsleepable, spawnpoint only.
//	* make the hunger effect actually something to be afraid of.
//	* general config stuff.

public class NoHunger implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("NoHunger");

	@Override
	public void onInitialize() {
		LOGGER.info("No Hunger initialized!");
	}
}
