package com.majalis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.majalis.asset.AssetEnum;
import com.majalis.character.PlayerCharacter;
import com.majalis.save.SaveEnum;
import com.majalis.save.SaveManager;
import com.majalis.save.SaveManager.GameMode;
import com.majalis.save.SaveService;
/*
 * Screen for displaying, saving, and loading save files.
 */
public class SaveScreen extends AbstractScreen {

	public static final Array<AssetDescriptor<?>> resourceRequirements = new Array<AssetDescriptor<?>>();
	static {
		resourceRequirements.add(AssetEnum.UI_SKIN.getSkin());
		resourceRequirements.add(AssetEnum.BUTTON_SOUND.getSound());
	}
	private final SaveService saveService;
	private final Sound sound;
	
	public SaveScreen(ScreenFactory factory, ScreenElements elements, SaveService saveService) {
		super(factory, elements, null);
		this.saveService = saveService;
		this.sound = assetManager.get(AssetEnum.BUTTON_SOUND.getSound());
		setClearColor(Color.SLATE.r, Color.SLATE.g, Color.SLATE.b, 1);
	}

	@Override
	public void buildStage() {
		Skin skin = assetManager.get(AssetEnum.UI_SKIN.getSkin());
		Table saveTable = new Table();
		saveTable.setPosition(100, 1050);
		saveTable.align(Align.topLeft);
		this.addActor(saveTable);

		final Label console = new Label("", skin);
		console.setColor(Color.GOLD);
		console.setPosition(1000, 100);
		this.addActor(console);
		
		final TextButton backButton = new TextButton ("Back", skin);
		backButton.setPosition(1500, 50);
		this.addActor(backButton);
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
				showScreen(ScreenEnum.LOAD_GAME);
			}
		});
		
		final TextButton confirmButton = new TextButton ("", skin);
		confirmButton.setPosition(1050, 250);
		confirmButton.setWidth(450);
		confirmButton.setColor(Color.YELLOW);
		confirmButton.addAction(Actions.hide());
		this.addActor(confirmButton);		
		
		final TextButton quickLoadButton = new TextButton ("Quick Load", skin);
		quickLoadButton.setPosition(400, 50);
		this.addActor(quickLoadButton);
		quickLoadButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
        		saveService.newSave(".toa-data/quicksave.json");
				showScreen(ScreenEnum.LOAD_GAME);
			}
		});
		
		// for each save slot				
		for (int ii = 0; ii < 10; ii++) {
			// load the data
			final int num = ii;
			final String path = ".toa-data/save" + ii + ".json";
			SaveManager saveManager = new SaveManager(false, ".toa-data/save" + ii + ".json", ".toa-data/profile.json");
			PlayerCharacter character = saveManager.loadDataValue(SaveEnum.PLAYER, PlayerCharacter.class);
			GameMode mode = saveManager.loadDataValue(SaveEnum.MODE, GameMode.class);
			String timestamp = saveManager.loadDataValue(SaveEnum.TIME_STAMP, String.class);
			// create an actor based on the data that will display stats with a button with clicklistener (and associated enter-> click functionality) that will save the current game to that file(overwrite), load that file, a button with a clicklistener that will delete that file
			final Label saveValue = new Label(getSaveText(character, mode, ii, timestamp), skin);			
			boolean saveExists = character.getJobClass() != null;
			saveValue.setColor(!saveExists? Color.BLACK : mode == GameMode.SKIRMISH ? Color.BLUE : Color.FOREST);
			// add the actor to the list
			final TextButton saveButton = new TextButton ("Save", skin);
			
			final TextButton loadButton = new TextButton ("Load", skin);
			if (saveExists) {
				loadButton.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
						saveService.newSave(path);
						showScreen(ScreenEnum.LOAD_GAME);
					}
				});
				saveButton.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
						confirmButton.clearListeners();
						confirmButton.addAction(Actions.show());
						confirmButton.setText("Overwrite Save File " + (num + 1) + "?");
						confirmButton.addListener(getSaveListener(path, saveValue, loadButton, confirmButton, num, console));
					}
				});
			}
			else {
				loadButton.setColor(Color.GRAY);
				saveButton.addListener(getSaveListener(path, saveValue, loadButton, confirmButton, num, console));
			}
			
			
			final TextButton deleteButton = new TextButton ("Delete", skin);			
			deleteButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
					confirmButton.clearListeners();
					confirmButton.addAction(Actions.show());
					confirmButton.setText("Delete Save File " + (num + 1) + "?");
					confirmButton.addListener(new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
							SaveManager saveManager = new SaveManager(false, path, ".toa-data/profile.json");
							saveManager.newSave();
							saveValue.setText("Save File " + (num + 1) + " - No Save File");
							saveValue.setColor(Color.BLACK);
							loadButton.setColor(Color.GRAY);
							loadButton.clearListeners();
							confirmButton.addAction(Actions.hide());
							saveButton.clearListeners();
							saveButton.addListener(getSaveListener(path, saveValue, loadButton, confirmButton, num, console));
						}
					});
				}
			});
			
			saveTable.add(saveValue).align(Align.left).width(800);
			saveTable.add(saveButton);	
			saveTable.add(loadButton);				
			saveTable.add(deleteButton).row();	
		}
	}
	
	private ClickListener getSaveListener(final String path, final Label saveValue, final TextButton loadButton, final TextButton confirmButton, final int num, final Label console) {
		return new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
				saveService.manualSave(path);
				SaveManager saveManager = new SaveManager(false, path, ".toa-data/profile.json");
				PlayerCharacter characterTemp = saveManager.loadDataValue(SaveEnum.PLAYER, PlayerCharacter.class);
				GameMode modeTemp = saveManager.loadDataValue(SaveEnum.MODE, GameMode.class);
				String timestamp = saveManager.loadDataValue(SaveEnum.TIME_STAMP, String.class);
				saveValue.setText(getSaveText(characterTemp, modeTemp, num, timestamp));
				consoleLog(console, "Save File " + (num + 1) + " was saved.");
				saveValue.setColor(modeTemp == GameMode.SKIRMISH ? Color.BLUE : Color.FOREST);
				loadButton.setColor(Color.WHITE);
				loadButton.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						sound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
						saveService.newSave(path);
						showScreen(ScreenEnum.LOAD_GAME);
					}
				});
				confirmButton.addAction(Actions.hide());
			}
		};
	}
	
	private String getSaveText(PlayerCharacter character, GameMode mode, int saveNumber, String timestamp) {
		return "Save File " + (saveNumber + 1) + " - " + (character.getJobClass() == null ? "No Save File" : "\"" + character.getCharacterName() + "\" - " + (mode == GameMode.SKIRMISH ? character.getJobClass().getLabel() : "Story Mode") + " - Level: " + character.getLevel() + "\n" + timestamp);
	}
	
	private void consoleLog(Label console, String newDisplay) {
		console.setText(newDisplay);
		console.clearActions();
		console.addAction(Actions.sequence(Actions.alpha(1), Actions.fadeOut(4)));
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			showScreen(ScreenEnum.LOAD_GAME);
		}
	}
}