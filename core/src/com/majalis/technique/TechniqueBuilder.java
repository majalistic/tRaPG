package com.majalis.technique;

import com.majalis.character.StatusType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.majalis.character.GrappleType;
import com.majalis.character.SexualExperience.SexualExperienceBuilder;
import com.majalis.character.Stance;
import com.majalis.technique.Bonus.BonusCondition;
import com.majalis.technique.Bonus.BonusType;
import com.majalis.technique.ClimaxTechnique.ClimaxType;
import com.majalis.technique.TechniquePrototype.TechniqueHeight;

public class TechniqueBuilder {
	
	protected Stance usableStance;
	protected Stance resultingStance;
	protected String name;
	protected boolean doesDamage;
	protected boolean doesHealing;
	protected SpellEffect spellEffect;
	protected SexualExperienceBuilder sex;
	protected SexualExperienceBuilder selfSex;
	protected int powerMod;
	protected int staminaCost;
	protected int stabilityCost;
	protected int manaCost;
	protected double knockdown;
	protected int armorSunder;
	protected int gutCheck;
	protected int guardMod;
	protected int parryMod;
	protected int evadeMod;
	private Stance forceStance;
	protected TechniqueHeight height;
	protected boolean ignoresArmor;
	protected boolean setDamage;
	protected boolean blockable;
	protected boolean parryable;
	protected boolean evadeable;
	protected boolean causesBleed;
	protected int setBleed;
	protected GrappleType grapple;
	protected ClimaxType climaxType;
	protected StatusType selfEffect;
	protected StatusType enemyEffect;
	protected int range;
	protected int advance;
	protected OrderedMap<BonusCondition, Bonus> bonuses;
	
	public TechniqueBuilder(Stance usableStance, Stance resultingStance, String name) {
		this.usableStance = usableStance;
		this.resultingStance = resultingStance;
		this.name = name;
		spellEffect = null;
		doesDamage = false;
		doesHealing = false;
		powerMod = 0;
		staminaCost = 0;
		stabilityCost = 0;
		manaCost = 0;
		sex = new SexualExperienceBuilder();
		selfSex = new SexualExperienceBuilder();
		forceStance = null;
		knockdown = 0;
		armorSunder = 0;
		gutCheck = 0;
		guardMod = 0;
		parryMod = 0;
		evadeMod = 0;
		ignoresArmor = false;
		setDamage = false;
		blockable = false;
		parryable = false;
		evadeable = false;
		causesBleed = true;
		setBleed = 0;
		grapple = GrappleType.NULL;
		selfEffect = null;
		enemyEffect = null;
		height = TechniqueHeight.NONE;
		range = 2;
		advance = 0;
		bonuses = new OrderedMap<BonusCondition, Bonus>();
		switch(resultingStance.getClimaxType()) {
			case ANAL: selfSex.setAnalSexTop(1); break;
			case ANAL_RECEPTIVE: if (usableStance.getClimaxType() == ClimaxType.ANAL_RECEPTIVE) selfSex.setAnal(1); else selfSex.setAnalSex(1); break;
			case BACKWASH: selfSex.setAssBottomTeasing(1); break;
			case FACIAL: 
			case ORAL: selfSex.setOralSexTop(1); break;
			case ORAL_RECEPTIVE: if (usableStance.getClimaxType() == ClimaxType.ORAL_RECEPTIVE) selfSex.setOral(1); else selfSex.setOralSex(1); break;
			default: break;
		}
	}
	
	public TechniqueBuilder addBonus(BonusCondition condition, BonusType type) { return addBonus(condition, type, 1); }
	public TechniqueBuilder addBonus(BonusCondition condition, BonusType type, int amount) {
		Bonus bonus = bonuses.get(condition, new Bonus(condition, type, amount));
		bonus.getBonusMap().put(type,  amount);
		bonuses.put(condition, bonus);
		return this;
	}
	
	public TechniqueBuilder setRange(int range) {
		this.range = range;
		return this;
	}

	public TechniqueBuilder setStamDam(int stamDam) {
		gutCheck = stamDam;
		return this;
	}
	
	public TechniqueBuilder setIgnoreArmor() {
		ignoresArmor = true;
		return this;
	}
	
	public TechniqueBuilder setAutoDamage() {
		setDamage = true;
		return this;
	}
	
