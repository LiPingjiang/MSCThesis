package com.mscthesis.pli.iotgateway.EN;

/**
 * Created by pli on 2016/7/14.
 */
public class ENPaser {
    Entities entities = null;
    EntityConstructor eConstructor = null;


    public class EntityConstructor{
        public String type="";
        public String name="";
        public String lastRelationType="";


    }

    public String toTurtle( String ontoInformation ){
        return entities.toTurtle(ontoInformation);
    }
    public void readENString( String enSchema){

        entities = new Entities();
        String[] lines = enSchema.split("\n");
        for (String line: lines){
            addToEntityConstructorLineByLine(line);
        }
    }
    private void addToEntityConstructorLineByLine(String line) {

//		System.out.println("text:" + line);
        if(line.equals(""))
            return;
        if(line.charAt(0)=='<'){

            String[] strings = line.substring(1,line.length()).split(" ");

            eConstructor = new EntityConstructor();
            eConstructor.type=strings[0].replace("\t", "").replace(" ", "");
            eConstructor.name=strings[1].replace("\t", "").replace(" ", "");
//			System.out.println("-----------     :" + eConstructor.type + "  " + eConstructor.name + " size:"+entities.getSize());

            entities.addEntity(eConstructor.type, eConstructor.name);
        }else if(line.charAt(0)=='>'){

            eConstructor = null;

        }else{


            if(line.length()>15 && line.substring(0,13).replace("\t", "").equals("rdfs:comment") ){

                eConstructor.lastRelationType = "rdfs:comment";
                entities.addToEntity(eConstructor.type, eConstructor.name, eConstructor.lastRelationType, line.substring(14,line.length()).replace("\t", ""));
//				System.out.println("strings: " + line.substring(13,line.length()));

            }
            else if (line.length()>15 && line.substring(0,11).replace("\t", "").equals("rdfs:label")){
//				System.out.println("strings: " + line.substring(11,line.length()));
                eConstructor.lastRelationType = "rdfs:label";
                entities.addToEntity(eConstructor.type, eConstructor.name, eConstructor.lastRelationType, line.substring(13,line.length()));
            }
            else{
//				System.out.println("text:" + line);
                String[] strings = line.substring(0,line.length()).split(" ");
//				System.out.println("********************************    string: " + line.replace("\t", "") +" strings.length: " + strings.length);
//				System.out.println("type: " + strings[0].replace("\t", ""));

                if(strings.length==1){
                    entities.addToEntity(eConstructor.type, eConstructor.name, eConstructor.lastRelationType, strings[0].replace("\t", "").replace(" ", ""));
                }else if(strings.length==2){
                    eConstructor.lastRelationType = strings[0].replace("\t", "").replace(" ", "");
                    entities.addToEntity(eConstructor.type, eConstructor.name, eConstructor.lastRelationType, strings[1].replace("\t", "").replace(" ", ""));
//					System.out.println("length 2 "+strings[0].replace("\t", ""));
                }
            }
        }
    }
}
