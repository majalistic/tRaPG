package com.majalis.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.majalis.asset.AssetEnum;
import com.majalis.character.Armor;
import com.majalis.character.Armor.ArmorType;
import com.majalis.character.Item;
import com.majalis.character.Item.EffectType;
import com.majalis.character.Item.Misc;
import com.majalis.character.Item.MiscType;
import com.majalis.character.Item.Plug;
import com.majalis.character.Item.PlugType;
import com.majalis.character.Item.Mouthwear;
import com.majalis.character.Item.Accessory;
import com.majalis.character.Item.AccessoryType;
import com.majalis.character.Item.ChastityCage;
import com.majalis.character.Item.Potion;
import com.majalis.character.Item.Weapon;
import com.majalis.character.Item.WeaponType;
import com.majalis.character.PlayerCharacter;
import com.majalis.character.AbstractCharacter.Stat;
import com.majalis.encounter.Background;
import com.majalis.encounter.EncounterHUD;
import com.majalis.save.SaveEnum;
import com.majalis.save.SaveService;
import com.majalis.screens.TimeOfDay;

public class ShopScene extends Scene {

	private final SaveService saveService;
	private final Skin skin;
	private final Sound buttonSound;
	private final Sound itemSound;
	private final Shop shop;
	private final PlayerCharacter character;
	private final Label console;
	private final Label money;
	
	public static class Shop {
		private Array<Item> items;
		private ShopCode shopCode;
		private boolean done;
		private Shop() {}
		private Shop(ShopCode shopCode) {
			this.shopCode = shopCode;
			this.items = new Array<Item>();
			done = false;
		}
		public String getShopCode() {
			return shopCode.toString();
		}
	}
	