	public TechniqueBuilder setCausesBleed(boolean casuesBleed) {
		this.causesBleed = casuesBleed;
		return this;
	}
	
	public TechniqueBuilder setBleed(int setBleed) {
		this.setBleed = setBleed;
		return this;
	}
	
	public TechniqueBuilder setBlockable(boolean blockable) {
		this.blockable = blockable;
		return this;
	}	
	
	public TechniqueBuilder setParryable(boolean parryable) {
		this.parryable = parryable;
		return this;
	}	
	
	public TechniqueBuilder setEvadeable(boolean evadeable) {
		this.evadeable = evadeable;
		return this;
	}	
	
	public TechniqueBuilder addSelfSex(SexualExperienceBuilder addedBuilder) {
		selfSex.combine(addedBuilder);
		return this;
	}
	
	public TechniqueBuilder addSex(SexualExperienceBuilder addedBuilder) {
		sex.combine(addedBuilder);
		return this;
	}
	
	protected TechniqueBuilder setForceStance(Stance forceStance) {
		if (forceStance == null) return this;
		this.forceStance = forceStance;
		if (forceStance.isAnalReceptive()) {
			addSex(new SexualExperienceBuilder().setAnalSex(1));
		}
		else if (forceStance.isAnalPenetration()) {
			addSex(new SexualExperienceBuilder().setAnalSexTop(1));
		}
		else if (forceStance.isOralReceptive()) {
			addSex(new SexualExperienceBuilder().setOralSex(1));
		}
		else if (forceStance.isOralPenetration()) {
			addSex(new SexualExperienceBuilder().setOralSexTop(1));
		}
		return this;
	}
	
	public TechniquePrototype build() {
		String lightDescription = getDescription();
		return new TechniquePrototype(usableStance, resultingStance, name, doesDamage, doesHealing, powerMod, staminaCost, stabilityCost, manaCost, spellEffect, sex, selfSex, forceStance, knockdown, armorSunder, gutCheck, height, guardMod, parryMod, evadeMod, ignoresArmor, setDamage, blockable, parryable, evadeable, causesBleed, setBleed, grapple, climaxType, selfEffect, enemyEffect, range, advance, getExpandedInfo() + lightDescription, lightDescription, getBonusInfo(), bonuses); 
	}	
	
	protected String getExpandedInfo() { 
		StringBuilder builder = new StringBuilder();
		if (doesDamage) {
			builder.append("Deals" + (powerMod > 0 ? " +" + powerMod : powerMod < 0 ? " " + powerMod : "") + " damage, improved by " + (spellEffect != null ? "MAG" : "STR") + ".\n");
		}
		if (doesHealing) {
			builder.append("Heals user with a power of " + powerMod + ", improved by MAG.\n");
		}
		if (selfEffect != null) {
			builder.append("Increases Strength dramatically, erodes - improved by MAG.\n");
		}
		if (enemyEffect != null) {
			if (enemyEffect == StatusType.STRENGTH_DEBUFF) {
				builder.append("Decreases Strength dramatically, duration improved by MAG.\n");
			}
			else {
				builder.append("Increases stamina costs and reduces stamina regen, duration improved by MAG.\n");
			}
		}	
		if (!causesBleed) {
			builder.append("Will not cause bleed even with a sharp weapon.\n");
		}
		if (knockdown > 0) {
			builder.append("Causes " + (knockdown > 1.6 ? "heavy" : knockdown > 1.1 ? "medium" : "light") + " knockdown.\n");
		}
		if (armorSunder > 0) {
			builder.append("Causes " + (armorSunder > 1.6 ? "heavy" : armorSunder > 1.1 ? "medium" : "light") + " armor sundering.\n");
		}
		if (gutCheck > 0) {
			builder.append("Causes " + (armorSunder > 1.6 ? "heavy" : armorSunder > 1.1 ? "medium" : "light") + " enemy stamina destruction.\n");
		}
		return builder.toString();
	}
	
	protected String getBonusInfo() {
		StringBuilder builder = new StringBuilder();
		for (OrderedMap.Entry<BonusCondition, Bonus> bonus : bonuses.entries()) {
			builder.append(bonus.key.getDescription() + "\n");
			builder.append(bonus.value.getDescription());
		}
		return builder.toString();
	}
	
