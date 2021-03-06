package com.majalis.save;

import java.text.DateFormat;
import java.util.Calendar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SerializationException;
import com.majalis.asset.AssetEnum;
import com.majalis.battle.BattleAttributes;
import com.majalis.character.EnemyCharacter;
import com.majalis.character.Item;
import com.majalis.character.Perk;
import com.majalis.character.PlayerCharacter;
import com.majalis.character.PlayerCharacter.QuestFlag;
import com.majalis.character.PlayerCharacter.QuestType;
import com.majalis.character.SexualExperience;
import com.majalis.character.AbstractCharacter.Stat;
import com.majalis.character.Techniques;
import com.majalis.encounter.EncounterCode;
import com.majalis.scenes.ShopScene.Shop;
import com.majalis.screens.TownScreen.TownCode;
/*
 * Used for file handling, both reading and writing - both game files and encounter replay files.
 */
public class SaveManager implements SaveService, LoadService {
	private boolean encoded;
    private final FileHandle file; 
    private final FileHandle profileFile;
    private GameSave save;
    private ProfileSave profileSave;
   
    public SaveManager(SaveManager original, String path) {
    	this.encoded = original.encoded;
    	this.profileFile = original.profileFile;
    	this.file = Gdx.files.local(path);
    	save = original.save;
    	profileSave = original.profileSave;
    	saveToJson(save);
    }
    
    public SaveManager(boolean encoded, String path, String profilePath) {
        this.encoded = encoded;
        file = Gdx.files.local(path);   
        profileFile = Gdx.files.local(profilePath);
        save = getSave();
        profileSave = getProfileSave();
    }
    
    public void manualSave(String path) {
    	new SaveManager(this, path);
    }
    
    public void saveDataValue(ProfileEnum key, Object object) { saveDataValue(key, object, true); }
    public void saveDataValue(ProfileEnum key, Object object, boolean saveToJson) {
    	switch (key) {
			case KNOWLEDGE:		profileSave.addKnowledge((String) object); break;
			case ACHIEVEMENT:   profileSave.addAchievement((Achievement) object); break;
		}
    	if (saveToJson) {
    		saveToProfileJson(profileSave);
    	}
    }
    
    @SuppressWarnings("unchecked")
    public <T> T loadDataValue(ProfileEnum key, Class<?> type) {
    	switch (key) {
			case KNOWLEDGE: 		return (T) (ObjectMap<String, Integer>) profileSave.enemyKnowledge;
			case ACHIEVEMENT: 		return (T) (ObjectMap<String, Integer>) profileSave.achievements;
		}
    	return null;
    }
    
