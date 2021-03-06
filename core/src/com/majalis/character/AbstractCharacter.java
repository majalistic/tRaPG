package com.majalis.character;

import com.majalis.asset.AnimatedActor;
import com.majalis.asset.AssetEnum;
import com.majalis.character.Attack.AttackHeight;
import com.majalis.character.Attack.Status;
import com.majalis.character.Item.Accessory;
import com.majalis.character.Item.ChastityCage;
import com.majalis.character.Item.EffectType;
import com.majalis.character.Item.EquipEffect;
import com.majalis.character.Item.Equipment;
import com.majalis.character.Item.ItemEffect;
import com.majalis.character.Item.Misc;
import com.majalis.character.Item.MiscType;
import com.majalis.character.Item.Mouthwear;
import com.majalis.character.Item.Plug;
import com.majalis.character.Item.Weapon;
import com.majalis.character.PlayerCharacter.Bootyliciousness;
import com.majalis.save.MutationResult;
import com.majalis.save.MutationResult.MutationType;
import com.majalis.save.SaveManager.JobClass;
import com.majalis.technique.ClimaxTechnique.ClimaxType;
import com.majalis.technique.SpellEffect;
import com.majalis.technique.Bonus;

import static com.majalis.character.Techniques.*;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
/*
 * Abstract character class, both enemies and player characters extend this class
 */
public abstract class AbstractCharacter extends Group {
	
	// some of these ints will be enumerators or objects in time
	/* permanent stats */
	protected String label;
	protected PronounSet pronouns;
	protected boolean secondPerson;
	
	/* rigid stats */
	protected JobClass jobClass;
	protected EnemyEnum enemyType;
	protected int level;
	protected int experience;
	protected int baseStrength;
	protected int baseEndurance;
	protected int baseAgility;
	protected int basePerception;
	protected int baseMagic;
	protected int baseCharisma;
	protected int baseLuck; // 0 for most classes, can go negative
	
	protected int baseDefense;
	protected int baseEvade;
	protected int baseBlock;
	protected int baseParry;
	protected int baseCounter;
	protected int heartbeat;
	
	protected IntArray healthTiers; // total these to receive maxHealth, maybe cache it when this changes
	protected IntArray staminaTiers; // total these to receive maxStamina, maybe cache it when this changes
	protected IntArray manaTiers; // total these to receive maxMana, maybe cache it when this changes
	protected ObjectMap<String, Integer> perks;
	
	/* morphic stats */
	private int currentHealth;
	private int currentStamina;
	private int currentMana; // mana might be replaced with spell slots that get refreshed
	
	protected Stability stability;
	protected int focus;
	protected int fortune;
	
	protected Arousal arousal;
	
	protected Ass ass;
	// public Mouth mouth; 
	protected PhallusType phallus;	
	protected Bootyliciousness bootyliciousness;
	protected int mouthful;
	protected int knotInflate;
	
	protected Stance stance;
	protected Stance oldStance;
	protected GrappleStatus grappleStatus;
	protected ObjectMap<String, Integer> statuses; // status effects will be represented by a map of Enum to Status object
	
	protected Array<Item> inventory;
	protected Array<Equipment> equipment;
	protected int food;
	protected int range;

	protected boolean wrapLegs;
	
	private transient AnimatedActor belly;
	private transient AnimatedActor cock;
	
	protected Weapon disarmedWeapon;
	
	// deprecated fields
	private Weapon weapon, rangedWeapon;
	private Armor shield, armor, legwear, underwear, headgear, armwear, footwear;
	private Accessory firstAccessory;
	private Plug plug;
	private ChastityCage cage;
	private Mouthwear mouthwear;
	@SuppressWarnings("unused")	private int lust; // legacy

	/* Constructors */
	protected AbstractCharacter() { inventory = new Array<Item>(); equipment = new Array<Equipment>(); ass = new Ass(new Sphincter(), new Rectum(), new Colon()); }
	protected AbstractCharacter(boolean defaultValues) {
		if (defaultValues) {
			secondPerson = false;
			level = 1;
			experience = 0;
			baseStrength = baseEndurance = baseAgility = basePerception = baseMagic = baseCharisma = 3;
			baseDefense = 0;
			baseLuck = 0;
			baseEvade = 0;
			baseBlock = 0;
			baseParry = 0;
			baseCounter = 0;
			healthTiers = getDefaultHealthTiers();
			staminaTiers = getDefaultStaminaTiers();
			manaTiers = getDefaultManaTiers();
			currentHealth = getMaxHealth();
			currentStamina = getMaxStamina();
			currentMana = getMaxMana();
			stability = Stability.Surefooted;
			focus = fortune = 10;
			stance = Stance.BALANCED;
			phallus = PhallusType.NORMAL;
			perks = new ObjectMap<String, Integer>();
			statuses = new ObjectMap<String, Integer>();
			grappleStatus = GrappleStatus.NULL;
			ass = new Ass(new Sphincter(), new Rectum(), new Colon());
			inventory = new Array<Item>();
			equipment = new Array<Equipment>();
		}		
	}
	
	public String getLabel() { return label; }
	public String getPhallusLabel() { return phallus.getLabel(); }	
	protected Boolean getSecondPerson() { return secondPerson; }
	public Sphincter getSphincter() { return ass.getSphincter(); }
	public Rectum getRectum() { return ass.getRectum(); }
	public Colon getColon() { return ass.getColon(); }
	public int getMaxHealth() { return getMax(healthTiers); }
	public int getMaxStamina() { return getMax(staminaTiers); }
	public int getMaxMana() { return getMax(manaTiers); }
	
	public int getRange() { return range; }
	public boolean winsGrapples() { return enemyType == EnemyEnum.SLIME; }
	public boolean kyliraAvailable() { return false; }
	public boolean trudyAvailable() { return false; }
	public boolean isImpotent() { return false; }
	
	public int getCurrentHealth() { return currentHealth; }
	protected int getCurrentStamina() { return currentStamina; }
	protected int getCurrentMana() { return currentMana; }
	public float getHealthPercent() { return currentHealth / (getMaxHealth() * 1.0f); }
	protected float getStaminaPercent() { return currentStamina / (getMaxStamina() * 1.0f); }
	protected float getBalancePercent() { return stability.getPercent(); }
	public AssetDescriptor<Texture> getBalanceDisplay() { return stability.getDisplay(); }
	protected float getManaPercent() { return currentMana / (getMaxMana() * 1.0f); }
	protected Stability getStability() { return stability; }
	public Stance getStance() { return stance; }
	public GrappleStatus getGrappleStatus() { return grappleStatus; } // should be passed into character state
	public ObjectMap<String, Integer> getStatuses() { return statuses; }
		
	protected IntArray getDefaultHealthTiers() { return new IntArray(new int[]{10, 10, 10, 10}); }
	protected IntArray getDefaultStaminaTiers() { return new IntArray(new int[]{5, 5, 5, 5}); }
	protected IntArray getDefaultManaTiers() { return new IntArray(new int[]{0}); }
	
	public AssetDescriptor<Texture> getHealthDisplay() { 
		switch (getHealthDegradation()) {
			case 0: return AssetEnum.HEALTH_ICON_0.getTexture();
			case 1: return AssetEnum.HEALTH_ICON_1.getTexture();
			case 2: return AssetEnum.HEALTH_ICON_2.getTexture();
			default: return AssetEnum.HEALTH_ICON_3.getTexture();
		}
	}
	
	public AssetDescriptor<Texture> getStaminaDisplay() { 
		switch (getStaminaDegradation()) {
			case 0: return AssetEnum.STAMINA_ICON_0.getTexture();
			case 1: return AssetEnum.STAMINA_ICON_1.getTexture();
			case 2: return AssetEnum.STAMINA_ICON_2.getTexture();
			default: return AssetEnum.STAMINA_ICON_3.getTexture();
		}
	}
	
	public AssetDescriptor<Texture> getManaDisplay() {  
		switch (4 - (int)(getManaPercent() * 100)/ 25) {
			case 0: return AssetEnum.MANA_ICON_0.getTexture();
			case 1: return AssetEnum.MANA_ICON_1.getTexture();
			case 2: return AssetEnum.MANA_ICON_2.getTexture();
			default: return AssetEnum.MANA_ICON_3.getTexture();
		}
	}
	
	public void setStance(Stance stance) { this.stance = stance; }
	public void setGrappleStatus(GrappleStatus status) { grappleStatus = status; } // for battle factory	
	public void setRange(int range) { this.range = range; }
	public Array<MutationResult> modHealth(int healthMod) { return modHealth(healthMod, ""); }
	public Array<MutationResult> modHealth(int healthMod, String cause) { 
		int healthChange = this.currentHealth;
		this.currentHealth += healthMod; 
		if (currentHealth > getMaxHealth()) {
			currentHealth = getMaxHealth();  
		}
			
		healthChange = this.currentHealth - healthChange;
		// if need to track overkill arises, can do so here - marking an overkill var with the amount of overkill
		if (this.currentHealth < 0) this.currentHealth = 0; 
		return healthChange == 0 ? new Array<MutationResult>() : new Array<MutationResult>(new MutationResult[]{new MutationResult(healthChange > 0 ? "Gained " + healthChange + " health"  + (cause.isEmpty() ? "!" : " " + cause + "!") : "You take " + -healthChange + " damage" + (cause.isEmpty() ? "!" : " " + cause + "!"), healthChange, MutationType.HEALTH)}); 
	}
	
	protected int getHealthRegen() { return getEndurance() / 3; }
	protected int getStaminaRegen() { return Math.max(getEndurance() / (isGravitied() ? 4 : 2), 0); }
	protected int getStabilityRegen() { return getAgility() / (isOily() ? 4 : 2) + perks.get(Perk.QUICKFOOTED.toString(), 0); }
	protected void setHealthToMax() { currentHealth = getMaxHealth(); }	
	protected void setStaminaToMax() { currentStamina = getMaxStamina(); }	
	protected void modStamina(int staminaMod) { this.currentStamina += staminaMod; if (currentStamina > getMaxStamina()) currentStamina = getMaxStamina(); }
	protected void setStabilityToMax() { stability = Stability.Surefooted; }
	protected void setStabilityToMin() { stability = Stability.Dazed; }
	protected void modStability(int stabilityMod) { stability = stability.shift(stabilityMod); if (stance.isIncapacitating() && !stability.isDown()) stability = Stability.Down; }
	protected void setManaToMax() { currentMana = getMaxMana(); }
	protected void modMana(int manaMod) { this.currentMana += manaMod; if (currentMana > getMaxMana()) currentMana = getMaxMana(); if (currentMana < 0) currentMana = 0; }
	protected int getStrength() { return Math.max(((baseStrength + itemBonus(Stat.STRENGTH) + getStrengthBuff()) - (getHealthDegradation() / 2 + getStaminaDegradation() / 2 + getLustDegradation() / 2))/(strengthDebuffed() ? 2 : 1), 0); }
	protected int getEndurance() { return Math.max((baseEndurance + itemBonus(Stat.ENDURANCE) + getEnduranceBuff()) - (getHealthDegradation()), 0); }
	protected int getAgility() { return Math.max((baseAgility + itemBonus(Stat.AGILITY) + getAgilityBuff()) - (getHealthDegradation() + getStaminaDegradation() + getCumInflation()), 0); }
	protected int getPerception() { return Math.max(basePerception + itemBonus(Stat.PERCEPTION), 0) + getPerceptionBuff(); }
	protected int getMagic() { return Math.max(baseMagic + itemBonus(Stat.MAGIC), 0); }
	protected int getCharisma() { return Math.max(baseCharisma + itemBonus(Stat.CHARISMA), 0); }
	public int getLewdCharisma() { return getCharisma() + perks.get(Perk.EROTIC.toString(), 0) * 2; }	
	protected int stepDown(int value) { if (value < 3) return value; else if (value < 7) return 3 + (value - 3)/2; else return 5 + (value - 7)/3; } 
	protected int getStat(Stat stat) { return stepDown(getRawStat(stat)); }
	public int getRawStat(Stat stat) {
		switch(stat) {
			case STRENGTH: return getStrength();
			case ENDURANCE: return getEndurance();
			case AGILITY: return getAgility();
			case PERCEPTION: return getPerception();
			case MAGIC: return getMagic();
			case CHARISMA: return getCharisma();
			default: return -1;
		}
	}
	