	protected String getDescription() {
		StringBuilder builder = new StringBuilder();
		if (sex.getAssTeasing() > 0) {
			int totalAssTeasing = sex.getAssTeasing() + powerMod;
			builder.append("Seduces the target into wanting to fuck your ass, with a power of " + totalAssTeasing + ", improved by CHR.\n");
		}
		if (sex.getMouthTeasing() > 0) {
			int totalMouthTeasing = sex.getMouthTeasing() + powerMod;
			builder.append("Seduces the target into wanting to fuck your mouth, with a power of " + totalMouthTeasing + ", improved by CHR.\n");
		}
		if (sex.getAssBottomTeasing() > 0) {
			int totalAssTeasing = sex.getAssTeasing() + powerMod;
			builder.append("Seduces the target into wanting to get fucked in the ass, with a power of " + totalAssTeasing + ", improved by CHR.\n");
		}
		if (sex.getMouthBottomTeasing() > 0) {
			int totalMouthTeasing = sex.getMouthTeasing() + powerMod;
			builder.append("Seduces the target into wanting to get fucked in the mouth, with a power of " + totalMouthTeasing + ", improved by CHR.\n");
		}
		if (selfSex.getAssTeasing() > 0) {
			builder.append("Arouses you into wanting to fuck someone's ass, with a power of " + selfSex.getAssTeasing() + ".\n");
		}
		if (selfSex.getMouthTeasing() > 0) {
			builder.append("Arouses you into wanting to fuck someone's mouth, with a power of " + selfSex.getMouthTeasing() + ".\n");
		}
		if (selfSex.getAssBottomTeasing() > 0) {
			builder.append("Arouses you into wanting to get fucked in the ass, with a power of " + selfSex.getAssBottomTeasing() + ".\n");
		}
		if (selfSex.getMouthBottomTeasing() > 0) {
			builder.append("Arouses you into wanting to get fucked in the mouth, with a power of " + selfSex.getMouthBottomTeasing() + ".\n");
		}
		if (blockable || parryable || evadeable) {
			Array<String> blockText = new Array<String>();
			if (blockable) blockText.add("blocked");
			if (parryable) blockText.add("parried");
			if (evadeable) blockText.add("evaded");
			String blockTextString = "";
			for (String s : blockText) { blockTextString += s + ", "; }
			blockTextString.substring(0, blockTextString.length() - 2);
			builder.append("Can be " + blockTextString + "\n");
		}
		else if (doesDamage) {
			builder.append("CANNOT be blocked.\n");
		}
		if (doesDamage && (spellEffect != null || setDamage)) {
			builder.append("Ignores armor.\n");
		}
		if (guardMod > 0) {
			builder.append("Blocks with shield (" + (guardMod == 1 ? "1/4" : guardMod == 2 ? "1/2" : guardMod == 3 ? "3/4" : guardMod >= 4 ? "All" : "") + ")\n");
		}
		if (parryMod > 0) {
			builder.append("Parries with weapon (" + (parryMod == 1 ? "1/4" : parryMod == 2 ? "1/2" : parryMod == 3 ? "3/4" : parryMod >= 4 ? "All" : "") + ")\n");
		}
		if (evadeMod > 0) {
			builder.append("Dodges enemy attacks (" + (evadeMod == 1 ? "1/4" : evadeMod == 2 ? "1/2" : evadeMod == 3 ? "3/4" : evadeMod >= 4 ? "All" : "") + ")\n");
		}
		if (staminaCost > 0) {
			builder.append("Costs " + staminaCost + " stamina, reduced by END.\n");
		}
		else if (staminaCost < 0) {
			builder.append("Recovers " + -staminaCost + " stamina, improved by END.\n");
		}
		if (stabilityCost > 0) {
			builder.append("Causes " + stabilityCost + " instability, reduced by AGI.\n");			
		}
		if (manaCost > 0) {
			builder.append("Costs " + manaCost + " mana.\n");			
		}
		if (height != TechniqueHeight.NONE) {
			builder.append(height.toString() + "-height attack.\n");
		}
		if (forceStance != null) {
			builder.append("Forces enemy into " + forceStance.getLabel() + " stance.\n");
		}
		return builder.toString();
	}
}
