package com.majalis.traprpg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
/*
 * ScreenFactory implementation to generate and cache screens.
 */
public class ScreenFactoryImpl implements ScreenFactory {

	private static final int winWidth = 1280;
	private static final int winHeight = 720;
	private final Game game;
	private final AssetManager assetManager;
	private final SaveService saveService;
	private final LoadService loadService;	
	private final GameWorldManager gameWorldManager;
	private final EncounterFactory encounterFactory;
	private final SpriteBatch batch;
	private boolean loading;
	private int currentEncounterCode;
	
	public ScreenFactoryImpl(Game game, AssetManager assetManager, SaveManager saveManager, GameWorldManager gameWorldManager, EncounterFactory encounterFactory, SpriteBatch batch) {
		this.game = game;
		this.assetManager = assetManager;
		this.saveService = saveManager;
		this.loadService = saveManager;
		this.gameWorldManager = gameWorldManager;
		this.encounterFactory = encounterFactory;
		this.batch = batch;
		loading = true;
		currentEncounterCode = loadService.loadDataValue("EncounterCode", Integer.class);
	}

	@Override  
	public AbstractScreen getScreen(ScreenEnum screenRequest) {
		// this needs to be moved
		GameWorldManager.GameContext context = loadService.loadDataValue("Context", GameWorldManager.GameContext.class);
		gameWorldManager.setContext(context);
		currentEncounterCode = loadService.loadDataValue("EncounterCode", Integer.class);
		// this needs to  be moved
		
		OrthographicCamera camera = new OrthographicCamera();
        FitViewport viewport =  new FitViewport(winWidth, winHeight, camera);
        BitmapFont font = new BitmapFont();
        ScreenElements elements = new ScreenElements(viewport, batch, font);
        
		AbstractScreen tempScreen;
		switch(screenRequest){
			case SPLASH: 
				return new SplashScreen(this, elements, assetManager, 50);
			case MAIN_MENU: 
				if (getAssetCheck(MainMenuScreen.resourceRequirements)){
					return new MainMenuScreen(this, elements, assetManager, saveService, loadService); 
				}
				break;
			case NEW_GAME:	
			case ENCOUNTER: 
				tempScreen = getEncounter(elements);
				if (tempScreen != null) return tempScreen;
				break; 
			case LOAD_GAME: 
				tempScreen = getGameScreen(elements);
				if (tempScreen != null) return tempScreen;
				break;
			case GAME_OVER:				
				if (getAssetCheck(GameOverScreen.resourceRequirements)){
					return new GameOverScreen(this, elements, assetManager);
				}
				break;
			case OPTIONS: 	return new OptionScreen(this, elements);
			case REPLAY: 	return new ReplayScreen(this, elements);
			case EXIT: 		return new ExitScreen(this, elements);
		}
		loading = true;
		return new LoadScreen(this, elements, assetManager, screenRequest);
	}

	private boolean getAssetCheck(ObjectMap<String, Class<?>> pathToType){
		// if the loading screen has just loaded the assets, don't perform the checks or increment the reference counts
		if (loading){
			loading = false;
			return true;
		}
		// if screens are being switched but no assets need to be loaded, don't call the loading screen
		boolean assetsLoaded = true;
		for (String path: pathToType.keys()){
			if (!assetManager.isLoaded(path)){
				assetsLoaded = false;
			}
			assetManager.load(path, pathToType.get(path));
		}
		return assetsLoaded;
	}
	
	private AbstractScreen getEncounter(ScreenElements elements){
		if (getAssetCheck(EncounterScreen.resourceRequirements)){
			saveService.saveDataValue("EncounterCode", currentEncounterCode + 1);
			return new EncounterScreen(this, elements, assetManager, saveService, encounterFactory.getEncounter(currentEncounterCode++, elements.getFont()));
		}
		else {
			return null;
		}
	}
	
	private AbstractScreen getGameScreen(ScreenElements elements){
		switch (gameWorldManager.getGameContext()){
			case ENCOUNTER: return getEncounter(elements);
			case WORLD_MAP: 
				if (getAssetCheck(GameScreen.resourceRequirements)){
					return new GameScreen(this, elements, assetManager, saveService, gameWorldManager.getGameWorld(elements.getFont()));
				}
				else return null;
			default: return null;
		}	
	}
	
	@Override
	public Game getGame() {
		return game;
	}
	
	private class ExitScreen extends AbstractScreen{
		protected ExitScreen(ScreenFactory factory, ScreenElements elements) {
			super(factory, elements);
		}
		@Override
		public void buildStage() {
			Gdx.app.exit();
		}
	}
}