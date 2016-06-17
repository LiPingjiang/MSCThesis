package com.pli.RDFManager;

import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.sym.Name;

public class Entities {

	HashMap< String, Entity> entities = new HashMap< String, Entity>();// key = Entity Name, value = Entity Object
	HashMap< String, Entity> anonEntities = new HashMap< String, Entity>();
	HashMap< String, Entity> anotations = new HashMap< String, Entity>();
	Entities(){
		
	}
	
	//Type equals to Relation Type or Characteristics Type
	//Value equals to Relation Name or Characteristics Name
	public void addToEntity( String EntityType, String EntityName, String Type, String Value){
		//check whether exist
		//if so, just add relation and characteristic
		//if not, add as new one
		if (!entities.containsKey(EntityName)){
			entities.put(EntityName, new Entity(EntityType, EntityName));
		}
		entities.get(EntityName).Add(Type, Value);
		
	}
	public void addToEntity( String EntityType, String EntityName, String Type, String Value, boolean isAnonymous){
		if(isAnonymous)
			AddToAnon(EntityType,EntityName,Type,Value);
		else {
			addToEntity(EntityType,EntityName,Type,Value);
		}
		
	}
	
	public void addEntity( String EntityType, String EntityName){

		if (!entities.containsKey(EntityName)){
			entities.put(EntityName, new Entity(EntityType, EntityName));
		}
		
	}
	
	//Anonymous Class or instance
	public String AddToAnon( String EntityType, String Type, String Value){
		
		//TODO
		//check whether the anonymous entity exist
//		if (!anonEntities.containsKey(EntityName)){
//			anonEntities.put(EntityName, new Entity(EntityType, EntityName));
//		}
		String EntityName = "anon_"+anonEntities.size();
		anonEntities.put(EntityName, new Entity(EntityType, EntityName));

		anonEntities.get(EntityName).Add(Type, Value);
		
		return EntityName;
	}
	public boolean AddToAnon( String EntityType, String EntityName, String Type, String Value){
		
		//TODO
		//check whether the anonymous entity exist
		if (!anonEntities.containsKey(EntityName)){
			anonEntities.put(EntityName, new Entity(EntityType, EntityName));
		}
//		anonEntities.put(EntityName, new Entity(EntityType, EntityName));

		anonEntities.get(EntityName).Add(Type, Value);
		
		return true;
	}
	public String AddAnon( String EntityType){
		
		String EntityName = "anon_"+anonEntities.size();
		anonEntities.put(EntityName, new Entity(EntityType, EntityName));

		
		return EntityName;
	}
	public void addAnotations( String EntityType, String EntityName, String Type, String Value){
		//check whether exist
		//if so, just add relation and characteristic
		//if not, add as new one
		if (!anotations.containsKey(EntityName)){
			anotations.put(EntityName, new Entity(EntityType, EntityName));
		}
		anotations.get(EntityName).Add(Type, Value);
		
	}
	
	class Entity{
		private String EntityType;
		private String EntityName;
		HashMap<String, String> container = new HashMap<String, String>(); //Relation And Characteristics
		
		Entity( String Type, String Name){
			EntityType = Type;
			EntityName = Name;
		}
		
		//Add relation or characteristic to the Entity
		public boolean Add( String Type, String Value){
			if(container.containsKey(Type)){
				//check whether it is the same value
				if(container.get(Type).equals(Value))
					return true;
				else
					return false;
			}else {
				container.put( Type, Value);
			}
				
			return true;
		}
		
		public HashMap<String, String> get() {
			return container;
		}
		
		public String getName(){
			return EntityName;
		}
		public String getType(){
			return EntityType;
		}
	}

	public String toENSchema() {
		String enString="";
		
		//Anotation
		enString += "<!--Anotation-->\n";
		enString += printEntitiesHashMap(anotations);
		//ENTITY
		enString += "<!--Entities-->\n";
		enString += printEntitiesHashMap(entities);
		//Anonymous Entity
		enString += "<!--Anonymous Entities-->\n";
		enString += printEntitiesHashMap(anonEntities);
		
		return enString;
	}
	public String printEntitiesHashMap( HashMap< String, Entity> entities ) {
		String result="";
		
		for(Entry<String, Entity> entry : entities.entrySet()) {
		    String entityName = entry.getKey();
		    Entity entity = entry.getValue();
		    String body="";
		    body +="< " + entity.getType() + " " + entityName + "\n";
		    // do what you have to do here
		    // In your case, an other loop.
		    for(Entry<String, String> info : entity.get().entrySet()) {//info means relations or characteritics
		    	body +="\t" + info.getKey() + " " + info.getValue() + "\n";
		    }
		    body+=">" + "\n";
		    result += body;
		}
		result+= "\n";
		
		return result;
		
	}
	
}
