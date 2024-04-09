package rbasamoyai.ogden;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class OgdenConfigs {

	public static class Server {
		public final ForgeConfigSpec.BooleanValue projectilesCanLoadChunks;

		Server(ForgeConfigSpec.Builder builder) {
			builder.comment("Server configuration settings for Ogden").push("server");

			projectilesCanLoadChunks = builder
					.comment("If Ogden projectiles can forceload chunks. Only generated chunks are forceloaded.")
					.translation("ogden.configgui.projectilesCanLoadChunks")
					.worldRestart()
                    .define("projectilesCanLoadChunks", true);

			builder.pop();
		}
	}

	private static final ForgeConfigSpec serverSpec;
	private static final Server SERVER;
	static {
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}
	public static Server server() { return SERVER; }
	public static void registerConfigs() {
        ModLoadingContext mlContext = ModLoadingContext.get();
		mlContext.registerConfig(ModConfig.Type.SERVER, serverSpec);
	}

	public static void onModConfigLoad(ModConfig modConfig) {

	}

	public static void onModConfigReload(ModConfig modConfig) {
	}

}