	public Array<MutationResult> saveDataValue(SaveEnum key, Object object) { return saveDataValue(key, object, true); }
	@SuppressWarnings("unchecked")
	public Array<MutationResult> saveDataValue(SaveEnum key, Object object, boolean saveToJson) {
		Array<MutationResult> result = new Array<MutationResult>();
    	switch (key) {
	    	case PLAYER: 			save.player = (PlayerCharacter) object; break;
	    	case ENEMY: 			save.enemy = (EnemyCharacter) object; break;
	    	case SCENE_CODE: 		int sceneCode = (Integer) object; if (!save.sceneCode.contains(sceneCode)) save.sceneCode.add(sceneCode); break; // this could be replaced with an IntSet instead of an IntArray the way nodes are
	    	case CONTEXT: 			if (object == null) save.context = save.returnContext; else save.context = (GameContext) object; break;
	    	case RETURN_CONTEXT: 	save.returnContext = (GameContext) object; break;
	    	case NODE_CODE: 		save.nodeCode = (Integer) object; break;
	    	case MAP_CODE: 			save.mapCode = (Integer) object; break; 
	    	case ENCOUNTER_CODE:	save.encounterCode = (EncounterCode) object; break;
	    	case VISITED_LIST:		VisitInfo visitedNode = (VisitInfo) object; save.visitedNodeList.put(visitedNode.nodeCode, visitedNode); break;
	    	case BATTLE_CODE:		save.battleAttributes = (BattleAttributes) object; break;
	    	case CLASS:				save.player.setJobClass((JobClass) object); save.player.load(); break;
	    	case WORLD_SEED:		save.worldSeed = (Integer) object; break;
	    	case HEALTH: 			result.addAll(save.player.modHealth((Integer) object)); result.addAll(save.player.cureBleed((Integer) object / 5)); break; 
	    	case WILLPOWER: 		result.addAll(save.player.modWillpower((Integer) object)); break;
	    	case SKILL: 			save.player.addSkill((Techniques) object, 1); result.add(new MutationResult("Gained " + ((Techniques) object).getTrait().getName() + " technique!")); break; // this should get a result back from addSkill
	    	case PERK:				save.player.addPerk((Perk) object, 1); result.add(new MutationResult("Gained" + ((Perk) object).getLabel() + " perk!")); break; // this should get a result back from addPerk
	    	case SKILL_POINT:		result.addAll(save.player.modSkillPoints((Integer) object)); break;
	    	case PERK_POINT:		result.addAll(save.player.modPerkPoints((Integer) object)); break;
	    	case MAGIC_CRYSTAL:		result.addAll(save.player.modMagicPoints((Integer) object)); break;
	    	case FOOD:				result.addAll(save.player.modFood((Integer) object)); break; 
	    	case TIME:				result.addAll(save.player.timePass((Integer) object)); break;
	    	case SCOUT:				int val = (Integer)object; if (val == 0) save.player.resetScout(); result.addAll(save.player.increaseScout(val)); break;
	    	case STEALTH:			int val2 = (Integer)object; if (val2 == 0) save.player.resetStealth(); result.addAll(save.player.increaseStealth(val2)); break;
	    	case EXPERIENCE:		result.addAll(save.player.modExperience((Integer) object)); break; 
	    	case GOLD:				result.addAll(save.player.modMoney((Integer) object)); break; 
	    	case DEBT:				result.addAll(save.player.modDebt((Integer) object)); break;
	    	case MODE:				save.mode = (GameMode) object; if ((GameMode) object == GameMode.SKIRMISH) save.player.load(); break;
	    	case MUSIC:				save.newMusic = (AssetEnum) object; break;
	    	case CONSOLE:			save.battleConsole = (Array<Array<MutationResult>>) object; break;
	    	case ANAL:				result.addAll(save.player.receiveSex((SexualExperience) object)); break;
	    	case ITEM:				result.addAll(save.player.receiveItem((Item) object)); break;
	    	case SHOP:				save.shops.put(((Shop) object).getShopCode(), (Shop) object); break;
	    	case TOWN: 				save.town = (TownCode) object; break;
	    	case TRUDY:				save.trudy = (Integer) object; break;
	    	case KYLIRA:			save.kylira = (Integer) object; break;
	    	case QUEST: 			QuestFlag flag = (QuestFlag) object; save.player.setQuestStatus(flag.type, flag.value); if(save.player.getQuestStatus(QuestType.TRUDY) > 4) save.trudy = -1; if (save.player.getQuestStatus(QuestType.ELF) > 3) save.kylira = -1; break;
	    	case RESULT: 			save.results.addAll((Array<MutationResult>) object); break;
	    	case BATTLE_RESULT: 	save.battleResults.addAll((Array<MutationResult>) object); break;
	    	case PORTRAIT:			if (object == null) save.player.popPortraitPath(); else save.player.setCurrentPortrait((AssetEnum)object); break;
	    	case ENCOUNTER_END:		save.player.refresh(); save.sceneCode.clear(); save.results.clear(); save.battleResults.clear(); break;
	    	case GAME_OVER: 		save.player.setGameOver((GameOver) object); break;
	    	case TIME_STAMP: 		break;
    	}	
    	if (saveToJson) { save.timestamp = getTimeStamp(); saveToJson(save); } //Saves current save immediately
        return result;
	}
	
	public void flush() { saveToJson(save); }
	
