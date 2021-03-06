package com.majalis.character;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class StaminaBar extends DisplayWidget {
	private final AssetManager assetManager;
	private final AbstractCharacter character;
	private final ProgressBar bar;
	private final Image icon;
	private final Label label;
	private final Label diffValueDisplay;
	private int value;
	public StaminaBar(AbstractCharacter character, AssetManager assetManager, Skin skin) {
		this.character = character;
		this.assetManager = assetManager;
		bar = new ProgressBar(0, 1, .01f, false, skin);
		bar.setWidth(350);
		bar.setValue(character.getStaminaPercent());
		this.addActor(bar);
		
		icon = new Image(assetManager.get(character.getStaminaDisplay()));
		icon.setPosition(3, 7.5f);
		this.addActor(icon);
		
		label = new Label(character.getCurrentStamina() + " / " + character.getMaxStamina(), skin);
		label.setColor(Color.BROWN);
		label.setPosition(75, 8);
		this.addActor(label);
		bar.setColor(character.getStaminaColor());
		
		diffValueDisplay = new Label("", skin);
		diffValueDisplay.setPosition(getX() + 350, getY() + 25);
		this.addActor(diffValueDisplay);
		value = character.getCurrentStamina();
	}
	
	@Override
	public void act(float delta) {
		float characterStaminaPercent = character.getStaminaPercent();
		if(Math.abs(bar.getValue() - characterStaminaPercent) > .01) {
			if (bar.getValue() < characterStaminaPercent) {
				bar.setValue(bar.getValue() + .01f);
			}
			else {
				bar.setValue(bar.getValue() - .01f);
			}
		}
		icon.setDrawable(new TextureRegionDrawable(new TextureRegion(assetManager.get(character.getStaminaDisplay()))));
		label.setText(character.getCurrentStamina() + " / " + character.getMaxStamina());
		bar.setColor(character.getStaminaColor());
		if (value != character.getCurrentStamina()) {
			setDiffLabel(diffValueDisplay, character.getCurrentStamina() - value);
			value = character.getCurrentStamina();
		}
		super.act(delta);
	}
}