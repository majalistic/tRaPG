package com.majalis.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.ObjectMap;

public enum AnimationEnum {
	BUTTBANG(AssetEnum.GAME_OVER_ANIMATION),
	HARPY(AssetEnum.HARPY_ANIMATION),
	BRIGAND(AssetEnum.BRIGAND_ANIMATION),
	CENTAUR(AssetEnum.CENTAUR_ANIMATION),
	UNICORN(AssetEnum.CENTAUR_ANIMATION), 
	ORC(AssetEnum.ORC_ANIMATION),
	WEREWOLF(AssetEnum.WEREWOLF_ANIMATION),
	ORC_PRONE_BONE(AssetEnum.ORC_PRONE_BONE_ANIMATION),
	GOBLIN(AssetEnum.GOBLIN_ANIMATION),
	GOBLIN_MALE(AssetEnum.GOBLIN_ANIMATION),
	TRUDY(AssetEnum.TRUDY_SPRITE_ANIMATION),
	BEASTMISTRESS(AssetEnum.BEASTMISTRESS_ANIMATION),
	GHOST(AssetEnum.GHOST_ANIMATION),
	GHOST_SPOOKY(AssetEnum.GHOST_ANIMATION),
	GHOST_SPOOKY_BLOODLESS(AssetEnum.GHOST_ANIMATION),
	BUNNY_CREAM(AssetEnum.BUNNY_ANIMATION),
	BUNNY_VANILLA(AssetEnum.BUNNY_ANIMATION),
	BUNNY_CARAMEL(AssetEnum.BUNNY_ANIMATION),
	BUNNY_CHOCOLATE(AssetEnum.BUNNY_ANIMATION),
	BUNNY_DARK_CHOCOLATE(AssetEnum.BUNNY_ANIMATION),
	NULL(AssetEnum.NULL_ANIMATION)	
	;
	private static final ObjectMap<AssetEnum, AnimatedActorFactory> factoryMap = new ObjectMap<AssetEnum, AnimatedActorFactory>();
	private final AssetEnum animationToken;
	private AnimationEnum(AssetEnum animationToken) {
		this.animationToken = animationToken;
	}
	
	public AssetEnum getAnimationToken() { return animationToken; }
	
	public AnimatedActor getAnimation(AssetManager assetManager) {
		AnimatedActorFactory factory = factoryMap.get(animationToken);
		if (factory == null) {
			factory = assetManager.get(animationToken.getAnimation());
			factoryMap.put(animationToken, factory);
		}			
		AnimatedActor animation = factory.getInstance();
		if (this == NULL || this == BUTTBANG) return animation;
		if (this == HARPY) {
			animation.setSkeletonPosition(900, 550);
		}
		else if (this == BRIGAND || this == BEASTMISTRESS || this == BUNNY_CREAM || this == BUNNY_VANILLA || this == BUNNY_CARAMEL || this == BUNNY_CHOCOLATE || this == BUNNY_DARK_CHOCOLATE) {
			animation.setSkeletonPosition(900, 450);
		}
		else if (this == GOBLIN || this == GOBLIN_MALE) {
			animation.setSkeletonPosition(1000, 350);
		}
		else if (this == ORC_PRONE_BONE) {
			animation.setSkeletonPosition(985, 310);
		}
		else {
			animation.setSkeletonPosition(1000, 550);
		}
		
		if (this == CENTAUR) {
			animation.setSkeletonSkin("BrownCentaur");
		}
		else if (this == UNICORN) {
			animation.setSkeletonSkin("WhiteUnicorn");
		}
		else if (this == GHOST) {
			animation.setSkeletonSkin("Fantasy");
		}
		else if (this == GHOST_SPOOKY) {
			animation.setSkeletonSkin("RealityBloody");
		}
		else if (this == GHOST_SPOOKY_BLOODLESS) {
			animation.setSkeletonSkin("RealityClean");
		}
		else if (this == GOBLIN) {
			animation.setSkeletonSkin("Femme");
		}
		else if (this == GOBLIN_MALE) {
			animation.setSkeletonSkin("Homme");
		}
		else if (this == BUNNY_CREAM) {
			animation.setSkeletonSkin("Vanilla");
		}
		else if (this == BUNNY_VANILLA) {
			animation.setSkeletonSkin("Cinnamon");
		}
		else if (this == BUNNY_CARAMEL) {
			animation.setSkeletonSkin("Caramel");
		}
		else if (this == BUNNY_CHOCOLATE) {
			animation.setSkeletonSkin("Mocha");
		}
		else if (this == BUNNY_DARK_CHOCOLATE) {
			animation.setSkeletonSkin("Chocolate");
		}
		if (this == ORC_PRONE_BONE) {
			animation.setAnimation(0, "SlowMed", true);
		}
		else {
			animation.setAnimation(0, "Idle Erect", true);
		}
		return animation;
	}
}