    @SuppressWarnings("unchecked")
    public <T> T loadDataValue(SaveEnum key, Class<?> type) {
    	switch (key) {
	    	case PLAYER: 			return (T) (PlayerCharacter)save.player;
	    	case ENEMY: 			return (T) (EnemyCharacter)save.enemy;
	    	case SCENE_CODE: 		return (T) (IntArray)save.sceneCode;
	    	case CONTEXT: 			return (T) save.context;
	    	case RETURN_CONTEXT: 	return (T) save.returnContext;
	    	case NODE_CODE: 		return (T) (Integer)save.nodeCode;
	    	case MAP_CODE:			return (T) (Integer)save.mapCode;
	    	case ENCOUNTER_CODE:	return (T) save.encounterCode;
	    	case VISITED_LIST:		return (T) save.visitedNodeList;
	    	case BATTLE_CODE:		return (T) save.battleAttributes;
	    	case CLASS:				return (T) save.player.getJobClass();
	    	case WORLD_SEED:		return (T) (Integer) save.worldSeed;
	    	case HEALTH:			return (T) (Integer) save.player.getCurrentHealth();
	    	case SKILL:				return (T) (ObjectMap<Techniques, Integer>) save.player.getSkills();	
	    	case SKILL_POINT:		return (T) (Integer) save.player.getSkillPoints();
	    	case PERK_POINT:		return (T) (Integer) save.player.getPerkPoints();
	    	case MAGIC_CRYSTAL:		return (T) (Integer) save.player.getMagicPoints();
	    	case PERK:				return (T) (ObjectMap<Perk, Integer>) save.player.getPerks();	
	    	case FOOD: 				return (T) (Integer) save.player.getFood();
	    	case EXPERIENCE:		return (T) (Integer) save.player.getExperience();
	    	case MODE:				return (T) (GameMode) save.mode;
	    	case MUSIC:				return (T) (AssetEnum) save.newMusic;
	    	case CONSOLE:			return (T) (Array<Array<MutationResult>>) save.battleConsole;
	    	case TOWN: 				return (T) (TownCode) save.town;
	    	case TRUDY:				return (T) (Integer) save.trudy;
	    	case KYLIRA:			return (T) (Integer) save.kylira;
	    	case TIME_STAMP: 		return (T) (String) save.timestamp;
	    	case ANAL:			
	    	case ITEM:
	    	case GOLD:
	    	case WILLPOWER:
	    	case DEBT:
	    	case ENCOUNTER_END:
	    	case SCOUT:
	    	case STEALTH:
	    	case PORTRAIT:
	    	case GAME_OVER:
	    	case QUEST:				
	    							break;
	    	case SHOP:				return (T) (ObjectMap<String, Shop>) save.shops;
	    	case TIME :				return (T) (Integer) save.player.getTime();
	    	case RESULT:			return (T) save.results;
	    	case BATTLE_RESULT: 	return (T) save.battleResults;
    	}	
    	return null;
    }
    
    private void saveToJson(GameSave save) {
        Json json = new Json();
        json.setOutputType(OutputType.json);
        if(encoded) file.writeString(Base64Coder.encodeString(json.prettyPrint(save)), false);
        else {   	
        	Group playerParent = save.player.getParent();
       
        	if (playerParent != null) playerParent.removeActor(save.player, false);
        	Group enemyParent = new Group();
        	Array<Actor> enemyChildren = new Array<Actor>();
        	Array<Action> enemyActions = new Array<Action>();
        	float enemyX = 0;
        	float enemyY = 0;
        	float enemyScale = 1;
        	if (save.enemy != null) {
            	enemyParent = save.enemy.getParent();
            	if (enemyParent != null) {
            		enemyParent.removeActor(save.enemy, false);
            	}	
            	for (Actor actor : save.enemy.getChildren()) { enemyChildren.add(actor); }
            	save.enemy.clearChildren();
            	for (Action action : save.enemy.getActions()) { enemyActions.add(action); }
            	save.enemy.getActions().clear();
            	enemyX = save.enemy.getX();
            	enemyY = save.enemy.getY();
            	enemyScale = save.enemy.getScaleX();
            	save.enemy.setPosition(0, 0);
            	save.enemy.setScale(1);
        	}
        	try {
        		file.writeString(json.prettyPrint(save), false);
        	}
        	catch (GdxRuntimeException ex) {
        		ex.printStackTrace();
        	}
        	
        	if (playerParent != null) playerParent.addActor(save.player);
        	if (save.enemy != null) {
        		if (enemyParent != null) {
        			enemyParent.addActor(save.enemy);
        		}	
        		for (Actor actor : enemyChildren) { save.enemy.addActor(actor); }
        		for (Action action : enemyActions) { save.enemy.addAction(action); }
        		save.enemy.setPosition(enemyX, enemyY);
            	save.enemy.setScale(enemyScale);
        	}
        }
    }
    
