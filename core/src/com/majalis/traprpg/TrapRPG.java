package com.majalis.traprpg;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
/*
 * Package shared entry point for each platform.  Generates a ScreenFactory and service for dependency injection, and switches to the splash screen for loading.
 */
public class TrapRPG extends Game {
	
	public void create() {	
		SaveManager saveManager = new SaveManager(false);
		AssetManager assetManager = new AssetManager();
		EncounterFactory encounterFactory = new EncounterFactory(assetManager, saveManager);
		init(new ScreenFactoryImpl(this, assetManager, saveManager, new GameWorldManager(encounterFactory), encounterFactory, new SpriteBatch()));
	}
	/*
	 * Takes a factory implementation and uses it to generate a screen and switch to it
	 */
	public void init(ScreenFactory factory){
		AbstractScreen screen = factory.getScreen(ScreenEnum.SPLASH);
		screen.buildStage();
		setScreen(screen);
	}
}