	public int getBaseStat(Stat stat) {
		switch(stat) {
			case STRENGTH: return baseStrength;
			case ENDURANCE: return baseEndurance;
			case AGILITY: return baseAgility;
			case PERCEPTION: return basePerception;
			case MAGIC: return baseMagic;
			case CHARISMA: return baseCharisma;
			default: return -1;
		}
	}
	// all the item-related buffs need to move into item bonus
	protected int getStrengthBuff() { return totalEquipEffect(EquipEffect.STR_BONUS) + statuses.get(StatusType.STRENGTH_BUFF.toString(), 0); }
	protected int getEnduranceBuff() { return statuses.get(StatusType.ENDURANCE_BUFF.toString(), 0); }
	protected int getAgilityBuff() { return totalEquipEffect(EquipEffect.AGI_BONUS) + statuses.get(StatusType.AGILITY_BUFF.toString(), 0); }
	protected int getPerceptionBuff() { return totalEquipEffect(EquipEffect.PER_BONUS); }
	protected boolean strengthDebuffed() { return statuses.get(StatusType.STRENGTH_DEBUFF.toString(), 0) > 0; }
	protected boolean isGravitied() { return statuses.get(StatusType.STRENGTH_DEBUFF.toString(), 0) > 0; }
	protected boolean isOily() { return statuses.get(StatusType.OIL.toString(), 0) > 0; }
	protected boolean isParalyzed() { return statuses.get(StatusType.PARALYSIS.toString(), 0) > 0; }
	protected boolean isHypnotized() { return statuses.get(StatusType.HYPNOSIS.toString(), 0) > 0 || hasEquipEffect(EquipEffect.HYPNOSIS); }
	protected int itemBonus(Stat stat) { return getFirstAccessory() != null && getFirstAccessory().getBoostedStat() != null && getFirstAccessory().getBoostedStat() == stat ? 1 : 0; }
	protected int getBaseDefense() { return Math.max(baseDefense, 0); }
	protected int getMagicResistance() { return 0; }
	protected int getTraction() { return 2; }
	// temporary for battle coherence
	protected int getArmorScore() { return getArmor() != null && getArmor().getDurability() > 0 ? getArmor().getShockAbsorption(): 0; }
	protected int getLegwearScore() { return getLegwear() != null ? getLegwear().getShockAbsorption() : 0; }
	protected int getUnderwearScore() { return getUnderwear() != null ? getUnderwear().getShockAbsorption() : 0; }
	public int getHealthDegradation() { return getDegradation(healthTiers, currentHealth); }
	public int getStaminaDegradation() { return getDegradation(staminaTiers, currentStamina); }
	public int getLustDegradation() { return arousal.getLust() >= 100 ? 4 : arousal.getLust() >= 75 ? 3 : arousal.getLust() >= 50 ? 2 : arousal.getLust() >= 25 ? 1 : 0; }
	protected int getCumInflation() { return ass.getFullnessAmount() >= 20 || mouthful >= 20 ? 2 : ass.getFullnessAmount() >= 10 || mouthful >= 10 || fullOfEggs() ? 1 : 0; } 
	public boolean isLewd() { return perks.get(Perk.CATAMITE.toString(), 0) > 0 || perks.get(Perk.ANAL_ADDICT.toString(), 0) > 2 || perks.get(Perk.COCK_LOVER.toString(), 0) > 7 || arousal.getLust() >= 75; }
	
	public Color getHealthColor() { return getValueColor(getHealthDegradation()); } 
	public Color getStaminaColor() { return getValueColor(getStaminaDegradation()); }
	public Color getStabilityColor() { return getValueColor(stability == Stability.Perfect ? 0 : stability.isGood() ? 1 : stability.isDown() || stability == Stability.Tripping ? 3 : 2); }
	
	protected CharacterState getCurrentState(AbstractCharacter target) { return new CharacterState(getStats(), getRawStats(), getWeapon(), getRangedWeapon(), getShield(), stability.lowBalance(), currentMana, enemyType == null ? true : enemyType.isCorporeal(), this, target); }
	protected boolean alreadyIncapacitated() { return stance.isIncapacitatingOrErotic(); }
	protected boolean wasIncapacitated() { return oldStance != null ? oldStance.isIncapacitatingOrErotic() : false; }
	protected boolean hasGrappleAdvantage() { return grappleStatus.isAdvantage(); }

 	private boolean hasEquipEffect(EquipEffect effect) { return totalEquipEffect(effect) > 0; }
	private int totalEquipEffect(EquipEffect effect) {
		int total = 0;
		for (Equipment equipped : equipment) {
			if (equipped.getEquipEffect() == effect) { total++; }
		}
		return total;
	}
	
	public String getStatusBlurb() {
		String blurb = "";
		blurb += strengthDebuffed() ? "Weakening Curse\n" : "";
		blurb += isGravitied() ? "Gravity\n" : "";
		blurb += isOily() ? "Oil\n" : "";
		blurb += isParalyzed() ? "Paralyzed\n" : "";
		blurb += isHypnotized() ? "Hypnotized\n" : "";
		switch(getHealthDegradation()) {
			case 3: blurb += "Injured (-3)\n"; break;
			case 2: blurb += "Wounded (-2)\n"; break;
			case 1: blurb += "Hurt (-1)\n"; break;
			default:
		}
		switch(getStaminaDegradation()) {
			case 3: blurb += "Breathless (-3)\n"; break;
			case 2: blurb += "Gasping (-2)\n"; break;
			case 1: blurb += "Winded (-1)\n"; break;
			default:
		}
		switch(getLustDegradation()) {
			case 4: blurb += "Cumdrunk (-2)\n"; break;
			case 2: blurb += "Lusty (-1)\n"; break;
			default:
		}
		
		return blurb;
	}
	
	public boolean outOfStamina(Technique technique) { return getStaminaMod(technique) >= currentStamina; }	
	public boolean outOfStability(Technique technique) { return checkStability(getStabilityChange(technique)).isDown(); }
	public boolean outOfStaminaOrStability(Technique technique) { return outOfStamina(technique) || outOfStability(technique); }
	public boolean lowStaminaOrStability(Technique technique) {	return getStaminaMod(technique) >= currentStamina - 5 || checkStability(getStabilityChange(technique)).lowBalance(); }
	protected boolean lowStability() { return stability.lowBalance(); }
	protected boolean isErect() { return arousal.isErect() && !isChastitied() && phallus != PhallusType.NONE; }
	public int getCurrentLust() { return arousal.getLust(); }	
	public String getDefeatMessage() { return label + (secondPerson ? " are " : " is ") + "defeated!"; }
	
	public Array<MutationResult> modFood(Integer foodMod) {
		int foodChange = food;
		food += foodMod; 
		Array<MutationResult> result = new Array<MutationResult>();
		Array<MutationResult> starve = new Array<MutationResult>();
		if (food < 0) {
			starve.addAll(modHealth(5 * food, "from starvation"));
			food = 0; 
		}

		foodChange = food - foodChange;
		
		if (foodChange != 0) {
			result.add(new MutationResult(foodChange > 0 ? "+" + foodChange + " fullness!" : "Hunger increases by " + -foodChange + "!", foodChange, MutationType.FOOD));
		}
		result.addAll(starve);
		return result;
	}
	
	public int getBleed() { return statuses.get(StatusType.BLEEDING.toString(), 0); }
	protected int getClimaxVolume() { return 3; }
	