    private void saveToProfileJson(ProfileSave save) {
        Json json = new Json();
        json.setOutputType(OutputType.json);
        if(encoded) profileFile.writeString(Base64Coder.encodeString(json.prettyPrint(save)), false);
        else {   	
        	try {
        		profileFile.writeString(json.prettyPrint(save), false);
        	}
        	catch (GdxRuntimeException ex) {
        		ex.printStackTrace();
        	}
        }
    }
    
    private GameSave getSave() {
        GameSave save;
        if(file.exists()) {
	        Json json = new Json();
	        try {
		        if(encoded)save = json.fromJson(GameSave.class, Base64Coder.decodeString(file.readString()));
		        else {
		        	save = json.fromJson(GameSave.class,file.readString());
		        }
	        }
	        catch(SerializationException ex) {
	        	System.err.println(ex.getMessage());
	        	// this should load the Troja save file
	        	save = getDefaultSave();
	        }
        }
        else {
        	save = getDefaultSave();
        }
        save = save == null ? new GameSave(true) : save;
        save.addBonusPoints(profileSave);
        return save;
    }
    
    private ProfileSave getProfileSave() {
    	ProfileSave save;
        if(profileFile.exists()) {
	        Json json = new Json();
	        if(encoded)save = json.fromJson(ProfileSave.class, Base64Coder.decodeString(profileFile.readString()));
	        else {
	        	save = json.fromJson(ProfileSave.class,profileFile.readString());
	        }
        }
        else {
        	save = getDefaultProfileSave();
        }
        return save==null ? new ProfileSave(true) : save;
    }
    
    public void newSave() {
    	save = getDefaultSave();
    }
    
    // this will load/create a new save with the designated path, then flush 
    public void newSave(String path) {
    	save = new SaveManager(encoded, path, profileFile.path()).save;
    	saveToJson(save);
    }
    
    private GameSave getDefaultSave() {
    	GameSave tempSave = new GameSave(true);
    	tempSave.addBonusPoints(profileSave);
    	saveToJson(tempSave);
    	return tempSave;
    }
    
    private ProfileSave getDefaultProfileSave() {
    	ProfileSave tempSave = new ProfileSave(true);
    	saveToProfileJson(tempSave);
    	return tempSave;
    }
    
    private static String getTimeStamp() {
    	return DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    }
    
    public static class VisitInfo { 
    	public int numberOfEncounters;
    	public int lastEncounterTime;
    	public int randomVal;
    	public int visibility;
    	public int nodeCode;
    	public int mapCode;
		public boolean isHidden;
    	
    	@SuppressWarnings("unused")
		private VisitInfo() {}
    	
    	public VisitInfo(int numberOfEncounters, int lastEncounterTime, int randomVal, int visibility, int nodeCode, int mapCode) { this(numberOfEncounters, lastEncounterTime, randomVal, visibility, nodeCode, mapCode, false); }   	
    	public VisitInfo(int numberOfEncounters, int lastEncounterTime, int randomVal, int visibility, int nodeCode, int mapCode, boolean isHidden) {
    		this.numberOfEncounters = numberOfEncounters;
    		this.lastEncounterTime = lastEncounterTime;
    		this.randomVal = randomVal;
    		this.visibility = visibility;
    		this.nodeCode = nodeCode;
    		this.isHidden = isHidden;
    	}
    	
    	public void setVisibility(int newVisibility) { if (visibility < newVisibility) visibility = newVisibility; } 
    }
    
