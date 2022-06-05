package puddlepep.nohunger;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import puddlepep.nohunger.config.*;

// todo
//	* make beds unsleepable, spawnpoint only.

public class NoHunger implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("NoHunger");
	public static final Config CONFIG = ConfigHandler.loadConfig();

	@Override
	public void onInitialize() {
		LOGGER.info("No Hunger initialized!");
	}
}