	public Weapon getWeapon() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Weapon && ((Weapon) item).isMelee()) return (Weapon)item; } return null; }
	public Weapon getRangedWeapon() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Weapon && !((Weapon) item).isMelee()) return (Weapon)item; } return null; }
	public Armor getArmor() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Armor && ((Armor) item).coversTop()) return (Armor)item; } return null; }
	public Armor getLegwear() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Armor && ((Armor) item).coversBottom()) return (Armor)item; } return null; }
	public Armor getUnderwear() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Armor && ((Armor) item).isUnderwear()) return (Armor)item; } return null; }
	public Armor getShield() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Armor && ((Armor) item).isShield()) return (Armor)item; } return null; }
	public Armor getHeadgear() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Armor && ((Armor) item).isHeadgear()) return (Armor)item; } return null; }
	public Armor getArmwear() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Armor && ((Armor) item).isArmwear()) return (Armor)item; } return null; }
	public Armor getFootwear() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Armor && ((Armor) item).isFootwear()) return (Armor)item; } return null; }
	public Accessory getFirstAccessory() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Accessory) return (Accessory)item; } return null; }
	public Plug getPlug() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Plug) return (Plug)item; } return null; }
	public ChastityCage getCage() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof ChastityCage) return (ChastityCage)item; } return null; }
	public Mouthwear getMouthwear() { cleanEquipment(); for (Equipment item : equipment) { if(item instanceof Mouthwear) return (Mouthwear)item; } return null; }

	public String equip(Equipment item) {
		if (item == null) return "";
		String result = canBeEquipped(item);
		if (result.equals("")) {
			result = "You equipped the " + item.getName() + ".\n";
			if (item instanceof Weapon) {
				if (((Weapon)item).isMelee()) result += unequip(getWeapon());
				else result += unequip(getRangedWeapon());
			}
			if (item instanceof Armor) {
				Armor armor = (Armor) item;
				if (armor.coversTop()) result += unequip(getArmor());
				if (armor.coversBottom()) result += unequip(getLegwear());
				if (armor.isUnderwear()) result += unequip(getUnderwear());
				if (armor.isShield()) result += unequip(getShield());
				if (armor.isHeadgear()) result += unequip(getHeadgear());
				if (armor.isArmwear()) result += unequip(getArmwear());
				if (armor.isFootwear()) result += unequip(getFootwear());
			}
			if (item instanceof Accessory) { result += unequip(getFirstAccessory()); }
			if (item instanceof Plug) {
				Plug plug = (Plug) item;
				if (plug.isPlug()) { ass.togglePlug();	}
				result += unequip(getPlug());
			}
			if (item instanceof ChastityCage) {
				if (this.cock != null) {
					this.cock.setSkeletonSkin(isChastitied() ? "Cage" : phallus.getSkin());
				}
				result += unequip(getCage());
			}
			if (item instanceof Mouthwear) { result += unequip(getMouthwear()); }
			
			equipment.add(item);
			inventory.removeValue(item, true);		
		}
		
		return result;
	}
	
	private String canBeEquipped(Equipment item) {
		String result = "";
		if (item instanceof Weapon) {
			if (((Weapon)item).isMelee() && isCursed(getWeapon())) result = "The " + getName(getWeapon()) + " is cursed and cannot be removed!";
			else if (isCursed(getRangedWeapon())) result = "The " + getName(getRangedWeapon()) + " is cursed and cannot be removed!";
		}
		if (item instanceof Armor) {
			Armor armor = (Armor) item;
			if (armor.coversTop() && isCursed(getArmor())) result = "The " + getName(getArmor()) + " is cursed and cannot be removed!";
			if (armor.coversBottom() && isCursed(getLegwear())) result = "The " + getName(getLegwear()) + " is cursed and cannot be removed!";
			if (armor.isUnderwear() && isCursed(getUnderwear())) result +=result = "The " + getName(getUnderwear()) + " is cursed and cannot be removed!";
			if (armor.isShield() && isCursed(getShield())) result += result = "The " + getName(getShield()) + " is cursed and cannot be removed!";
			if (armor.isHeadgear() && isCursed(getHeadgear())) result += result = "The " + getName(getHeadgear()) + " is cursed and cannot be removed!";
			if (armor.isArmwear() && isCursed(getArmwear())) result += result = "The " + getName(getArmwear()) + " is cursed and cannot be removed!";
			if (armor.isFootwear() && isCursed(getFootwear())) result += result = "The " + getName(getFootwear()) + " is cursed and cannot be removed!";
		}
		if (item instanceof Accessory) { if (isCursed(getFirstAccessory())) result = "The " + getName(getFirstAccessory()) + " is cursed and cannot be removed!"; }
		if (item instanceof Plug) {	if (isCursed(getPlug())) result = "The " + getName(getPlug()) + " is cursed and cannot be removed!"; }
		if (item instanceof ChastityCage) { 
			if (isCursed(getCage())) result = "The " + getName(getCage()) + " is cursed and cannot be removed!"; 
			if (getCage() != null && !hasKey()) result = "You cannot remove your chastity cage without a key!";
		}
		if (item instanceof Mouthwear) { if (isCursed(getMouthwear())) result = "The " + getName(getMouthwear()) + " is cursed and cannot be removed!"; }
		
		return result;
	}
	
	private boolean isCursed(Equipment equipment) { return equipment != null && equipment.isCursed(); }
	private String getName(Equipment equipment) { return equipment != null ? equipment.getName() : ""; }	
	
	public String unequip(Equipment item) {
		if (item == null) return "";
		if (item instanceof ChastityCage && getCage() != null && !hasKey()) return "You cannot remove your chastity cage without a key!";
		if (item.isCursed()) return "You cannot remove a cursed item!";
		String result = equipment.contains(item, true) ? "You unequipped the " + item.getName() + "." : ""; 
		equipment.removeValue(item, true);		
		inventory.add(item);
		return result + "\n";
	}
	
	public String unequipWeapon() { return unequip(getWeapon()); }
	public String unequipRangedWeapon()  { return unequip(getRangedWeapon()); }
	public String unequipShield() { return unequip(getShield()); }
	public String unequipArmor() { return unequip(getArmor()); }
	public String unequipLegwear() { return unequip(getLegwear()); }	
	public String unequipUnderwear() { return unequip(getUnderwear()); }	
	public String unequipHeadgear() { return unequip(getHeadgear()); }
	public String unequipArmwear() { return unequip(getArmwear()); }	
	public String unequipFootwear() { return unequip(getFootwear()); }
	public String unequipAccessory() { return unequip(getFirstAccessory()); }
	public String unequipPlug() { return unequip(getPlug()); }
	public String unequipCage() { Misc key = new Misc(MiscType.KEY); inventory.add(key); String result = unequip(getCage()); inventory.removeValue(key, true); return result; }
	public String unequipMouthwear() { return unequip(getMouthwear()); }
	
	public boolean isPlugged() { return getPlug() != null && getPlug().isPlug(); }
	public boolean isChastitied() { return getCage() != null; }
	protected boolean hasKey() { return inventory.contains(new Misc(MiscType.KEY), false); }
	
	protected int getDegradation(IntArray tiers, int currentValue) {
		int numTiers = tiers.size;
		int value = currentValue;
		for (int tier : tiers.items) {
			value -= tier;
			numTiers--;
			if (value <= 0) return numTiers;
		}
		return numTiers;
	}
	
	// this method can be removed, as the CharacterState could dictate what modifiers are applied to the stamina cost of a technique
	protected int getStaminaMod(Technique technique) {
		int staminaMod = technique.getStaminaCost() * (isGravitied() ? 2 : 1);
		if (staminaMod >= 0) {
			staminaMod -= getStaminaRegen();
			if (staminaMod < 0) staminaMod = 0;
		}
		else {
			staminaMod -= getStaminaRegen();
		}
		return staminaMod;
	}
	
	// right now this and "doAttack" handle once-per-turn character activities
	public void extractCosts(Technique technique) {
		oldStance = stance;
		setStance(!technique.getStance().isNull() ? technique.getStance() : stance);
		if (oldStance != Stance.PRONE && oldStance != Stance.SUPINE && (stance == Stance.PRONE || stance == Stance.SUPINE)) {
			setStabilityToMin();
		}
		
		int staminaMod = getStaminaMod(technique); 
		modStamina(-staminaMod);
		modStability(getStabilityChange(technique));
		modMana(-technique.getManaCost());
		
		Array<String> toRemove = new Array<String>();
		// statuses degrade with time in a general way currently
		for(String key: statuses.keys()) {
			StatusType type = StatusType.valueOf(key);
			if (!type.degrades()) continue;
			int value = statuses.get(key) - 1;
			statuses.put(key, value);
			if (value <= 0) {
				toRemove.add(key);
			}
		}
		for(String key: toRemove) {
			statuses.remove(key);
		}
	}
		
	public Attack doAttack(Attack resolvedAttack) {
		heartbeat++;
		int bleedDamage = getBloodLossDamage();
		if (bleedDamage > 0) {
			resolvedAttack.addAttackerResults(modHealth(-getBloodLossDamage()));
			resolvedAttack.addMessageToAttacker(new MutationResult(label + (secondPerson ? " bleed" : " bleeds") + " out for " + getBloodLossDamage() + " damage!", -getBloodLossDamage(), MutationType.HEALTH));
		}
		
		if (!resolvedAttack.isSuccessful()) {
			resolvedAttack.addMessageToAttacker(new MutationResult(resolvedAttack.getUser() + " used " + resolvedAttack.getName() + (resolvedAttack.getStatus() == Status.MISSED ? " but missed!" : (resolvedAttack.getStatus() == Status.EVADED ? " but was evaded!" : resolvedAttack.getStatus() == Status.FIZZLE ? " but the spell fizzled!" : "! FAILURE!"))));
			
			if ((resolvedAttack.getStatus() == Status.MISSED || resolvedAttack.getStatus() == Status.EVADED) && enemyType == EnemyEnum.HARPY && stance == Stance.FELLATIO && resolvedAttack.getForceStance() == Stance.FELLATIO_BOTTOM) {
				resolvedAttack.addMessageToAttacker(new MutationResult(properCase(pronouns.getNominative()) + " crashes to the ground!", Stance.PRONE));
				setStance(Stance.PRONE);
			}
			else if(resolvedAttack.getForceStance() != null) {
				setStance(oldStance);
			}	
			return resolvedAttack;
		}
		
		if (resolvedAttack.getItem() != null) {
			UseItemEffect effect = consumeItem(resolvedAttack.getItem(), true);
			resolvedAttack.addMessageToAttacker(effect.results.size > 0 && effect.results.get(0).getType() != MutationType.NONE ? new MutationResult(effect.resultDisplay, effect.results.get(0).getMod(), effect.results.get(0).getType()) : new MutationResult(effect.resultDisplay));
			resolvedAttack.addAttackerResults(effect.results);
			resolvedAttack.addAttackerResults(new Array<MutationResult>(new MutationResult[]{new MutationResult("You used a " + resolvedAttack.getItem().getName() + ".")}));
		}
		else if (!resolvedAttack.isAttack() && !resolvedAttack.isClimax() && resolvedAttack.getSex().isEmpty()) {
			resolvedAttack.addMessageToAttacker(new MutationResult(resolvedAttack.getUser() + " used " + resolvedAttack.getName() + "!"));
		}
		
		if (resolvedAttack.isSpell() && resolvedAttack.getSpellEffect() == SpellEffect.ARMOR_REPAIR) {
			resolvedAttack.addMessageToAttacker(repairArmor(getMagic() * 4));
		}
		
		if (resolvedAttack.isHealing()) {
			resolvedAttack.addAttackerResults(modHealth(resolvedAttack.getHealing()));
			resolvedAttack.addMessageToAttacker(new MutationResult(resolvedAttack.getUser() + " heal" + (secondPerson ? "" : "s" ) + " for " + resolvedAttack.getHealing()+"!", resolvedAttack.getHealing(), MutationType.HEALTH));
		}
		Buff buff = resolvedAttack.getSelfEffect();
		if (buff != null) {
			statuses.put(buff.type.toString(), buff.power);
		}
		if (enemyType != null) {
			for (MutationResult mr : enemyType.getEroticMessages(resolvedAttack.getForceStance(), pronouns)) {
				resolvedAttack.addMessageToDefender(mr);
			}
			
			if (resolvedAttack.getForceStance() == Stance.FACE_SITTING_BOTTOM) {
				resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getNominative()) + " rides your face!"));
				resolvedAttack.addMessageToDefender(new MutationResult("You receive a faceful of ass!"));
			}
			else if (resolvedAttack.getForceStance() == Stance.KNOTTED_BOTTOM) {
				if (knotInflate == 0) {
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getPossessive()) + " powerful hips try to force something big inside!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You struggle... but can't escape!"));
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getPossessive()) + " grapefruit-sized knot slips into your rectum!  You take 4 damage!", -4, MutationType.HEALTH));
					resolvedAttack.addMessageToDefender(new MutationResult("You learned about Anatomy(Wereslut)! You are being bred!"));
					resolvedAttack.addMessageToDefender(new MutationResult("Your anus is permanently stretched!"));
				}
				else if (knotInflate < 3) {
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getPossessive()) + " tremendous knot is still lodged in your rectum!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You can't dislodge it; it's too large!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You're drooling!"));
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getPossessive()) + " fat thing is plugging your shithole!"));					
				}
				else {
					resolvedAttack.addMessageToDefender(new MutationResult("The battle is over, but your ordeal has just begun!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You are about to be bred like a bitch!"));
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getNominative()) + "'s going to ejaculate her runny dog cum in your bowels!"));	
				}
				knotInflate++;
			}
			else if (resolvedAttack.getForceStance() == Stance.MOUTH_KNOTTED_BOTTOM) {
				if (knotInflate == 0) {
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getPossessive()) + " powerful hips try to force something big inside!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You struggle... but can't escape!"));
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getPossessive()) + " melon-sized knot forces your jaw open! You take 4 damage!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You learned about Anatomy(Wereslut)! You are being bred!"));
				}
				else if (knotInflate < 3) {
					resolvedAttack.addMessageToDefender(new MutationResult(properCase(pronouns.getPossessive()) + " tremendous knot is still stuck behind your teeth!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You can't dislodge it; it's too large!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You're drooling!"));					
				}
				else {
					resolvedAttack.addMessageToDefender(new MutationResult("The battle is over, but your ordeal has just begun!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You are about to swallow doggy cum!"));
				}
				knotInflate++;
			}
			else if (resolvedAttack.getForceStance() == Stance.OVIPOSITION_BOTTOM) {
				if (knotInflate >= 3) {
					resolvedAttack.addMessageToDefender(new MutationResult("The battle is over, but your ordeal has just begun!"));
					resolvedAttack.addMessageToDefender(new MutationResult("You are full of " + pronouns.getPossessive() + " eggs!"));
				}
				knotInflate++;
			}
		}
		
		// all climax logic should go here
		if (resolvedAttack.isClimax()) {
			resolvedAttack.addMessageToAttacker(new MutationResult(climax()));
			if (this.cock != null && !isImpotent()) {
				this.cock.setAnimation(0, "EdgingToClimax", false);
				this.cock.addAnimation(0, "ClimaxToFlaccid", false, 4);
			}
		}
		
		for (Bonus bonus : resolvedAttack.getBonuses()) {
			String bonusDescription = bonus.getDescription(label);
			if (bonusDescription != null) {
				resolvedAttack.addMessageToAttacker(new MutationResult(bonusDescription));
			}
		}
		
		return resolvedAttack;
	}
	private Armor getArmorHit(AttackHeight height) {
		return height == AttackHeight.HEAD ? getHeadgear() : height == AttackHeight.FOOT ? getFootwear() : height == AttackHeight.ARM ? getArmwear() : height != AttackHeight.LOW ? getArmor() : getLegwear() != null && getLegwear().getShockAbsorption() > 0 ? getLegwear() : getUnderwear();
	}
	
	// return an array of array of strings and mutation results packaged together, save the mutation results into the battle results but that doesn't work either because doAttack can also cause mutations
	public AttackResult receiveAttack(Attack attack) {
		Array<MutationResult> resultToAttacker = attack.getToAttackerMessages();
		Array<MutationResult> resultToDefender = attack.getToDefenderMessages();
		boolean knockedDown = false;
		grappleStatus = attack.getGrapple();
		
		modRange(-attack.getAdvance());
		
		if (attack.isSuccessful()) {
			if (attack.getForceStance() == Stance.DOGGY_BOTTOM && bootyliciousness != null) { resultToDefender.add(new MutationResult("They slap their hips against your " + bootyliciousness.toString().toLowerCase() + " booty!")); }
			if (attack.isAttack() || attack.isClimax() || !attack.getSex().isEmpty()) { resultToAttacker.add(new MutationResult(attack.getUser() + " used " + attack.getName() +  " on " + (attack.isKylira() ? "Kylira" : attack.isTrudy() ? "Trudy" : secondPerson ? label.toLowerCase() : label) + "!")); }
			if (attack.getForceStance() == Stance.BALANCED) { 
				resultToDefender.add(new MutationResult(attack.getUser() + " broke free!", Stance.BALANCED));
				if (stance == Stance.FELLATIO_BOTTOM) { resultToDefender.add(new MutationResult("It slips out of your mouth and you get to your feet!", Stance.BALANCED)); }
				else if (stance == Stance.SIXTY_NINE_BOTTOM) { resultToDefender.add(new MutationResult("You spit out their cock and push them off!", Stance.BALANCED)); }
				else if (stance == Stance.HANDY_BOTTOM) {}
				else if (stance.isAnalReceptive()) { resultToDefender.add(new MutationResult("It pops out of your ass and you get to your feet!", Stance.BALANCED)); }
			}
			
			Buff buff = attack.getEnemyEffect();
			if (buff != null) { statuses.put(buff.type.toString(), buff.power); }

			int shieldDamage = attack.getShieldDamage();
			
			int blockMod = attack.getBlockAmount();
			if (blockMod > 0) {				
				if (getShield() != null && getShield().getDurability() > 0) {
					resultToDefender.add(new MutationResult((blockMod >= 4 ? "The blow strikes off the shield!" : blockMod >= 3 ? "The blow is mostly blocked by the shield!" : blockMod >= 2 ? "The blow is half-blocked by the shield!" : "The blow is barely blocked by the shield!") + "\nIt deals " + shieldDamage + " damage to it!", shieldDamage, MutationType.ARMOR_DESTROY));
					getShield().modDurability(-shieldDamage);
					if (getShield().getDurability() == 0) resultToDefender.add(new MutationResult("The shield is broken!"));
				}
			}
			
			int parryMod = attack.getParryAmount();
			// ICON: should display amount of damage parried (getParryReduction) with parry icon
			if (parryMod > 0) { resultToDefender.add(new MutationResult((parryMod >= 4 ? "The blow is parried away!" : parryMod >= 3 ? "The blow is mostly deflected by a parry!" : parryMod >= 2 ? "The blow is half-deflected by a parry!" : "The blow is barely blocked by a parry!"))); }
			// ICON: should display amount of damage evaded (getDodgeReduction) with evade icon
			boolean oilyFire = attack.isSpell() && attack.getSpellEffect() == SpellEffect.FIRE_DAMAGE && isOily();
			if (oilyFire) statuses.put(StatusType.OIL.toString(), 0); 
			int damage = attack.getDamage() * (oilyFire ? 2 : 1);
			Armor hitArmor = getArmorHit(attack.getAttackHeight());
			if (!attack.ignoresArmor() && damage > 0) {
				if (getBaseDefense() > 0) resultToDefender.add(new MutationResult("Damage reduced by " + Math.min(damage, getBaseDefense()) + "!"));
				// ICON: should display amount of damage that hits armor along with in-tact armor icon				
				damage -= getBaseDefense() + (hitArmor != null ? hitArmor.getShockAbsorption() : 0);
				// add daze (status effect that reduces perception and agility)
			}
			if (attack.getMagicDamageReduction() > 0) { resultToDefender.add(new MutationResult("Magic resistance reduced damage by " + attack.getMagicDamageReduction() + "!")); }
			
			if (damage > 0) {	
				attack.addDefenderResults(modHealth(-damage));
				if (attack.isSpell()) {
					if (oilyFire) { resultToDefender.add(new MutationResult("The fire ignites the oil!")); }
					resultToDefender.add(new MutationResult("The magic strikes for " + damage + " damage!", -damage, MutationType.HEALTH));
				}
				else { resultToDefender.add(new MutationResult("The blow strikes for " + damage + " damage!", -damage, MutationType.HEALTH)); }
				
				if (!(attack.ignoresArmor() || ((hitArmor == null || hitArmor.getDurability() == 0)))) { resultToDefender.add(new MutationResult("The blow strikes off the armor!")); }
			}
			
			if (attack.ignoresArmor() || ((hitArmor == null || hitArmor.getDurability() == 0))) {
				int bleed = attack.getBleeding();
				if (bleed > 0 && canBleed()) {
					resultToDefender.add(new MutationResult("It opens wounds! +" + bleed + " blood loss!", bleed, MutationType.BLEED));
					statuses.put(StatusType.BLEEDING.toString(), statuses.get(StatusType.BLEEDING.toString(), 0) + bleed);
				}
			}
			
			int plugRemove = attack.plugRemove();
			if (plugRemove > 0) {
				if (getLegwear() != null && getLegwear().getShockAbsorption() > 0 && getLegwear().coversAnus()) {
					resultToDefender.add(new MutationResult("They pull down your " + getLegwear().getName() + "!"));
					unequipLegwear();
				}
				else if (getUnderwear() != null && getUnderwear().getShockAbsorption() > 0 && getUnderwear().coversAnus()) {
					resultToDefender.add(new MutationResult("They pull down your " + getUnderwear().getName() + "!"));
					unequipUnderwear();
				}
				else {
					resultToDefender.add(new MutationResult("They pull out your " + getPlug().getName() + "!"));
					unequipPlug();
				}
			}
			
			int knockdown = attack.getForce();
			knockdown -= getTraction();
			knockdown = Stability.getKnockdownConversion(knockdown);
			if (knockdown > 0) {
				if (!alreadyIncapacitated() && !wasIncapacitated()) {
					resultToDefender.add(new MutationResult("It's a solid blow! It reduces balance by " + knockdown + "!", -knockdown, MutationType.BALANCE));
					if (stability.isDown()) {
						if (enemyType == EnemyEnum.OGRE) {
							resultToDefender.add(new MutationResult(label + (secondPerson ? " are " : " is ") + "knocked to their knees!", Stance.KNEELING));
							stability = Stability.Teetering;
							setStance(Stance.KNEELING);
						}
						else {
							resultToDefender.add(new MutationResult(label + (secondPerson ? " are " : " is ") + "knocked to the ground!", Stance.SUPINE));
							setStabilityToMin();
							setStance(Stance.SUPINE);							
						}
						knockedDown = true;
					}
				}
			}
			
			int trip = attack.getTrip();
			if (trip >= 100) {
				if (!alreadyIncapacitated()) {
					setStabilityToMin();
					setStance(Stance.PRONE);
					resultToDefender.add(new MutationResult(label + (secondPerson ? " are " : " is ") + "tripped and "+ (secondPerson ? "fall" : "falls") +" prone!", Stance.PRONE));
					knockedDown = true;
				}
			}
			
			int armorSunder = attack.getArmorSunder();
			if (armorSunder > 0) {
				if (hitArmor != null && hitArmor.getDurability() > 0) {
					resultToDefender.add(new MutationResult("It's an armor shattering blow! It reduces " + hitArmor.getName() + " durability by " + (armorSunder > hitArmor.getDurability() ? hitArmor.getDurability() : armorSunder) + "!", -(armorSunder > hitArmor.getDurability() ? hitArmor.getDurability() : armorSunder), MutationType.ARMOR_DESTROY));
					hitArmor.modDurability(-armorSunder);
					if (hitArmor.getDurability() == 0) resultToDefender.add(new MutationResult("The " + hitArmor.getName() + " is broken!"));
				}
			}
			
			int gutcheck = attack.getGutCheck();
			if (gutcheck > 0) {
				if (!alreadyIncapacitated()) {
					currentStamina -= gutcheck; // this should be reduced by armor defense
					if (currentStamina < -5) currentStamina = -5;
					resultToDefender.add(new MutationResult("It's winds " + (secondPerson ? "you" : "them") + "! It reduces stamina by " + gutcheck + "!", -gutcheck, MutationType.STAMINA));
					if (currentStamina <= 0 && grappleStatus == GrappleStatus.NULL) {
						resultToDefender.add(new MutationResult(label + (secondPerson ? " fall " : " falls ") + "to the ground!", Stance.PRONE));
						setStabilityToMin();
						setStance(Stance.PRONE);
						knockedDown = true;
					}
				}
			}
			
			int disarm = attack.getDisarm();
			if (disarm >= 100 && disarm()) { resultToDefender.add(new MutationResult((secondPerson ? "You are " : label + " is ") + "disarmed!")); }
			
			Stance forcedStance = attack.getForceStance();
			if (forcedStance != null) {
				if (stance != forcedStance) { 
					resultToDefender.add(new MutationResult(label + (secondPerson ? " are " : " is ") + "forced into " + forcedStance.getLabel() + " stance!", forcedStance));
					setStance(forcedStance);
					if (forcedStance == Stance.PRONE || forcedStance == Stance.SUPINE) {
						setStabilityToMin();
					}
					else if (forcedStance == Stance.KNEELING && stability.isGood()) {
						stability = Stability.Unstable;
					}
				}
			}
			SexualExperience sex = attack.getSex();
			SexualExperience selfSex = attack.getSelfSex();

			// get mutation results from this lust stuff
			if ((enemyType != EnemyEnum.GOLEM || arousal.isErect()) && enemyType != EnemyEnum.QUETZAL) {
				if (!sex.isEmpty() || !selfSex.isEmpty()) {
					int formerLust = arousal.getLust();
					resultToDefender.addAll(increaseLust(sex, selfSex));
					int lustChange = arousal.getLust() - formerLust;
					if (sex.isTeasing()) resultToDefender.add(new MutationResult(label + (secondPerson ? " are seduced" : " is seduced") + "! " + (lustChange > 0 ? ((secondPerson ? " Your " : " Their ") + "lust raises by " + lustChange + "!") : (secondPerson ? " You " : " They ") + "cum!")));
				}	
			}
			else if (enemyType == EnemyEnum.QUETZAL) { resultToDefender.addAll(increaseLust(selfSex)); }
			
			String internalShotText = null;
			if (attack.getClimaxType() == ClimaxType.ANAL) {
				Array<MutationResult> temp = fillButt(attack.getClimaxVolume());
				attack.addDefenderResults(temp);		
				if (temp.size > 0) internalShotText = temp.first().getText();
			}
			else if (attack.getClimaxType() == ClimaxType.ORAL) {
				Array<MutationResult> temp = fillMouth(1);
				attack.addDefenderResults(temp);	
				if (temp.size > 0) internalShotText = temp.first().getText();
			}
			if (internalShotText != null) resultToDefender.add(new MutationResult(internalShotText));
			
			if (ass.getFullnessAmount() > 0 && !stance.isAnalReceptive()) resultToDefender.add(new MutationResult(getLeakMessage()));
			if (mouthful > 0 && !stance.isOralReceptive()) resultToDefender.add(new MutationResult(getDroolMessage()));
		}

		if (!alreadyIncapacitated() && !knockedDown) {
			if (enemyType == EnemyEnum.OGRE) {
				if (stability.isDown()) {
					setStance(Stance.KNEELING);
					resultToDefender.add(new MutationResult(label + (secondPerson ? " lose your" : " loses their") + " footing and " + (secondPerson ? "trip" : "trips") + "!", Stance.KNEELING));
					stability = Stability.Teetering;
				}
				// you blacked out
				else if (currentStamina <= 0) {
					resultToDefender.add(new MutationResult(label + (secondPerson ? " run " : " runs ") + "out of breath and " + (secondPerson ? "collapse" : "collapses") + "!", Stance.KNEELING));
					setStance(Stance.KNEELING);
					stability = Stability.Teetering;
				}
			}
			else {
				// you tripped
				if (stability.isDown() && grappleStatus == GrappleStatus.NULL) {
					setStance(Stance.PRONE);
					resultToDefender.add(new MutationResult(label + (secondPerson ? " lose your" : " loses their") + " footing and " + (secondPerson ? "trip" : "trips") + "!", Stance.PRONE));
					setStabilityToMin();
				}
				// you blacked out
				else if (currentStamina <= 0 && grappleStatus == GrappleStatus.NULL) {
					resultToDefender.add(new MutationResult(label + (secondPerson ? " run " : " runs ") + "out of breath and " + (secondPerson ? "collapse" : "collapses") + "!", Stance.SUPINE));
					setStance(Stance.SUPINE);
					setStabilityToMin();
				}
			}
		}
		
		updateDisplay();
		
		// currently this returns something with the plain text results, the dialog, the attacker results, and the defender results - only the defender results for the enemy attack and the attacker results from the player's attack are used for reporting end of combat things, but should also be used for 
		// the combat log itself - the plain text results should similarly be split between attacker and defender results, thus giving the 2X2 matrix of outputs - attacker-attacker, attacker-defender, defender-attacker, defender-defender
		return new AttackResult(resultToAttacker, resultToDefender, new Array<String>(), attack.getAttackerResults(), attack.getDefenderResults());
	}
	
	protected void updateDisplay() {}
	
	protected Array<MutationResult> increaseLust(SexualExperience ... sexes) {
		Array<MutationResult> results = new Array<MutationResult>();
		String spurt = "";
		String oldArousal = arousal.getCurrentState();
		results.addAll(arousal.increaseArousal(sexes, perks));
		if (this.cock != null) {
			if (isImpotent()) {
				this.cock.setAnimation(0, "Flaccid", false);
			}
			else if (!oldArousal.equals(arousal.getCurrentState())) {
				this.cock.setAnimation(0, oldArousal + "To" + arousal.getCurrentState(), false);
			}			
		}
		
		if (arousal.isClimax() && stance.isEroticReceptive()) {
			spurt = climax();
			if (this.cock != null && !arousal.isClimax() && !isImpotent()) {
				this.cock.setAnimation(0, "EdgingToClimax", false);
				this.cock.addAnimation(0, "ClimaxToFlaccid", false, 4);
			}
		}
		if (!spurt.isEmpty()) { results.add(new MutationResult(spurt)); }
		return results;
	}
	
	protected abstract String climax();
	protected boolean canBleed() { return true; }
	
	protected Array<MutationResult> getResult(String text) { return new Array<MutationResult>(new MutationResult[]{new MutationResult(text)}); }
	protected Array<MutationResult> getResult(String text, int mod, MutationType type) { return new Array<MutationResult>(new MutationResult[]{new MutationResult(text, mod, type)}); }
	
	public UseItemEffect consumeItem(Item item) { return consumeItem(item, false); }
	public UseItemEffect consumeItem(Item item, boolean combatUse) {
		ItemEffect effect = item.getUseEffect();
		Array<MutationResult> results = new Array<MutationResult>();
		if (effect == null) { return new UseItemEffect("Item cannot be used.", results); }
		String result = "";
		switch (effect.getType()) {
			case HEALING:
				int currentHealth = getCurrentHealth();
				results.addAll(modHealth(effect.getMagnitude()));
				result = "You used " + item.getName() + " and restored " + String.valueOf(getCurrentHealth() - currentHealth) + " health!";
				break;
			case MANA:
				int currentMana = getCurrentMana();
				modMana(effect.getMagnitude());
				result = "You used " + item.getName() + " and restored " + String.valueOf(getCurrentHealth() - currentMana) + " mana!";
				break;
			// this should perform buff stacking if need be - but these item buffs should be permanent until consumed
			case BONUS_STRENGTH:
				result = "You used " + item.getName() + " and temporarily increased Strength by " + effect.getMagnitude() + "!";
				statuses.put(StatusType.STRENGTH_BUFF.toString(), effect.getMagnitude());
				break;
			case BONUS_ENDURANCE:
				result = "You used " + item.getName() + " and temporarily increased Endurance by " + effect.getMagnitude() + "!";
				statuses.put(StatusType.ENDURANCE_BUFF.toString(), effect.getMagnitude());
				break;
			case BONUS_AGILITY:
				result = "You used " + item.getName() + " and temporarily increased Agility by " + effect.getMagnitude() + "!";
				statuses.put(StatusType.AGILITY_BUFF.toString(), effect.getMagnitude());
				break;
			case MEAT:
				result = "You ate the " + item.getName() + "! Hunger decreased by 5.";
				results.addAll(modFood(effect.getMagnitude()));
				break;
			case SPIDER:
				result = "You ate the " + item.getName() + "?! WHY?! Hunger decreased by 5, uhhhhhhh?";
				results.addAll(modFood(effect.getMagnitude()));
				break;
			case SLIME:
				result = "You ate the " + item.getName() + ", temporarily increasing defense by " + effect.getMagnitude() + ".";
				baseDefense += effect.getMagnitude();
			case BANDAGE:
				int currentBleed = statuses.get(StatusType.BLEEDING.toString(), 0);
				if (currentBleed != 0) {
					result = "You applied the " + item.getName() + " to staunch bleeding by " + Math.max(currentBleed, effect.getMagnitude()) + "!";
					statuses.put(StatusType.BLEEDING.toString(), Math.max(currentBleed - effect.getMagnitude(), 0));
				}
				break;
			case MAGIC:
			case KNOCKDOWN:
			case ARMOR_SUNDER:
				result = combatUse ? "You used " + (effect.getType() == EffectType.MAGIC ? "the frozen flame" : effect.getType() == EffectType.KNOCKDOWN ? "the wind scroll" : "the acid scroll") + "!" : "Nothing happened!"; 
				break;
			case TOWN_PORTAL:
				result = !combatUse ? "You used the Town Portal Scroll!" : "Nothing happened!"; 
				break;
			default:
				result = "Nothing happened!";
		}	
		if ((!combatUse && effect.getType() == EffectType.TOWN_PORTAL) || (effect.getType() != EffectType.GEM && (combatUse || (effect.getType() != EffectType.MAGIC && effect.getType() != EffectType.KNOCKDOWN && effect.getType() != EffectType.ARMOR_SUNDER)))) {
			inventory.removeValue(item, true);
		}
		
		return new UseItemEffect(result, results);
	}
	
	public boolean disarm() {
		if (getWeapon() != null && getWeapon().isDisarmable()) {
			disarmedWeapon = getWeapon();
			equipment.removeValue(getWeapon(), true);
			return true;
		}
		return false;
		
	}
	protected Array<MutationResult> fillMouth(int mouthful) {
		this.mouthful += mouthful;
		return new Array<MutationResult>();
	}
	protected Array<MutationResult> fillButt(int buttful) {
		ass.fillButtWithCum(buttful);
		return new Array<MutationResult>();
	}
	
	protected void drainMouth() { mouthful = 0; }
	protected void drainButt() { ass.fillButtWithCum(-1); }
	protected abstract String getLeakMessage();
	protected abstract String getDroolMessage();
	protected boolean fullOfEggs() { return false; }
	
	public AnimatedActor getBelly(AssetManager assetManager) {
		if (this.belly == null) {
			this.belly = assetManager.get(AssetEnum.BELLY_ANIMATION.getAnimation()).getInstance();
			ass.setBelly(belly);
		}
		return this.belly;
	}
	
	public AnimatedActor getCock(AssetManager assetManager) {
		if (this.cock == null) {
			this.cock = assetManager.get(AssetEnum.DONG_ANIMATION.getAnimation()).getInstance();
			this.cock.setSkeletonSkin(isChastitied() ? "Cage" : phallus.getSkin());
			this.cock.setAnimation(0, isImpotent() ? "Flaccid" : arousal.getCurrentState(), false);
		}
		return this.cock;
	}
	
	protected OrderedMap<Stat, Integer> getStats() {
		OrderedMap<Stat, Integer> stats = new OrderedMap<Stat, Integer>();
		for (Stat stat: Stat.values()) {
			stats.put(stat, getStat(stat));
		}
		return stats;
	}
	
	protected ObjectMap<Stat, Integer> getRawStats() {
		ObjectMap<Stat, Integer> stats = new ObjectMap<Stat, Integer>();
		for (Stat stat: Stat.values()) {
			stats.put(stat, getRawStat(stat));
		}
		return stats;
	}
	
	public MutationResult getStanceTransform(Technique firstTechnique) {
		Stance newStance = firstTechnique.getStance();
		if (newStance.isNull() || (stance != null && stance == newStance)) {
			return new MutationResult("");
		}
		String stanceTransform = newStance.getLabel();
		String vowels = "aeiou";
		String article = vowels.indexOf(Character.toLowerCase(stanceTransform.charAt(0))) != -1 ? "an" : "a";
		return new MutationResult(label + " adopt" + (secondPerson ? "" : "s") + " " + article + " " + stanceTransform + " stance! ", newStance);
 	}
	
	public Technique getEmptyTechnique(AbstractCharacter target) { return new Technique(Techniques.DO_NOTHING.getTrait(), getCurrentState(target), 1); }
	
	protected Array<Techniques> getHypnotizedTechniqueOptions(AbstractCharacter target) {
		Array<Techniques> possibles = new Array<Techniques>();
		switch(stance) {
			case BLITZ: return getTechniques(target, HOLD_BACK);
			case COUNTER: return getTechniques(target, RIPOSTE);
			case OFFENSIVE: return getTechniques(target, RESERVED_ATTACK, SIT_ON_IT, TURN_AND_SIT);
			case BALANCED: return getTechniques(target, BLOCK, DUCK, HIT_THE_DECK, SIT_ON_IT, TURN_AND_SIT);
			case DEFENSIVE: return getTechniques(target, TAUNT, DUCK);
			case STONEWALL: return getTechniques(target, LOWER_GUARD);
			case DRAWN: return getTechniques(target, CANCEL);
			case SEDUCTION: return getTechniques(target, SLAP_ASS, GESTURE, PUCKER_LIPS, PRESENT, DUCK, HIT_THE_DECK);
			case PRONE: return getTechniques(target, REST_FACE_DOWN, ROLL_OVER_UP, PUSH_UP, KNEE_UP);
			case SUPINE: return getTechniques(target, REST, ROLL_OVER_DOWN, PUSH_UP, KNEE_UP);
			case HANDS_AND_KNEES: return getTechniques(target, STAY, SLAP_ASS_KNEES, KNEE_UP_HANDS);
			case KNEELING: return getTechniques(target, STAY_KNELT, GRAB_IT);
			case FULL_NELSON_BOTTOM: return getTechniques(target, SUBMIT);
			case DOGGY_BOTTOM: return getTechniques(target, RECEIVE_DOGGY, PUSH_BACK_DOGGY, SPREAD_DOGGY, SELF_SPANK);
			case PRONE_BONE_BOTTOM: return getTechniques(target, RECEIVE_PRONE_BONE);
			case ANAL_BOTTOM:  return getTechniques(target, RECEIVE_ANAL, STROKE, WRAP_LEGS);
			case HANDY_BOTTOM: return getTechniques(target, STROKE_IT, TANDEM_STROKE, FONDLE_BALLS, KISS_IT, KISS_BALLS, SPIT_ON_IT, OPEN_UP, OPEN_WIDE);
			case STANDING_BOTTOM: return getTechniques(target, RECEIVE_STANDING, STROKE_STANDING);
			case COWGIRL_BOTTOM: return getTechniques(target, RIDE_ON_IT, BOUNCE_ON_IT, SQUEEZE_IT);
			case REVERSE_COWGIRL_BOTTOM: return getTechniques(target, RIDE_ON_IT_REVERSE, BOUNCE_ON_IT_REVERSE, SQUEEZE_IT_REVERSE);
			case KNOTTED_BOTTOM: return getTechniques(target, RECEIVE_KNOT);
			case MOUTH_KNOTTED_BOTTOM: return getTechniques(target, SUCK_KNOT);
			case OVIPOSITION_BOTTOM: return getTechniques(target, RECEIVE_EGGS);
			case FELLATIO_BOTTOM: return getTechniques(target, SUCK_IT, SUCK_AND_STROKE, SUCK_AND_BEAT, BLOW, DEEPTHROAT, LICK_BALLS);
			case FACEFUCK_BOTTOM: return getTechniques(target, GET_FACEFUCKED);
			case OUROBOROS_BOTTOM: return getTechniques(target, RECEIVE_OUROBOROS);
			case FACE_SITTING_BOTTOM: return getTechniques(target, GET_FACE_RIDDEN);
			case SIXTY_NINE_BOTTOM:  return getTechniques(target, RECIPROCATE_FORCED);
			case GROUND_WRESTLE: return getTechniques(target, REST_WRESTLE);			
			case GROUND_WRESTLE_FACE_DOWN: return getTechniques(target, REST_GROUND_DOWN);
			case GROUND_WRESTLE_FACE_UP:  return getTechniques(target, REST_GROUND_UP);
			case WRAPPED_BOTTOM: return getTechniques(target, SQUEEZE_REST);
			case COWGIRL: return getTechniques(target, ERUPT_COWGIRL, BE_RIDDEN, KNOT);
			case REVERSE_COWGIRL: return getTechniques(target, ERUPT_COWGIRL, BE_RIDDEN_REVERSE, KNOT);
			case CRUSHING: return getTechniques(target, ERUPT_ANAL, CRUSH, PULL_UP);
			case FACE_SITTING: return getTechniques(target, SITTING_ORAL, RIDE_FACE);
			case SIXTY_NINE: return getTechniques(target, ERUPT_SIXTY_NINE, RECIPROCATE);
			case WRAPPED: return getTechniques(target, SQUEEZE_RELEASE, SQUEEZE_CRUSH, SQUEEZE, BITE);
			case ERUPT:
				setStance(Stance.BALANCED);
				possibles = getHypnotizedTechniqueOptions(target);
				setStance(Stance.ERUPT);
				return possibles;
			default: return possibles;
		}
	}
	
	protected Array<Techniques> getDefaultTechniqueOptions(AbstractCharacter target) {
		Array<Techniques> possibles = new Array<Techniques>();
		if (isParalyzed()) return getTechniques(target, DO_NOTHING);
		if (isHypnotized()) {
			possibles = getHypnotizedTechniqueOptions(target);
			if (possibles.size >= 1) return possibles;
		}
		switch(stance) {
			case BLITZ: return getTechniques(target, ALL_OUT_BLITZ, HOLD_BACK);
			case BERSERK: return getTechniques(target, RAGE);
			case COUNTER: return getTechniques(target, RIPOSTE, EN_GARDE);
			case OFFENSIVE: return getTechniques(target, BLITZ_ATTACK, BERSERK, REEL_BACK, POWER_ATTACK, GUT_CHECK, ARMOR_SUNDER, RECKLESS_ATTACK, KNOCK_DOWN, VAULT, FEINT_AND_STRIKE, TEMPO_ATTACK, RESERVED_ATTACK, HEADSHOT, WRISTSHOT, FOOTSHOT, FACE_SIT, SIT_ON_IT, TURN_AND_SIT, POUNCE_DOGGY, WRESTLE_TO_GROUND, WRESTLE_TO_GROUND_UP, MOUNT_FACE, SAY_AHH, FULL_NELSON);
			case BALANCED: return getTechniques(target, DRAW_ARROW, SPRING_ATTACK, NEUTRAL_ATTACK, CAUTIOUS_ATTACK, BLOCK, INCANTATION, SLIDE, DUCK, HIT_THE_DECK, USE_ITEM, KICK_OVER_FACE_UP, KICK_OVER_FACE_DOWN, SIT_ON_IT, TURN_AND_SIT, POUNCE_DOGGY, WRESTLE_TO_GROUND, WRESTLE_TO_GROUND_UP, MOUNT_FACE, SAY_AHH, FULL_NELSON);
			case DEFENSIVE: return getTechniques(target, REVERSAL_ATTACK, CAREFUL_ATTACK, GUARD, STONEWALL, CENTER, PARRY, TAUNT, SECOND_WIND,  SUDDEN_ADVANCE, INCANTATION, DUCK);
			case STONEWALL: return getTechniques(target, ABSOLUTE_GUARD, LOWER_GUARD);
			case FOCUS: return getTechniques(target, ASHI);
			case DRAWN: return getTechniques(target, FIRE, CANCEL);
			case SEDUCTION: return getTechniques(target, SLAP_ASS, GESTURE, PUCKER_LIPS, RUB, PRESENT, REVERSAL_ATTACK, BLOCK, DUCK, HIT_THE_DECK);
			case PRONE: return getTechniques(target, REST_FACE_DOWN, ROLL_OVER_UP, PUSH_UP, KNEE_UP, STAND_UP, KIP_UP);
			case SUPINE: return getTechniques(target, REST, ROLL_OVER_DOWN, PUSH_UP, KNEE_UP, STAND_UP, KIP_UP);
			case HANDS_AND_KNEES: return getTechniques(target, STAY, SLAP_ASS_KNEES, KNEE_UP_HANDS, STAND_UP_HANDS);
			case HAYMAKER: return getTechniques(target, HAYMAKER);
			case KNEELING: return getTechniques(target, UPPERCUT, STAY_KNELT, GRAB_IT, STAND_UP_KNEELING);
			case FULL_NELSON: return getTechniques(target, OVIPOSITION, HOLD, GRIP, TAKEDOWN, PENETRATE_STANDING, CORNHOLE);
			case AIRBORNE: return getTechniques(target, DIVEBOMB, JUMP_ATTACK, VAULT_OVER);
			case FULL_NELSON_BOTTOM: return getTechniques(target, SUBMIT, BREAK_FREE_FULL_NELSON, STRUGGLE_FULL_NELSON);
			case DOGGY_BOTTOM: return getTechniques(target, RECEIVE_DOGGY, PUSH_BACK_DOGGY, SPREAD_DOGGY, STROKE_DOGGY, BREAK_FREE_ANAL, STRUGGLE_DOGGY, SELF_SPANK);
			case PRONE_BONE_BOTTOM: return getTechniques(target, RECEIVE_PRONE_BONE, BREAK_FREE_ANAL, STRUGGLE_PRONE_BONE);
			case ANAL_BOTTOM:  return getTechniques(target, RECEIVE_ANAL, POUT, STROKE, BREAK_FREE_ANAL, STRUGGLE_ANAL, WRAP_LEGS);
			case HANDY_BOTTOM: return getTechniques(target, STROKE_IT, TANDEM_STROKE, FONDLE_BALLS, KISS_IT, KISS_BALLS, SPIT_ON_IT, OPEN_UP, OPEN_WIDE, LET_GO);
			case STANDING_BOTTOM: return getTechniques(target, RECEIVE_STANDING, STROKE_STANDING, BREAK_FREE_ANAL, STRUGGLE_STANDING);
			case COWGIRL_BOTTOM: return getTechniques(target, RIDE_ON_IT, BOUNCE_ON_IT, SQUEEZE_IT, STAND_OFF_IT);
			case REVERSE_COWGIRL_BOTTOM: return getTechniques(target, RIDE_ON_IT_REVERSE, BOUNCE_ON_IT_REVERSE, SQUEEZE_IT_REVERSE, STAND_OFF_IT);
			case KNOTTED_BOTTOM: return getTechniques(target, RECEIVE_KNOT);
			case MOUTH_KNOTTED_BOTTOM: return getTechniques(target, SUCK_KNOT);
			case OVIPOSITION_BOTTOM: return getTechniques(target, RECEIVE_EGGS);
			case FELLATIO_BOTTOM: return getTechniques(target, SUCK_IT, SUCK_AND_STROKE, SUCK_AND_BEAT, BLOW, DEEPTHROAT, LICK_BALLS, BREAK_FREE_ORAL, STRUGGLE_ORAL);
			case FACEFUCK_BOTTOM: return getTechniques(target, GET_FACEFUCKED, BREAK_FREE_ORAL, STRUGGLE_FACEFUCK);
			case OUROBOROS_BOTTOM: return getTechniques(target, RECEIVE_OUROBOROS, BREAK_FREE_ORAL, STRUGGLE_OUROBOROS);
			case FACE_SITTING_BOTTOM: return getTechniques(target, GET_FACE_RIDDEN, BREAK_FREE_FACE_SIT, STRUGGLE_FACE_SIT);
			case SIXTY_NINE_BOTTOM:  return getTechniques(target, RECIPROCATE_FORCED, BREAK_FREE_ORAL, STRUGGLE_SIXTY_NINE);
			case HELD: return getTechniques(target, UH_OH);
			case SPREAD: return getTechniques(target, RECEIVE_COCK);
			case PENETRATED: return getTechniques(target, HURK);
			case CASTING: return getTechniques(target, COMBAT_FIRE, COMBAT_HEAL, HEAL, TITAN_STRENGTH, WEAKENING_CURSE, GRAVITY, OIL, PARALYZE, HYPNOSIS, REFORGE, FOCUS_ENERGY, ACTIVATE);
			case ITEM: return getTechniques(target, ITEM_OR_CANCEL);
			case FELLATIO: return getTechniques(target, ERUPT_ORAL, IRRUMATIO, PULL_OUT_ORAL, BLOW_LOAD_ORAL, MOUTH_KNOT, FORCE_DEEPTHROAT);
			case FACEFUCK: return getTechniques(target, ERUPT_ORAL, FACEFUCK, PULL_OUT_ORAL);
			case OUROBOROS: return getTechniques(target, ERUPT_ORAL, ROUND_AND_ROUND, PULL_OUT_ORAL);				
			case GROUND_WRESTLE: return getTechniques(target, GRAPPLE, HOLD_WRESTLE, REST_WRESTLE, PIN, PENETRATE_MISSIONARY, FLIP_PRONE, RELEASE_SUPINE, PENETRATE_PRONE, FLIP_SUPINE, RELEASE_PRONE, CHOKE);			
			case GROUND_WRESTLE_FACE_DOWN: return getTechniques(target, REST_GROUND_DOWN, GRIND, BREAK_FREE_GROUND, STRUGGLE_GROUND);
			case GROUND_WRESTLE_FACE_UP:  return getTechniques(target, REST_GROUND_UP, BREAK_FREE_GROUND_UP, FULL_REVERSAL, REVERSAL, STRUGGLE_GROUND_UP);
			case WRAPPED_BOTTOM: return getTechniques(target, SQUEEZE_REST, BREAK_FREE_SQUEEZE, SQUEEZE_STRUGGLE);
			case PRONE_BONE: return getTechniques(target, ERUPT_ANAL, BLOW_LOAD, POUND_PRONE_BONE, PULL_OUT, KNOT);
			case DOGGY: return getTechniques(target, ERUPT_ANAL, BLOW_LOAD, POUND_DOGGY, SPANK, CRUSH_ASS, ASS_BLAST, PROSTATE_GRIND, PULL_OUT, KNOT);
			case ANAL: return getTechniques(target, ERUPT_ANAL, BLOW_LOAD, POUND_ANAL, PULL_OUT_ANAL);
			case STANDING: return getTechniques(target, ERUPT_ANAL, BLOW_LOAD, POUND_STANDING, PULL_OUT_STANDING);
			case COWGIRL: return getTechniques(target, ERUPT_COWGIRL, BE_RIDDEN, PUSH_OFF, PUSH_OFF_ATTEMPT, KNOT);
			case REVERSE_COWGIRL: return getTechniques(target, ERUPT_COWGIRL, BE_RIDDEN_REVERSE, PUSH_OFF_REVERSE, PUSH_OFF_ATTEMPT_REVERSE, KNOT);
			case HANDY:  return getTechniques(target, ERUPT_FACIAL, RECEIVE_HANDY);
			case KNOTTED: return getTechniques(target, KNOT_BANG);
			case MOUTH_KNOTTED: return getTechniques(target, MOUTH_KNOT_BANG);	
			case OVIPOSITION: return getTechniques(target, LAY_EGGS);
			case HOLDING: return getTechniques(target, OGRE_SMASH);
			case CRUSHING: return getTechniques(target, ERUPT_ANAL, CRUSH, PULL_UP);
			case FACE_SITTING: return getTechniques(target, SITTING_ORAL, RIDE_FACE);
			case SIXTY_NINE: return getTechniques(target, ERUPT_SIXTY_NINE, RECIPROCATE);
			case WRAPPED: return getTechniques(target, SQUEEZE_RELEASE, SQUEEZE_CRUSH, SQUEEZE, BITE);
			case DOGGY_KYLIRA: return getTechniques(target, POUND_DOGGY_KYLIRA, ERUPT_DOGGY_KYLIRA);
			case DOGGY_TRUDY: return getTechniques(target, POUND_DOGGY_TRUDY, ERUPT_DOGGY_TRUDY);
			case ERUPT:
				setStance(Stance.BALANCED);
				possibles = getDefaultTechniqueOptions(target);
				setStance(Stance.ERUPT);
				return possibles;
			default: return possibles;
		}
	}
	
	protected Array<Techniques> getTechniques(AbstractCharacter target, Techniques ... candidates) { 
		Array<Techniques> techniques = new Array<Techniques>(candidates);
		for (Techniques candidate : candidates) {
			if (inTechniques(candidate, SIT_ON_IT, TURN_AND_SIT) && !canSitOn(target)) { techniques.removeValue(candidate, true); }
			else if (candidate == POUNCE_DOGGY && !(target.stance == Stance.HANDS_AND_KNEES && isErect() && targetPouncable(target))) { techniques.removeValue(candidate, true); }
			else if (candidate == WRESTLE_TO_GROUND && !(target.stance == Stance.PRONE && isErect() && targetPouncable(target) && targetWrestlable(target))) { techniques.removeValue(candidate, true); }
			else if (candidate == WRESTLE_TO_GROUND_UP && !targetWrestlable(target)) { techniques.removeValue(candidate, true); }
			else if (inTechniques(candidate, WRESTLE_TO_GROUND_UP, MOUNT_FACE) && !(target.stance == Stance.SUPINE && isErect() && targetPouncable(target))) { techniques.removeValue(candidate, true); }
			else if (candidate == SAY_AHH && !(target.stance == Stance.KNEELING && isErect() && targetPouncable(target))) { techniques.removeValue(candidate, true); }
			else if (candidate == FULL_NELSON && !(target.stance.receivesMediumAttacks() && targetPouncable(target) && targetWrestlable(target))) { techniques.removeValue(candidate, true); }
			else if (candidate == USE_ITEM && !hasItemsToUse())  { techniques.removeValue(candidate, true); }
			else if (candidate == KICK_OVER_FACE_UP && target.getStance() != Stance.PRONE)  { techniques.removeValue(candidate, true); }
			else if (candidate == KICK_OVER_FACE_DOWN && target.getStance() != Stance.SUPINE)  { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, PUSH_UP, KNEE_UP_HANDS, BREAK_FREE_FULL_NELSON, STRUGGLE_FULL_NELSON, GRAPPLE, HOLD_WRESTLE, PIN, PENETRATE_MISSIONARY, FLIP_PRONE, RELEASE_SUPINE, PENETRATE_PRONE, FLIP_SUPINE, RELEASE_PRONE, GRIND, BREAK_FREE_GROUND, STRUGGLE_GROUND,
					BREAK_FREE_GROUND_UP, FULL_REVERSAL, REVERSAL, STRUGGLE_GROUND_UP, BREAK_FREE_SQUEEZE, SQUEEZE_STRUGGLE) && currentStamina <= 0) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, HOLD_WRESTLE, REST_WRESTLE, PIN, PENETRATE_MISSIONARY, FLIP_PRONE, RELEASE_SUPINE, PENETRATE_PRONE, FLIP_SUPINE, RELEASE_PRONE , GRIND, BREAK_FREE_GROUND, STRUGGLE_GROUND, BREAK_FREE_GROUND_UP, FULL_REVERSAL, REVERSAL, STRUGGLE_GROUND_UP) && grappleStatus == GrappleStatus.HELD) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, KNEE_UP, STAND_UP_HANDS, STAND_UP_KNEELING, CHOKE) && currentStamina <= 2) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, STAND_UP, GRIP) && currentStamina <= 4 || stability.compareTo(Stability.Dazed) < 0) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, KIP_UP, TAKEDOWN) && currentStamina <= 6) { techniques.removeValue(candidate, true); }
			else if (inTechniques(candidate, SLAP_ASS_KNEES, SELF_SPANK, WRAP_LEGS, PUSH_BACK_DOGGY, SPREAD_DOGGY) && !isLewd()) { techniques.removeValue(candidate, true); }		
			else if (candidate == GRAB_IT && !(target.isErect() && target.enemyType != EnemyEnum.SLIME && targetPouncable(target))) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, PENETRATE_STANDING, CORNHOLE) && (grappleStatus != GrappleStatus.HOLD || !isErect())) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, PENETRATE_STANDING) && (enemyType == EnemyEnum.BRIGAND)) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, CORNHOLE) && (enemyType != EnemyEnum.BRIGAND)) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, TAKEDOWN, PIN, FULL_REVERSAL, BREAK_FREE_FULL_NELSON, BREAK_FREE_ANAL, BREAK_FREE_ORAL, BREAK_FREE_FACE_SIT, BREAK_FREE_GROUND, BREAK_FREE_GROUND_UP, BREAK_FREE_SQUEEZE, PUSH_OFF, PUSH_OFF_REVERSE) && !hasGrappleAdvantage()) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, STRUGGLE_FULL_NELSON, STRUGGLE_DOGGY, STRUGGLE_PRONE_BONE, STRUGGLE_ANAL, STRUGGLE_STANDING, STRUGGLE_ORAL, STRUGGLE_FACEFUCK, STRUGGLE_OUROBOROS, STRUGGLE_FACE_SIT, STRUGGLE_SIXTY_NINE, STRUGGLE_GROUND, STRUGGLE_GROUND_UP, SQUEEZE_STRUGGLE, PUSH_OFF_ATTEMPT, PUSH_OFF_ATTEMPT_REVERSE) && hasGrappleAdvantage()) { techniques.removeValue(candidate, true); }	
			else if (candidate == REVERSAL && !grappleStatus.isDisadvantage()) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, RECEIVE_ANAL, POUT, STROKE, BREAK_FREE_ANAL, STRUGGLE_ANAL, WRAP_LEGS) && wrapLegs) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, SUCK_AND_STROKE, SUCK_AND_BEAT, BLOW, DEEPTHROAT, LICK_BALLS) && perks.get(Perk.MOUTH_MANIAC.toString(), 0) < 1) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, IRRUMATIO, FACEFUCK, ROUND_AND_ROUND, PULL_OUT_ORAL, POUND_ANAL, PULL_OUT_ANAL, POUND_DOGGY, POUND_DOGGY_KYLIRA, POUND_DOGGY_TRUDY, SPANK, CRUSH_ASS, ASS_BLAST, PROSTATE_GRIND, POUND_PRONE_BONE, POUND_STANDING, PULL_OUT_STANDING, PULL_OUT, BE_RIDDEN, BE_RIDDEN_REVERSE, PUSH_OFF, PUSH_OFF_REVERSE, PUSH_OFF_ATTEMPT, PUSH_OFF_ATTEMPT_REVERSE, RECEIVE_HANDY, CRUSH, PULL_UP, RECIPROCATE, FORCE_DEEPTHROAT) && arousal.isClimax()) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, ERUPT_ORAL, ERUPT_ANAL, ERUPT_DOGGY_TRUDY, ERUPT_DOGGY_KYLIRA, ERUPT_COWGIRL, ERUPT_FACIAL, ERUPT_SIXTY_NINE, BLOW_LOAD, BLOW_LOAD_ORAL) && !arousal.isClimax()) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, PENETRATE_MISSIONARY, FLIP_PRONE, RELEASE_SUPINE) && target.getStance() != Stance.GROUND_WRESTLE_FACE_UP ) { techniques.removeValue(candidate, true); }			
			else if (inTechniques(candidate, PENETRATE_PRONE, FLIP_SUPINE, RELEASE_PRONE) && target.getStance() != Stance.GROUND_WRESTLE_FACE_DOWN ) { techniques.removeValue(candidate, true); }			
			else if (inTechniques(candidate, FLIP_PRONE, RELEASE_SUPINE, FLIP_SUPINE, RELEASE_PRONE) && (grappleStatus == GrappleStatus.HOLD && isErect())) { techniques.removeValue(candidate, true); }			
			else if (inTechniques(candidate, PENETRATE_MISSIONARY, PENETRATE_PRONE) && (grappleStatus != GrappleStatus.HOLD || !isErect())) { techniques.removeValue(candidate, true); }
			else if (candidate == CRUSH && target.stance != Stance.SPREAD) { techniques.removeValue(candidate, true); }
			else if (candidate == PULL_UP && target.stance == Stance.SPREAD) { techniques.removeValue(candidate, true); }
			else if (candidate == SITTING_ORAL && !(isErect() && !target.isChastitied())) { techniques.removeValue(candidate, true); }		
			else if (candidate == RIDE_FACE && (isErect() && !target.isChastitied())) { techniques.removeValue(candidate, true); }	
			else if (inTechniques(candidate, SQUEEZE, BITE, SQUEEZE_CRUSH) && (currentStamina <= 0 || grappleStatus.isDisadvantage())) { techniques.removeValue(candidate, true); }
			else if (inTechniques(candidate, SQUEEZE, BITE) && grappleStatus == GrappleStatus.HOLD) { techniques.removeValue(candidate, true); }
			else if (candidate == SQUEEZE_RELEASE && (currentStamina > 0 && !grappleStatus.isDisadvantage())) { techniques.removeValue(candidate, true); }
			else if (candidate == SQUEEZE_CRUSH && grappleStatus != GrappleStatus.HOLD) { techniques.removeValue(candidate, true); }
			else if (candidate == MOUTH_KNOT && enemyType != EnemyEnum.WERESLUT) { techniques.removeValue(candidate, true); }
			else if (candidate == DRAW_ARROW && (getRangedWeapon() == null || range <= 1)) { techniques.removeValue(candidate, true); }
		}		
		return techniques; 
	}
	
	protected boolean inTechniques(Techniques candidate, Techniques ... techniques) {
		for (Techniques technique : techniques) {
			if (candidate == technique) return true;
		}
		return false;
	}
	
	protected String properCase(String sample) { return sample.substring(0, 1).toUpperCase() + sample.substring(1); }
	
	private Stability checkStability(int stabilityModifier) {
		Stability currentStability = stability;
		modStability(stabilityModifier);
		Stability resultingStability = stability;
		stability = currentStability;
		return resultingStability;
	}
	
	private int getStabilityChange(Technique technique) { 
		int possibleCost = getStabilityRegen() - technique.getStabilityCost();
		if (technique.getStabilityCost() > 0) possibleCost = Math.min(possibleCost, -1);
		else if (technique.getStabilityCost() < 0) possibleCost = Math.max(possibleCost, 1);
		else possibleCost = 0;
		return possibleCost;
	}

	private boolean canSitOn(AbstractCharacter target) { return isLewd() && target.getStance() == Stance.SUPINE && target.isErect() && targetRideable(target); }
	private boolean targetPouncable(AbstractCharacter target) { return (this.enemyType == null || this.enemyType.willPounce()) && (target.enemyType == null || target.enemyType.isPounceable()); }
	private boolean targetWrestlable(AbstractCharacter target) { return target.enemyType == null || target.enemyType.canWrestle(); }
	private boolean targetRideable(AbstractCharacter target) { return target.enemyType == null || target.enemyType.canBeRidden(); }
	private boolean hasItemsToUse() {
		if (inventory != null) {
			for (Item item : inventory) {
				if (item.isConsumable()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void modRange(int rangeMod) {
		range += rangeMod;
		if (range < 0) range = 0;
	}
	
	private int getMax(IntArray tiers) {
		int max = 0;
		for (int ii = 0; ii < tiers.size; ii++) {
			max += tiers.get(ii);
		}
		return max;
	}
	
	private Color getValueColor(int value) {
		switch (value) {
			case 0: return Color.WHITE;
			case 1: return Color.ORANGE;
			case 2: return Color.CORAL;
			default: return Color.RED;
		}
	}
	
	private void cleanEquipment() {
		if (weapon == null && rangedWeapon == null && shield == null && armor == null && legwear == null && underwear == null && headgear == null && armwear == null && footwear == null && firstAccessory == null && plug == null && cage == null && mouthwear == null) return;	
		Equipment weapon = this.weapon;
		Equipment rangedWeapon = this.rangedWeapon;
		Equipment shield = this.shield;
		Equipment armor = this.armor;
		Equipment legwear = this.legwear;
		Equipment underwear = this.underwear;
		Equipment headgear = this.headgear;
		Equipment armwear = this.armwear;
		Equipment footwear = this.footwear;
		Equipment firstAccessory = this.firstAccessory;
		Equipment plug = this.plug;
		Equipment cage = this.cage;
		Equipment mouthwear = this.mouthwear;
		this.weapon = this.rangedWeapon = null;
		this.shield = this.armor = this.legwear = this.underwear = this.headgear = this.armwear = this.footwear = null;
		this.firstAccessory = null;
		this.plug = null;
		this.cage = null;
		this.mouthwear = null;
		equip(weapon); equip(rangedWeapon); equip(shield); equip(armor); equip(legwear); equip(underwear); equip(headgear); equip(armwear); equip(footwear); equip(firstAccessory); equip(plug); equip(cage); equip(mouthwear);
	}
	private int getBloodLossDamage() { return Math.max(0, (statuses.get(StatusType.BLEEDING.toString(), 0) - getEndurance()) / 3); }
	private MutationResult repairArmor(int power) {
		String result = "";

		if (getArmor() != null) {
			getArmor().modDurability(power);
			result += getArmor().getName() + " durability improved by " + power + "!";
		}
		if (getLegwear() != null) {
			getLegwear().modDurability(power);
			result += getLegwear().getName() + " durability improved by " + power + "!";
		}
		
		return new MutationResult(result, power, MutationType.ARMOR);
	}
	
	public class UseItemEffect {
		private final String resultDisplay;
		private final Array<MutationResult> results;
		private UseItemEffect(String resultDisplay, Array<MutationResult> results) {
			this.resultDisplay = resultDisplay;
			this.results = results;
		}
		
		public String getResult() { return resultDisplay; }
		
	}
	
	public static class AttackResult {
		private final Array<MutationResult> toAttackerMessages;
		private final Array<MutationResult> toDefenderMessages;
		private final Array<String> dialog;
		private final Array<MutationResult> attackerResults;
		private final Array<MutationResult> defenderResults;
		
		protected AttackResult(Array<MutationResult> toAttackerMessages, Array<MutationResult> toDefenderMessages, Array<String> dialog, Array<MutationResult> attackerResults, Array<MutationResult> defenderResults) {
			this.toAttackerMessages = toAttackerMessages;
			this.toDefenderMessages = toDefenderMessages;
			this.dialog = dialog;
			this.attackerResults = attackerResults;
			this.defenderResults = defenderResults;
		}
		
		public Array<MutationResult> getToAttackerMessages() { return toAttackerMessages; }
		public Array<MutationResult> getToDefenderMessages() { return toDefenderMessages; }
		public Array<String> getDialog() { return dialog; }
		public Array<MutationResult> getAttackerResults() { return attackerResults; }
		public Array<MutationResult> getDefenderResults() { return defenderResults; }
	}
	
	public enum PhallusType {
		CUTE("MCCock"),
		TINY("MCCock"),
		SMALL("MCCock"),
		NORMAL("HumanCock"),
		MONSTER("Monster"), 
		DOG("DogLike"), 
		HORSE("HorseLike"), 
		BIRD("Harpy"), 
		GIANT("Monster"), 
		CAT("Monster"),
		NONE("NoCock");
		private final String skin;

		private PhallusType(String skin) { this.skin = skin; }
		
		public String getSkin() { return skin; }
		public String getLabel() { return this == CUTE ? "Cute" : this == TINY ? "Tiny" : this == SMALL ? "Small" : ""; }
		public String getDescription() { return this == CUTE ? "An adorable penis." : this == TINY ? "A very small penis." : this == SMALL ? "Average." : ""; }
	}
	
	public enum Stat {
		STRENGTH(AssetEnum.STRENGTH, "Strength determines raw attack power, which affects damage, how much attacks unbalance an enemies, and contests of strength, such as wrestling, struggling, or weapon locks. CAUTION: high strength attracts werewolves!", new String[]{"Crippled", "Feeble", "Weak", "Soft", "Able", "Strong", "Mighty", "Powerful", "Hulking", "Heroic", "Godlike", "Godlike", "Godlike"}),
		ENDURANCE(AssetEnum.ENDURANCE, "Endurance determines stamina and resilience, which affects your ability to keep up an assault without getting tired, your ability to shrug off low damage attacks, and wear heavier armor without becoming exhausted.", new String[]{"Feeble", "Infirm", "Fragile", "Frail", "Sturdy", "Durable", "Tough", "Stalwart", "Titanic", "Unstoppable", "Juggernaut", "Juggernaut", "Juggernaut"}),
		AGILITY(AssetEnum.AGILITY, "Agility determines balance and skill, affecting your ability to keep a sure footing even while doing acrobatic maneuvers, getting unblockable attacks against enemies, and evading enemy attacks.", new String[]{"Sluggish", "Clumsy", "Inept", "Slow", "Swift", "Quick", "Skillful", "Nimble", "Adept", "Preternatural", "Supernatural", "Supernatural", "Supernatural"}),
		PERCEPTION(AssetEnum.PERCEPTION, "Perception determines your ability to see what attacks an enemy may use next and prepare accordingly, as well as your base scouting ability, which determines what information you can see about upcoming areas.", new String[]{"Senseless", "Oblivious", "Dim-witted", "Slow-minded", "Alert", "Perceptive", "Observant", "Sharp", "Astute", "Eagle-eyed", "Omniscient", "Omniscient", "Omniscient"}),
		MAGIC(AssetEnum.MAGIC, "Magic determines your magical capabilities, such as how powerful magic spells are, and how many of them you can cast before becoming magically exhausted.", new String[]{"Unaware", "Mundane", "Aware", "Aligned", "Enchanted", "Mystical", "Otherwordly", "Arcane", "Mythical", "Omnipotent", "Demiurge", "Demiurge", "Demiurge"}),
		CHARISMA(AssetEnum.CHARISMA, "Charisma determines your ability to influence an enemy, getting them to calm down and listen to reason, enraging them, or seducing them.", new String[]{"Inhuman", "Horrible", "Uncouth", "Unpleasant", "Plain", "Likeable", "Charismatic", "Charming", "Magnetic", "Lovable", "Worshipable", "Worshipable", "Worshipable"});
		
		private final AssetEnum asset;
		private final String description;
		private final Array<String> ranks;
		private Stat(AssetEnum asset, String description, String[] ranks) {
			this.asset = asset;
			this.description = description;
			this.ranks = new Array<String>(ranks);
		}
		public AssetDescriptor<Texture> getAsset() { return asset.getTexture(); }
		public String getDescription() { return description; }
		public String getRankDescription(int rank) { return rank < ranks.size ? ranks.get(rank) : "Impossible"; }
		public String getLabel() {
			char[] chars = super.toString().replace("_", " ").toLowerCase().toCharArray();
			boolean found = false;
			for (int i = 0; i < chars.length; i++) {
				if (!found && Character.isLetter(chars[i])) {
					chars[i] = Character.toUpperCase(chars[i]);
					found = true;
			    } 
				else if (Character.isWhitespace(chars[i])) {
					found = false;
			    }
			}		
			return String.valueOf(chars);
		}
	}

	public enum Stability {
		Disoriented ("DOWN X 3", AssetEnum.BALANCE_ICON_2, 0),
		Dazed ("DOWN X 2", AssetEnum.BALANCE_ICON_2, 0),
		Down ("DOWN X 1", AssetEnum.BALANCE_ICON_2, 0),
		Tripping ("Tripping", AssetEnum.BALANCE_ICON_2, .1f),
		Teetering ("Teetering", AssetEnum.BALANCE_ICON_2, .2f),		
		Faltering ("Faltering", AssetEnum.BALANCE_ICON_1, .3f),		
		Weakfooted ("Weakfooted", AssetEnum.BALANCE_ICON_1, .4f),
		Unstable ("Unstable", AssetEnum.BALANCE_ICON_1, .5f),	
		Wobbly ("Wobbly", AssetEnum.BALANCE_ICON_1, .6f),
		Unsteady ("Unsteady", AssetEnum.BALANCE_ICON_0, .7f),	
		Stable ("Stable", AssetEnum.BALANCE_ICON_0, .8f),
		Surefooted ("Surefooted", AssetEnum.BALANCE_ICON_0, .9f),	
		Perfect ("Perfect", AssetEnum.BALANCE_ICON_0, 1);

		private final String label;
		private final AssetEnum texture;
		private final float percent;
		private Stability(String label, AssetEnum texture, float percent) {
			this.label = label;
			this.texture = texture;
			this.percent = percent;
		}
		
		public String getLabel() { return label; }
		public AssetDescriptor<Texture> getDisplay() { 	return texture.getTexture(); }
		public float getPercent() { return percent; }
		public boolean isDown() { return this.ordinal() < Tripping.ordinal(); }
		public boolean lowBalance() { return this.ordinal() < Weakfooted.ordinal(); }
		public boolean isGood() { return this.ordinal() > Unstable.ordinal(); }

		// this should just be a simple conversion of value to ordinal - change stability cost to be "tier cost" in skills, convert knockdown somehow, and then make it so that agility can downgrade the stability level costs - increase the number of stability levels as well
		public Stability shift(int stabilityMod) {
			int ordinal = this.ordinal() + stabilityMod;
			Stability [] values = Stability.values();
			return values.length < ordinal + 1 ? values[values.length - 1] : ordinal < 0 ? values[0] : values[ordinal];			
		}
		
		public static int getKnockdownConversion(int knockdownAmount) {
			if (knockdownAmount >= 20) return -10;
			if (knockdownAmount >= 10) return -3;
			if (knockdownAmount >= 5) return -2;
			if (knockdownAmount > 1) return -1;
			return 0;
		}
	}
	
	protected enum PronounSet {
		MALE ("he", "him", "his", "himself"),
		FEMALE ("she", "her", "her", "herself"),
		SECOND_PERSON ("you", "you", "your", "yourself")
		;
		private final String nominative, objective, possessive, reflexive;
		private PronounSet(String nominative, String objective, String possessive, String reflexive) {
			this.nominative = nominative;
			this.objective = objective;
			this.possessive = possessive;
			this.reflexive = reflexive;
		}
		public String getNominative() { return nominative; }
		public String getObjective() { return objective; }
		public String getPossessive() { return possessive; }
		public String getReflexive() { return reflexive; }
		
	}

	public ObjectMap<Perk, Integer> getPerks() {
		ObjectMap<Perk, Integer> tempPerks = new ObjectMap<Perk, Integer>();
		for (String key : perks.keys()) {
			tempPerks.put(Perk.valueOf(key), perks.get(key));
		}
		return tempPerks;
	}
}