    public static class GameSave {
		public String timestamp;
		private GameContext context;
		private GameContext returnContext;
    	private GameMode mode;	
    	private AssetEnum newMusic;
    	private int worldSeed;
    	private IntArray sceneCode;
    	private EncounterCode encounterCode;
    	private int nodeCode;
    	private int mapCode;
    	private Array<Array<MutationResult>> battleConsole;
    	private IntMap<VisitInfo> visitedNodeList;
    	private TownCode town;
    	private int trudy;
    	private int kylira;
    	// this can probably be refactored to contain a particular battle, but may need to duplicate the player character
    	private BattleAttributes battleAttributes;
    	private PlayerCharacter player;
    	private EnemyCharacter enemy;
    	private ObjectMap<String, Shop> shops;
		private Array<MutationResult> results;
		private Array<MutationResult> battleResults;
		
		// legacy attributes
    	@SuppressWarnings("unused") private String music;
    	@SuppressWarnings("unused") private IntArray visitedList; 
    	@SuppressWarnings("unused") private Array<String> console; 
    	// 0-arg constructor for JSON serialization: DO NOT USE
		@SuppressWarnings("unused")
		private GameSave() { 
			visitedNodeList = new IntMap<VisitInfo>();
			visitedNodeList.put(1,  new VisitInfo(1, 0, 0, 0, 1, 0)); 
			town = TownCode.TOWN_STORY;
			battleResults = new Array<MutationResult>();
			timestamp = "";
		}
    	
		// default save values-
    	public GameSave(boolean defaultValues) {
    		if (defaultValues) {
    			context = GameContext.ENCOUNTER;
    			worldSeed = (int) (Math.random()*10000);
    			// -1 sceneCode is the magic number to designate that a scene doesn't need to be loaded; just use the first (last) scene in the list
    			sceneCode = new IntArray();
    			encounterCode = EncounterCode.INITIAL;
    			returnContext = GameContext.WORLD_MAP;
        		nodeCode = 1;
        		mapCode = 0;
        		console = new Array<String>();
        		battleConsole = new Array<Array<MutationResult>>();
        		shops = new ObjectMap<String, Shop>();
        		visitedNodeList = new IntMap<VisitInfo>();
        		visitedNodeList.put(1,  new VisitInfo(1, 0, 0, 0, 1, 0));
        		kylira = 5;
        		trudy = 14; // these should be set with an actual automatic value (or based on the initial gen)
        		player = new PlayerCharacter(true);
        		results = new Array<MutationResult>();
        		battleResults = new Array<MutationResult>();
        		timestamp = getTimeStamp();
    		}
    	}
    	
    	private void addBonusPoints(ProfileSave profileSave) { 
    		if (profileSave != null && profileSave.achievements != null) {
    			int bonusPoints = 0;
    			for (ObjectMap.Entry<String, Integer> achievement : profileSave.achievements) {
    				if (achievement.value >= 1) bonusPoints++;
    			}    			
    			player.setBonusPoints(bonusPoints);  
    		}
    	}
    }
    
    public static class ProfileSave {
    	private ObjectMap<String, Integer> achievements;
		private ObjectMap<String, Integer> enemyKnowledge;
    	
    	// 0-arg constructor for JSON serialization: DO NOT USE
		private ProfileSave() {
			enemyKnowledge = new ObjectMap<String, Integer>();
    		achievements = new ObjectMap<String, Integer>();
		}
		private ProfileSave(boolean defaultValues) {
    		enemyKnowledge = new ObjectMap<String, Integer>();
    		achievements = new ObjectMap<String, Integer>();
    	}
		public void addAchievement(Achievement achievement) { addAchievement(achievement.toString()); }
		protected void addAchievement(String achievement) { addAchievement(achievement, 1); }
    	protected void addAchievement(String achievement, int amount) { achievements.put(achievement, amount); }
    	
    	protected void addKnowledge(String knowledge) { addKnowledge(knowledge, 1); }
    	protected void addKnowledge(String knowledge, int amount) { enemyKnowledge.put(knowledge, amount); }
    }
    
