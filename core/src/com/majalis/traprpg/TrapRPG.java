package com.majalis.traprpg;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
/*
 * Package shared entry point for each platform.  Generates a ScreenFactory and service for dependency injection, and switches to the splash screen for loading.
 */
public class TrapRPG extends Game {
	
	public void create() {	
		init(new ScreenFactoryImpl(this, new AssetManager(), new SaveManager(false)));
	}
	
	public void init(ScreenFactory factory){
		// this is to define access to the factory methods in DependencyContainer
		AbstractScreen screen = factory.getScreen(ScreenEnum.MAIN_MENU);
		screen.buildStage();
		setScreen(screen);
	}
	
}