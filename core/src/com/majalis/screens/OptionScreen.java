package com.majalis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.majalis.asset.AssetEnum;
import com.majalis.talesofandrogyny.TalesOfAndrogyny;
/*
 * The options/configuration screen.  UI that handles player input to save Preferences to a player's file system.
 */
public class OptionScreen extends AbstractScreen {
	
	public static final Array<AssetDescriptor<?>> resourceRequirements = new Array<AssetDescriptor<?>>();
	static {
		resourceRequirements.add(AssetEnum.UI_SKIN.getSkin());
		resourceRequirements.add(AssetEnum.BUTTON_SOUND.getSound());
		resourceRequirements.add(AssetEnum.MAIN_MENU_MUSIC.getMusic());
		resourceRequirements.add(AssetEnum.CAMP_BG0.getTexture());
		resourceRequirements.add(AssetEnum.CAMP_BG1.getTexture());
		resourceRequirements.add(AssetEnum.CAMP_BG2.getTexture());
		resourceRequirements.addAll(MainMenuScreen.resourceRequirements);
	}
	
	private final Preferences preferences;
	private final Skin skin;
	
	public OptionScreen(ScreenFactory factory, ScreenElements elements) {
		super(factory, elements, null);
		this.addActor(getCampBackground());
		skin = assetManager.get(AssetEnum.UI_SKIN.getSkin());
		final Sound sound = assetManager.get(AssetEnum.BUTTON_SOUND.getSound());
		
		preferences  = Gdx.app.getPreferences("tales-of-androgyny-preferences");
			
		/* Gameplay options */
		addLabelActor("- Gameplay Options -", 860, 950);
		final CheckBox makeup = new CheckBox("Show Makeup", skin);
		CheckBoxStyle style = makeup.getStyle();
		style.fontColor = Color.WHITE;
		makeup.setChecked(preferences.getBoolean("makeup", true));
		makeup.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final boolean val = makeup.isChecked();
	            preferences.putBoolean("makeup", val);
	        }
	    });
		makeup.getCells().get(0).size(50, 50);
		makeup.getCells().get(0).padRight(25);
		addActorAndListen(makeup, 325, 825);
		
		final CheckBox blood = new CheckBox("Show Blood", skin);
		blood.setChecked(preferences.getBoolean("blood", true));
		blood.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final boolean val = blood.isChecked();
	            preferences.putBoolean("blood", val);
	        }
	    });
		blood.getCells().get(0).size(50, 50);
		blood.getCells().get(0).padRight(25);
		addActorAndListen(blood, 575, 825);
		
		final CheckBox preload = new CheckBox("Preload (Restart game for effect)", skin);
		preload.setChecked(preferences.getBoolean("preload", false));
		preload.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final boolean val = preload.isChecked();
	            preferences.putBoolean("preload", val);
	        }
	    });
		preload.getCells().get(0).size(50, 50);
		preload.getCells().get(0).padRight(25);
		addActorAndListen(preload, 825, 825);
		
		final CheckBox cheat = new CheckBox("Cheat", skin);
		cheat.setChecked(preferences.getBoolean("cheat", false));
		cheat.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final boolean val = cheat.isChecked();
	            preferences.putBoolean("cheat", val);
	        }
	    });
		cheat.getCells().get(0).size(50, 50);
		cheat.getCells().get(0).padRight(25);
		addActorAndListen(cheat, 1300, 825);
		
		/* Favorite flavor */
		addLabelActor("Favorite Flavor", 1550, 900);
		final SelectBox<String> flavors = new SelectBox<String>(skin);
		flavors.setItems(new Array<String>(new String[]{"CREAM", "VANILLA", "CARAMEL", "CHOCOLATE", "DARK-CHOCOLATE"}));
		String currentFavoriteFlavor = preferences.getString("bunny", "CREAM");
		for (String flavorToCheck : flavors.getItems()) {
			if (flavorToCheck.equals(currentFavoriteFlavor)) {
				flavors.setSelected(flavorToCheck);
			}

		}
		flavors.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final String val = flavors.getSelected();
	            preferences.putString("bunny", val.toString());
	        }
	    });
		flavors.setWidth(150);
		addActorAndListen(flavors, 1550, 850);		
		
		/* Auto slider */
		final Label autoSpeed = addLabelActor("Autoplay speed: " + preferences.getInteger("autoplaySpeed", 5), 745, 800);
		final Slider autoSpeedSlider = new Slider(1, 10, 1, false, skin);
		
		autoSpeedSlider.setValue(preferences.getInteger("autoplaySpeed", 5));
		autoSpeedSlider.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final int val = (int)autoSpeedSlider.getValue();
	            preferences.putInteger("autoplaySpeed", val);
	            autoSpeed.setText("Autoplay speed: " + val);
	        }
	    });
		autoSpeedSlider.getStyle().knob.setMinHeight(40);
		autoSpeedSlider.setSize(400, 40);
		addActorAndListen(autoSpeedSlider, 745, 750);		
		
		/* Video options */
		addLabelActor("- Video Options -", 860, 550);
		
		/* Full Screen toggle */
		final CheckBox fullScreen = new CheckBox("FullScreen", skin);
		fullScreen.setChecked(preferences.getBoolean("fullScreen", false));
		fullScreen.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final boolean val = fullScreen.isChecked();
	            preferences.putBoolean("fullScreen", val);
	            if (!debug) {
		    		if(val) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		    		else Gdx.graphics.setWindowedMode(preferences.getInteger("width", TalesOfAndrogyny.defaultScreenWidth), preferences.getInteger("height", TalesOfAndrogyny.defaultScreenWidth));
	            }
	        }
	    });
		fullScreen.getCells().get(0).size(50, 50);
		fullScreen.getCells().get(0).padRight(25);
		addActorAndListen(fullScreen, 750, 420);
		
		final CheckBox cameraShake = new CheckBox("Camera Shake", skin);
		cameraShake.setChecked(preferences.getBoolean("cameraShake", true));
		cameraShake.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final boolean val = cameraShake.isChecked();
	            preferences.putBoolean("cameraShake", val);
	        }
	    });
		cameraShake.getCells().get(0).size(50, 50);
		cameraShake.getCells().get(0).padRight(25);
		addActorAndListen(cameraShake, 1000, 420);
		
		final CheckBox fastWorldMap = new CheckBox("Fast World Map", skin);
		fastWorldMap.setChecked(preferences.getBoolean("fastWorldMap", false));
		fastWorldMap.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final boolean val = fastWorldMap.isChecked();
	            preferences.putBoolean("fastWorldMap", val);
	        }
	    });
		fastWorldMap.getCells().get(0).size(50, 50);
		fastWorldMap.getCells().get(0).padRight(25);
		addActorAndListen(fastWorldMap, 1250, 420);
		
		/* Resolution selection */
		addLabelActor("Resolution", 550, 500);
		final SelectBox<Vector2> resolution = new SelectBox<Vector2>(skin);
		resolution.setItems(new Array<Vector2>(new Vector2[]{new Vector2(1920, 1080), new Vector2(1600, 900), new Vector2(1280, 720), new Vector2(960, 540)}));
		Vector2 currentResolution = new Vector2(preferences.getInteger("width", TalesOfAndrogyny.defaultScreenWidth), preferences.getInteger("height", TalesOfAndrogyny.defaultScreenHeight));
		for (Vector2 resolutionToCheck : resolution.getItems()) {
			if (resolutionToCheck.epsilonEquals(currentResolution, 1)) {
				resolution.setSelected(resolutionToCheck);
			}

		}
		resolution.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final Object val = resolution.getSelected();
	            final int width = (int)((Vector2) val).x;
	            final int height = (int)((Vector2) val).y;
	            preferences.putInteger("width", width);
	            preferences.putInteger("height", height);
	            Gdx.graphics.setWindowedMode(width, height);	
	        }
	    });
		resolution.setWidth(150);
		addActorAndListen(resolution, 550, 450);
		
		/* Sound options */
		addLabelActor("- Sound Options -", 860, 400);
		
		
		/* Sound slider */
		final Label soundVolumePercent = addLabelActor("Sound volume: " + (int)(preferences.getFloat("volume", 1) * 100) +"%", 745, 350);
		final Slider soundSlider = new Slider(0, 1, .1f, false, skin);
		
		soundSlider.setValue(preferences.getFloat("volume", 1));
		soundSlider.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final float val = soundSlider.getValue();
	            preferences.putFloat("volume", val);
	            preferences.putBoolean("isMuted", false);
	            sound.play(val *.5f);
	            soundVolumePercent.setText("Sound volume: " + String.valueOf((int)(val * 100)) +"%");
	        }
	    });
		soundSlider.getStyle().knob.setMinHeight(40);
		soundSlider.setSize(400, 40);
		addActorAndListen(soundSlider, 745, 300);
		/* Music slider */
		final Label musicVolumePercent = addLabelActor("Music volume: " + (int)(preferences.getFloat("musicVolume", 1) * 100) +"%", 745, 240);
		final Slider musicSlider = new Slider(0, 1, .1f, false, skin);
		
		musicSlider.setValue(preferences.getFloat("musicVolume", 1));
		musicSlider.addListener(new ChangeListener() {
	        @Override
	        public void changed(ChangeEvent event, Actor actor) {
	            final float val = musicSlider.getValue();
	            preferences.putFloat("musicVolume", val);
	            preferences.putBoolean("isMuted", false);
	            setVolume(val);
	            musicVolumePercent.setText("Music volume: " + String.valueOf((int)(val * 100)) +"%");
	        }
	    });
		musicSlider.setSize(400, 40);
		addActorAndListen(musicSlider, 745, 190);
		
		final TextButton done = new TextButton("Done", skin);
		
		done.addListener(
			new ClickListener() {
				@Override
		        public void clicked(InputEvent event, float x, float y) {
					if (!debug) {
						sound.play(preferences.getFloat("volume") *.5f);
						saveAndExit();	 
					}
		        }
			}
		);
		addActorAndListen(done, 1550, 50);
	}
	
	@Override
	public void buildStage() {}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			saveAndExit();
		}
		if(Gdx.input.isKeyJustPressed(Keys.SHIFT_LEFT)) {
			debug = !debug;
		}
	}
	
	private void saveAndExit() {
		preferences.flush();
		showScreen(ScreenEnum.MAIN_MENU);
	}
	
	private Label addLabelActor(String label, int x, int y) {
		Label newLabel = new Label(label, skin);
		newLabel.setColor(Color.WHITE);
		addActorAndListen(newLabel, x, y);
		return newLabel;
	}
	
	@Override
	public void show() {
		super.show();
	    getRoot().getColor().a = 0;
	    getRoot().addAction(Actions.fadeIn(0.5f));
	}
}