	public ShopScene(OrderedMap<Integer, Scene> sceneBranches, int sceneCode, final SaveService saveService, AssetManager assetManager, final PlayerCharacter character, Background background, final ShopCode shopCode, Shop loadedShop, EncounterHUD hud) {
		super(sceneBranches, sceneCode, hud);
		this.saveService = saveService;
		this.addActor(background);
		this.character = character;
		
		skin = assetManager.get(AssetEnum.UI_SKIN.getSkin());
		buttonSound = assetManager.get(AssetEnum.BUTTON_SOUND.getSound());
		itemSound = assetManager.get(AssetEnum.EQUIP.getSound());
		
		final Group inventoryGroup = new Group();
		this.addActor(inventoryGroup);
		
		Group moneyGroup = new Group();
		this.addActor(moneyGroup);
		moneyGroup.setPosition(1600, 950);

		addImage(moneyGroup, assetManager.get(AssetEnum.TEXT_BOX.getTexture()), 0, 0, 300, 100);
		addImage(inventoryGroup, assetManager.get(AssetEnum.BATTLE_TEXTBOX.getTexture()), 25, 0, 875, 1125);
																							
		final Image hoverBox = addImage(assetManager.get(AssetEnum.BATTLE_HOVER.getTexture()), 1300, 300, 600, 600); 
		this.removeActor(hoverBox);
		
		this.console = addLabel("", skin, Color.TAN, 1375, 925);
		this.money = addLabel(moneyGroup, String.valueOf(character.getMoney())+" Gold", skin, Color.GOLDENROD, 110, 45);
		final TextButton done = new TextButton("Done", skin);
		
		done.addListener(
			new ClickListener() {
				@Override
		        public void clicked(InputEvent event, float x, float y) {
					buttonSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
					nextScene();		   
		        }
			}
		);
		done.setPosition(1575, 25);
		
		// need to create methods for selling
		// need to show description of items (should be an attribute of an item)	
		// need to let a player equip items as they're purchased	
		// need to split different types of items into different lists - weapons, armors, accessories, consumables, skills
		
		shop = initShop(shopCode, loadedShop);
		
		saveService.saveDataValue(SaveEnum.SHOP, shop);
		saveService.saveDataValue(SaveEnum.PLAYER, character);
		
		final Table buyTable = new Table(skin);
		buyTable.align(Align.topLeft);
		
		ScrollPane buyScrollPane = new ScrollPane(buyTable);
		
		buyScrollPane.setScrollingDisabled(true, false);
		buyScrollPane.setOverscroll(false, false);
		buyScrollPane.setBounds(65, 75, 825, 950);
		
		inventoryGroup.addActor(buyScrollPane);
	
		Label buyLabel = new Label("Buy", skin);
		buyLabel.setColor(Color.BLACK);
		buyTable.add(buyLabel).row();
		
		boolean row = false;
		for (final Item potion: shop.items) {
			final TextButton potionButton = new TextButton(potion.getName() + " - " + potion.getValue() / (shopCode == ShopCode.GADGETEER_SHOP ? 3 : 1 ) + "G", skin);
			final Label description = new Label(potion.getDescription(), skin);
			description.setWrap(true);
			description.setColor(Color.FOREST);
			description.setAlignment(Align.top);
			final ScrollPane pane = new ScrollPane(description);
			pane.setBounds(1410, 300, 425, 550);
			pane.setScrollingDisabled(true, false);
			
			potionButton.addListener(new ClickListener() {
				@Override
		        public void clicked(InputEvent event, float x, float y) {
					if (buyItem(potion, shopCode)) {
						itemSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
						addActor(done);
						// this should do what the character screen now does, shifting the item list down
						potionButton.addAction(Actions.removeActor());
						shop.items.removeValue(potion, true);
						
						shop.done = true;
						money.setText(String.valueOf(character.getMoney())+" Gold");
						console.setText("You purchase the " + potion.getName() + ".");
						saveService.saveDataValue(SaveEnum.SHOP, shop);
						saveService.saveDataValue(SaveEnum.PLAYER, character);
					}
					else {
						console.setText("You can't afford the " + potion.getName());
					}
		        }
				@Override
		        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					inventoryGroup.addActor(hoverBox);
					inventoryGroup.addActor(pane);
				}
				@Override
		        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
					inventoryGroup.removeActor(hoverBox);
					inventoryGroup.removeActor(pane);
				}
			});
			buyTable.add(potionButton).size(400, 50).align(Align.left);
			if (row) buyTable.row();
			row = !row;
		}
		
		final Table sellTable = new Table(skin);
		sellTable.align(Align.topLeft);
		
		ScrollPane sellScrollPane = new ScrollPane(sellTable);
		
		sellScrollPane.setScrollingDisabled(true, false);
		sellScrollPane.setOverscroll(false, false);
		sellScrollPane.setBounds(65, 75, 825, 950);
		
		inventoryGroup.addActor(sellScrollPane);
	
		Label sellLabel = new Label("Sell", skin);
		sellLabel.setColor(Color.BLACK);
		sellTable.add(sellLabel).row();
		
		row = false;
		for (final Item potion: character.getInventory()) {
			final TextButton potionButton = new TextButton(potion.getName() + " - " + potion.getValue() / 4 + "G", skin);
			final Label description = new Label(potion.getDescription(), skin);
			description.setWrap(true);
			description.setColor(Color.FOREST);
			description.setAlignment(Align.top);
			final ScrollPane pane = new ScrollPane(description);
			pane.setBounds(1410, 300, 425, 550);
			pane.setScrollingDisabled(true, false);
			
			potionButton.addListener(new ClickListener() {
				@Override
		        public void clicked(InputEvent event, float x, float y) {
					if (sellItem(potion)) {
						itemSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
						// this should do what the character screen now does, shifting the item list down
						potionButton.addAction(Actions.removeActor());
						character.getInventory().removeValue(potion, true);
						money.setText(String.valueOf(character.getMoney())+" Gold");
						console.setText("You sell the " + potion.getName() + ".");
						saveService.saveDataValue(SaveEnum.PLAYER, character);
					}
					else {
						console.setText("You can't sell the " + potion.getName());
					}
		        }
				@Override
		        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					inventoryGroup.addActor(hoverBox);
					inventoryGroup.addActor(pane);
				}
				@Override
		        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
					inventoryGroup.removeActor(hoverBox);
					inventoryGroup.removeActor(pane);
				}
			});
			sellTable.add(potionButton).size(400, 50).align(Align.left);
			if (row) sellTable.row();
			row = !row;
		}
		
		final TextButton buy = new TextButton("Buy", skin);
		final TextButton sell = new TextButton("Sell", skin);
		
		buy.addListener(
			new ClickListener() {
				@Override
		        public void clicked(InputEvent event, float x, float y) {
					buttonSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
					sell.addAction(Actions.show());
					sellScrollPane.addAction(Actions.hide());
					buy.addAction(Actions.hide());
					buyScrollPane.addAction(Actions.show());
		        }
			}
		);
		sell.addListener(
			new ClickListener() {
				@Override
		        public void clicked(InputEvent event, float x, float y) {
					buttonSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
					sell.addAction(Actions.hide());
					sellScrollPane.addAction(Actions.show());
					buy.addAction(Actions.show());
					buyScrollPane.addAction(Actions.hide());
		        }
			}
		);
		
		buy.setPosition(1575, 100);
		sell.setPosition(1575, 100);
		
		sellScrollPane.addAction(Actions.hide());
		buy.addAction(Actions.hide());
		
		inventoryGroup.addActor(buy);
		inventoryGroup.addActor(sell);
		
		if (shopCode.isTinted()) {
			background.setColor(TimeOfDay.getTime(character.getTime()).getColor());
		}
		
		if (shop.done || shopCode != ShopCode.FIRST_STORY) addActor(done);
	}

	private Shop initShop(ShopCode shopCode, Shop shop) {
		if (shop != null) {
			if (shopCode == ShopCode.SHOP) {
				shop.items.addAll(getShopRestock(shopCode));
			}
			return shop;
		}
		shop = new Shop(shopCode);
		switch (shopCode) {
			case FIRST_STORY:
			case WEAPON_SHOP:
				for (WeaponType type: WeaponType.values()) {
					if (type.isBuyable() && type != WeaponType.Club) {
						shop.items.add(new Weapon(type));	
					}									
				}
				if (shopCode == ShopCode.WEAPON_SHOP) {
					for (WeaponType type: WeaponType.values()) {
						if (type.isBuyable()) {
							shop.items.add(new Weapon(type, 1));	
						}									
					}
					shop.items.add(new Armor(ArmorType.CLOTH_TOP));	
					shop.items.add(new Armor(ArmorType.BREASTPLATE));	
					shop.items.add(new Armor(ArmorType.DIAMOND_PLATE));	
					shop.items.add(new Armor(ArmorType.SKIRT));	
					shop.items.add(new Armor(ArmorType.BATTLE_SKIRT));	
					shop.items.add(new Armor(ArmorType.SHORTS));	
					shop.items.add(new Armor(ArmorType.SHIELD));	
					shop.items.add(new Armor(ArmorType.REINFORCED_SHIELD));	
					shop.items.add(new Armor(ArmorType.HELMET));
					shop.items.add(new Armor(ArmorType.GAUNTLET));
					shop.items.add(new Armor(ArmorType.SABATONS));
					shop.items.add(new Misc(MiscType.KEY));	
				}
				break;
			case MAGIC_SHOP:
				shop.items.add(new Potion(30, EffectType.MAGIC));	
				shop.items.add(new Potion(30, EffectType.MAGIC));	
				shop.items.add(new Potion(30, EffectType.KNOCKDOWN));	
				shop.items.add(new Potion(30, EffectType.KNOCKDOWN));	
				shop.items.add(new Potion(12, EffectType.ARMOR_SUNDER));	
				shop.items.add(new Potion(12, EffectType.ARMOR_SUNDER));	
				shop.items.add(new Potion(1, EffectType.TOWN_PORTAL));	
				shop.items.add(new Accessory(AccessoryType.STATBOOSTER, Stat.STRENGTH));	
				shop.items.add(new Accessory(AccessoryType.STATBOOSTER, Stat.ENDURANCE));	
				shop.items.add(new Accessory(AccessoryType.STATBOOSTER, Stat.AGILITY));	
				shop.items.add(new Accessory(AccessoryType.STATBOOSTER, Stat.PERCEPTION));	
				shop.items.add(new Accessory(AccessoryType.STATBOOSTER, Stat.MAGIC));	
				shop.items.add(new Accessory(AccessoryType.STATBOOSTER, Stat.CHARISMA));
				break;
			case SHOP:
				for (int ii = 0; ii < 4; ii++) {
					shop.items.add(new Potion(5, EffectType.MEAT));
				}
				shop.items.add(new Potion(10, EffectType.BANDAGE));
				shop.items.add(new Potion(10, EffectType.BANDAGE));
				shop.items.add(new Potion(10, EffectType.BANDAGE));
				for (int ii = 10; ii <= 20; ii += 10) {
					shop.items.add(new Potion(ii));
					shop.items.add(new Potion(ii));
				}
				shop.items.add(new Potion(3, EffectType.BONUS_STRENGTH));
				shop.items.add(new Potion(3, EffectType.BONUS_AGILITY));
				shop.items.add(new Potion(3, EffectType.BONUS_ENDURANCE));
				break;
			case GADGETEER_SHOP:
				shop.items.add(new Potion(15));
				shop.items.add(new Potion(15));
				shop.items.add(new Potion(3, EffectType.BONUS_STRENGTH));
				shop.items.add(new Potion(3, EffectType.BONUS_STRENGTH));
				shop.items.add(new Potion(3, EffectType.BONUS_AGILITY));
				shop.items.add(new Potion(3, EffectType.BONUS_AGILITY));
				shop.items.add(new Potion(3, EffectType.BONUS_ENDURANCE));
				shop.items.add(new Potion(3, EffectType.BONUS_ENDURANCE));
				shop.items.add(new Plug(PlugType.BUTTPLUG));
				shop.items.add(new Plug(PlugType.BEADS));
				shop.items.add(new Plug(PlugType.TAILPLUG));
				shop.items.add(new Plug(PlugType.RINGPLUG));
				shop.items.add(new Mouthwear());
				shop.items.add(new ChastityCage());
				break;
			case MONSTER_SHOP:
				shop.items.add(new Potion(30));
				shop.items.add(new Potion(30));
				shop.items.add(new Potion(10, EffectType.BANDAGE));
				shop.items.add(new Potion(10, EffectType.BANDAGE));
				shop.items.add(new Potion(30, EffectType.MAGIC));	
				shop.items.add(new Potion(30, EffectType.KNOCKDOWN));	
				shop.items.add(new Potion(12, EffectType.ARMOR_SUNDER));	
				shop.items.add(new Potion(1, EffectType.TOWN_PORTAL));	
				shop.items.add(new Armor(ArmorType.HELM_OF_WISDOM));	
				shop.items.add(new Armor(ArmorType.GAUNTLETS_OF_STRENGTH));	
				shop.items.add(new Armor(ArmorType.SHOES_OF_RUNNING));	
				break;
		}
		if (shopCode == ShopCode.SHOP) {
			shop.items.addAll(getShopRestock(shopCode));
		}
		return shop;
	}
	
	private Array<? extends Potion> getShopRestock(ShopCode shopCode) {
		Array<Potion> restock = new Array<Potion>();
		for (int ii = 0; ii < character.needShopRestock(shopCode); ii++) {
			switch(shopCode) {
				case SHOP:
					restock.add(new Potion(20, EffectType.MEAT));
					restock.add(new Potion(20, EffectType.MEAT));
					restock.add(new Potion(10, EffectType.BANDAGE));
					restock.add(new Potion(20));
					restock.add(new Potion(20));
					break;
				default: ;
			}
		}
		return restock;
	}

	private boolean buyItem(Item item, ShopCode shopCode) {
		return character.buyItem(item, item.getValue() / (shopCode == ShopCode.GADGETEER_SHOP ? 3 : 1));
	}
	
	private boolean sellItem(Item item) {
		return character.sellItem(item);
	}

	@Override
	public void activate() {
		isActive = true;
		this.addAction(Actions.show());
		this.setBounds(0, 0, 2000, 2000);
		saveService.saveDataValue(SaveEnum.SCENE_CODE, sceneCode);
	}

	private void nextScene() {
		sceneBranches.get(sceneBranches.orderedKeys().get(0)).setActive();
		isActive = false;
		addAction(Actions.hide());
	}
	
	public enum ShopCode {
		FIRST_STORY, SHOP, WEAPON_SHOP, GADGETEER_SHOP, MAGIC_SHOP, MONSTER_SHOP;
	
		public AssetDescriptor<Texture> getBackground() {
			switch(this) {
				case GADGETEER_SHOP:
					return AssetEnum.DEFAULT_BACKGROUND.getTexture();
				case MAGIC_SHOP:
					return AssetEnum.CABIN_BACKGROUND.getTexture();
				case WEAPON_SHOP:
				case SHOP:
				case FIRST_STORY:
				case MONSTER_SHOP:
					return AssetEnum.TOWN_BG.getTexture();
				default:
					break;
				
				}
			return null;
		}

		public AssetDescriptor<Texture> getForeground() {
			switch(this) {
				case FIRST_STORY:
				case SHOP:
					return AssetEnum.SHOPKEEP.getTexture();
				case GADGETEER_SHOP:
					return AssetEnum.GADGETEER.getTexture();
				case WEAPON_SHOP:
					return AssetEnum.TRAINER.getTexture();
				case MAGIC_SHOP:
					return AssetEnum.MERI_SILHOUETTE.getTexture();
				case MONSTER_SHOP:
					return AssetEnum.MERI_SILHOUETTE.getTexture();
				default:
					break;
			}
			return null;
		}
		
		public int getX() {	return 800; }

		public int getY() { return -100; }

		public boolean isTinted() { return this != GADGETEER_SHOP; }
	}
}