	public enum JobClass {
		WARRIOR ("Warrior") {
			@Override
			public int getBaseStat(Stat stat) {
				switch (stat) {
					case STRENGTH:    return 7;
					case ENDURANCE:   return 5;
					case AGILITY:     return 4;
					case PERCEPTION:  return 3;
					case MAGIC:       return 1;
					case CHARISMA:    return 2;
					default: return -1;
				}
			}}, 
		PALADIN ("Paladin") {
			@Override
			public int getBaseStat(Stat stat) {
				switch (stat) {
					case STRENGTH:    return 4;
					case ENDURANCE:   return 7;
					case AGILITY:     return 2;
					case PERCEPTION:  return 1;
					case MAGIC:       return 3;
					case CHARISMA:    return 5;
					default: return -1;
				}
			}}, 
		THIEF ("Thief") {
			@Override
			public int getBaseStat(Stat stat) {
				switch (stat) {
					case STRENGTH:    return 3;
					case ENDURANCE:   return 3;
					case AGILITY:     return 7;
					case PERCEPTION:  return 4;
					case MAGIC:       return 1;
					case CHARISMA:    return 4;
					default: return -1;
				}
			}}, 
		RANGER ("Ranger") {
			@Override
			public int getBaseStat(Stat stat) {
				switch (stat) {
					case STRENGTH:    return 3;
					case ENDURANCE:   return 2;
					case AGILITY:     return 5;
					case PERCEPTION:  return 7;
					case MAGIC:       return 1;
					case CHARISMA:    return 4;
					default: return -1;
				}
			}}, 
		MAGE ("Mage") {
			@Override
			public int getBaseStat(Stat stat) {
				switch (stat) {
					case STRENGTH:    return 2;
					case ENDURANCE:   return 2;
					case AGILITY:     return 2;
					case PERCEPTION:  return 5;
					case MAGIC:       return 7;
					case CHARISMA:    return 4;
					default: return -1;
				}
			}}, 
		ENCHANTRESS ("Enchanter") {
			@Override
			public int getBaseStat(Stat stat) {
				switch (stat) {
					case STRENGTH:    return 3;
					case ENDURANCE:   return 3;
					case AGILITY:     return 3;
					case PERCEPTION:  return 3;
					case MAGIC:       return 3;
					case CHARISMA:    return 7;
					default: return -1;
				}
			}};
		private final String label;

		JobClass(String label) { 
		    this.label = label;
		}
		public String getLabel() { return label; }
		public abstract int getBaseStat(Stat stat);
		public AssetDescriptor<Texture> getTexture() {
			switch(this) {
				case ENCHANTRESS: return AssetEnum.ENCHANTRESS.getTexture();
				case MAGE: return AssetEnum.MAGE.getTexture();
				case PALADIN: return AssetEnum.PALADIN.getTexture();
				case RANGER: return AssetEnum.RANGER.getTexture();
				case THIEF: return AssetEnum.THIEF.getTexture();
				case WARRIOR: return AssetEnum.WARRIOR.getTexture();
				default: return  AssetEnum.NULL.getTexture();
			}
		}
		public String getDescription() {
			switch(this) {
				case ENCHANTRESS: return "An enchantress knows the ways of magic and the blade, but their charms are unrivaled.";
				case MAGE: return "A mage is cursed with the burden and boon of sorcery; feared and reviled, their strange powers are unparalleled by any.";
				case PALADIN: return "A paladin is a sacred knight who takes a vow of chastity, and has an indomitable body and spirit.";
				case RANGER: return "A ranger is a hunter and outdoorsman, with a keen sense of their situation and their surroundings, and a sharp eye for detail.";
				case THIEF: return "A thief is a skilled individual, tending to avoid direct confrontation, even in a fight.  They specialize in trickery and feats of agility.";
				case WARRIOR: return "A warrior is a combat specialist with a focus on gracefully overpowering their opponents.";
				default: return "???";
			}
		}
	}
	
	public enum GameContext {
		ENCOUNTER,
		WORLD_MAP,
		BATTLE, 
		LEVEL,
		TOWN,
		CAMP,
		GAME_OVER
	}
	
	public enum GameOver {
		DEFAULT,
		MOUTH_FIEND
	}
	
	public enum GameMode {
		STORY,
		SKIRMISH
	}
	
}