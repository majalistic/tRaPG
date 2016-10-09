package com.majalis.character;

import com.majalis.Technique.TechniquePrototype;
import com.majalis.Technique.TechniquePrototype.TechniqueHeight;
import com.majalis.character.AbstractCharacter.Stance;
/*
 * Represents an action taken by a character in battle.  Will likely need a builder helper.
 */
public class Technique {
	private final TechniquePrototype technique;
	private final int strength;
	private final int block;
	private final Stance forceStance;
	private final boolean battleOver;
	
	protected Technique(TechniquePrototype techniquePrototype, int strength){
		this(techniquePrototype, strength, 0);
	}
	protected Technique(TechniquePrototype techniquePrototype, int strength, int block){
		this.technique = techniquePrototype;
		this.strength = strength;
		this.block = block + technique.getGuardMod();
		forceStance = technique.getForceStance();
		battleOver = technique.causesBattleOver();
	}
	
	public String getTechniqueName(){
		return technique.getName();
	}
	
	public Attack resolve(Technique otherTechnique){
		int rand = (int) Math.floor(Math.random() * 100);
		double blockMod = otherTechnique.isBlockable() ? (getBlock() > rand * 2 ? 0 : getBlock() > rand ? .5 : 1) : 1;
		
		boolean isSuccessful = technique.getTechniqueHeight() != TechniqueHeight.HIGH || otherTechnique.getStance().receivesHighAttacks;
		// this is temporarily to prevent struggling from failing to work properly on the same term an eruption or knot happens
		if (isSuccessful) isSuccessful = otherTechnique.getForceStance() == null || otherTechnique.getForceStance() == Stance.KNOTTED || otherTechnique.getForceStance() == Stance.KNEELING;
		
		return new Attack(isSuccessful, technique.getName(), (int)(getDamage() * blockMod), technique.isHealing() ? strength + technique.getPowerMod() : 0, technique.isTaunt() ? strength + technique.getPowerMod() : 0, technique.isGrapple() ? strength + technique.getPowerMod() : 0, technique.isClimax(), getForceStance());
	}
	
	private boolean isBlockable() {
		return technique.isBlockable();
	}
	
	protected boolean isHeal(){
		return technique.isHealing();
	}
	
	protected int getDamage(){
		// can special case powerMod 100 = 0 here
		int damage = technique.doesSetDamage() ? 4 : technique.isDamaging() ? strength + technique.getPowerMod() : 0;
		if (damage < 0) damage = 0;
		return damage;
	}	
	
	protected int getBlock(){
		return block;
	}

	protected Stance getStance(){
		return technique.getResultingStance();
	}

	// right now this is a pass-through for technique.getStaminaCost() - could be modified by player (status effect that increases stamina cost, for instance)
	protected int getStaminaCost(){
		return technique.getStaminaCost();
	}
	
	protected int getStabilityCost(){
		return technique.getStabilityCost();
	}
	
	protected int getManaCost(){
		return technique.getManaCost();
	}
	
	protected Stance getForceStance(){
		return forceStance;
	}	
	
	protected boolean forceBattleOver(){
		return battleOver;
	}
}
