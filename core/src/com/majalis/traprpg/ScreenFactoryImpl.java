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
	private final GameWorldFactory gameWorldFactory;
	private final EncounterFactory encounterFactory;
	private final BattleFactory battleFactory;
	private final SpriteBatch batch;
	private boolean loading;
	
	public ScreenFactoryImpl(Game game, AssetManager assetManager, SaveManager saveManager, GameWorldFactory gameWorldFactory, EncounterFactory encounterFactory, BattleFactory battleFactory, SpriteBatch batch) {
		this.game = game;
		this.assetManager = assetManager;
		this.saveService = saveManager;
		this.loadService = saveManager;
		this.gameWorldFactory = gameWorldFactory;
		this.encounterFactory = encounterFactory;
		this.battleFactory = battleFactory;
		this.batch = batch;
		loading = true;
	}

	@Override  
	public AbstractScreen getScreen(ScreenEnum screenRequest) {
		OrthographicCamera camera = new OrthographicCamera();
        FitViewport viewport =  new FitViewport(winWidth, winHeight, camera);
        BitmapFont font = new BitmapFont();
        ScreenElements elements = new ScreenElements(viewport, batch, font);
        PlayerCharacter character = loadService.loadDataValue("Player", PlayerCharacter.class);
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
				tempScreen = getEncounter(elements, character);
				if (tempScreen != null) return tempScreen;
				break; 
			case LOAD_GAME: 
				tempScreen = getGameScreen(elements, character);
				if (tempScreen != null) return tempScreen;
				break;
			case BATTLE:
				tempScreen = getBattle(elements, character);
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
	
	private AbstractScreen getEncounter(ScreenElements elements, PlayerCharacter character){
		if (getAssetCheck(EncounterScreen.resourceRequirements)){
			Integer encounterCode = loadService.loadDataValue("EncounterCode", Integer.class);
			return new EncounterScreen(this, elements, assetManager, saveService, encounterFactory.getEncounter(encounterCode, elements.getFont()));
		}
		else {
			return null;
		}
	}

	private AbstractScreen getBattle(ScreenElements elements, PlayerCharacter character){
		if (getAssetCheck(BattleScreen.resourceRequirements)){
			BattleCode battleCode = loadService.loadDataValue("BattleCode", BattleCode.class);
			return new BattleScreen(this, elements, saveService, battleFactory.getBattle(battleCode, character));
		}
		else {
			return null;
		}
	}
	
	private AbstractScreen getGameScreen(ScreenElements elements, PlayerCharacter character){
		SaveManager.GameContext context = loadService.loadDataValue("Context", SaveManager.GameContext.class);
		switch (context){
			case ENCOUNTER: return getEncounter(elements, character);
			case WORLD_MAP: 
				if (getAssetCheck(GameScreen.resourceRequirements)){
					return new GameScreen(this, elements, assetManager, saveService, gameWorldFactory.getGameWorld());
				}
				else return null;
			case BATTLE:
				return getBattle(elements, character);
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