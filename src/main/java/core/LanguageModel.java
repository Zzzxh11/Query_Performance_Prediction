/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Parastoo
 */
public class LanguageModel {
    
    public static HashMap<String,Double> GGModel;
    public HashMap<String, Double> Model ;
    public LanguageModel(){
        GGModel = new HashMap<>();
    }
    
    
    public HashMap<String, Double> createLanguageModel(String content) {
        Model = new HashMap<>();
        String[] entities = content.split(",");
        for (String entity : entities) {
            if(entity !=null && !entity.isEmpty()){
                if(!Model.containsKey(entity))
                    Model.put(entity, (double)1);

                else
                    Model.put(entity, Model.get(entity)+1);
                
                
            }
        }
        int entityNumber = entities.length;
        
        for (String key : Model.keySet()) {
            Model.put(key, Model.get(key) / (double)entityNumber);
        }
        
        return Model;
    } 
    public void createGGLanguageModel(String content) {
        String[] entities = content.split(",");
        for (String entity : entities) {
            if(entity !=null && !entity.isEmpty()){
                if(!GGModel.containsKey(entity))
                    GGModel.put(entity, (double)1);

                else
                    GGModel.put(entity, GGModel.get(entity)+1);
                
                
            }
        }
        
    } 
    
    public static void createGGLanguageModel(HashMap<String,String> documents){
        int entityNumber =0;
        for(String key: documents.keySet()){
            String[] entities = documents.get(key).split(",");
            entityNumber += entities.length;
            for (String entity : entities) {
                if(entity !=null && !entity.isEmpty()){
                    if(!GGModel.containsKey(entity))
                        GGModel.put(entity, (double)1);

                    else
                        GGModel.put(entity, GGModel.get(entity)+1);


                }
            }
        }
        for (String key : GGModel.keySet()) {
            GGModel.put(key, GGModel.get(key) / (double)entityNumber);
        }
    }
    
}
