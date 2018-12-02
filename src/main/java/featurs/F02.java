/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featurs;

//import com.github.andrewoma.dexx.collection.Pair;
import be.vanoosten.esa.tools.ESAtester_EN;
import core.SqlConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.mapping.Collection;

/**
 *
 * @author Parastoo
 */
public class F02 {
    private final String dataset ;
    private final String rs;
    private final int docCount;
    private final int start;
    private final int end;
    private ArrayList<String> queryEntities;
    private ArrayList<String> documentEntities;
    
    public HashMap<String,Integer> entities ;
    
    public F02(String ds ,String rs ,int docCount, int start,int end){
        this.dataset = ds;
        this.rs = rs;
        this.docCount = docCount;
        this.start = start;
        this.end = end;
        
    }
    
    public void run(){
        for (int i = start; i <= end; i++) {
//            if(i ==227){
                queryEntities = new ArrayList<>();
                documentEntities = new ArrayList<>();
                entities = new HashMap<>();
                double Score = CalculateSimilarityScore(Integer.toString(i));
                System.out.println("query "+i+" :"+ Score);
//            }
        }
    }

    
    
    private double CalculateSimilarityScore(String queryId) {
        double score = 0;
        String query = "SELECT q.entity,d.entity,rs.document_id,rs.rank from "+rs+"_"+dataset+" rs left join document_"+dataset+
                        " d on rs.document_id = d.title " +
                        "left join query_"+dataset+" q on rs.query_id = q.id where q.id = "+queryId +
                " and q.entity not like \'\'  order by rs.rank limit 20 "; 
        //and d.entity not like \'\'
        
        int counter = 0;
        ResultSet resultset = SqlConnection.select(query);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./result/"+rs+"_"+dataset+"_docs_donot_have_entities.txt")));
            String qEntities ="";
            String docEntities ="";
            while(resultset.next()){
                
                qEntities = resultset.getString(1);
                if(resultset.getString(2)== null){
                    bw.write("doc doesn't have entity : "+resultset.getString(3)+"\n");
                    bw.flush();
                }
                else
                {
                    docEntities  = docEntities + ","+ resultset.getString(2);
                    String rank = resultset.getString(4);
                    counter++;
                    if(counter>=docCount)
                        break;
                    }
                
                
            }
            
            queryEntities = new ArrayList(Arrays.asList(qEntities.split(",")));
            documentEntities = new ArrayList(Arrays.asList(docEntities.substring(3).split(",")));
            
            for(String entity:documentEntities){
                entity = entity.replace(" ", "");
                if(!entities.containsKey(entity))
                    entities.put(entity,1);
                else 
                    entities.put(entity,entities.get(entity)+1);
            }
            entities = sortHashMapByValues(entities, (entities.size()/4)+1);
            
            StringBuilder allEntites = new StringBuilder();
            for (String entity :queryEntities) {
                allEntites.append(","+entity);
            }
            for(String key : entities.keySet()){
                if(!key.equals(""))
                    allEntites.append(","+key);
            }
            query = "SELECT entity, title  from entity_"+dataset+" where entity in( "+allEntites.substring(1)+")";
            ResultSet resultset2 = SqlConnection.select(query);
            ArrayList<String> entityPair = new ArrayList<>();
            HashMap<String,String> entityTitle = new HashMap<>();
            while(resultset2.next()){
                
                String entity = resultset2.getString(1);
                String title  = resultset2.getString(2);
                if(title.equals("null")){
                    bw.write(entity+"\n");
                    bw.flush();
                }
                if(!entityTitle.containsKey(entity))
                    entityTitle.put(entity, title);
                
            }
            bw.close();
            for(String qEntity: queryEntities)
            {
                String qTitle = entityTitle.get(qEntity.replace(" ", ""));
                if(qTitle !=null){
                    for( String dEntity :entities.keySet() ){
                        String dTitle = entityTitle.get(dEntity);
                        if(dTitle != null){
                            entityPair.add(qTitle+" "+dTitle+" "+entities.get(dEntity));
                        }
                    }
                }
            }
            ESAtester_EN ee = new ESAtester_EN();
            ArrayList<String> entityPairResult =  ee.outputESA(entityPair);
            score = 0;
            for(String str:entityPairResult){
                score += Double.parseDouble(str.split(" ")[2]) * Double.parseDouble(str.split(" ")[3]);
//                score += Double.parseDouble(str.split(" ")[2]);
            }
            score = score/entities.size();
        } catch (SQLException ex) {
            Logger.getLogger(F01.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(F02.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(Exception ex){
            System.out.println(queryId+" has problem");
        }
        return score;
    }
    
    private LinkedHashMap<String, Integer> sortHashMapByValues( HashMap<String, Integer> passedMap, int size){
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapKeys, Collections.reverseOrder());
        Collections.sort(mapValues, Collections.reverseOrder());
        
        LinkedHashMap<String , Integer> sortedMap = new LinkedHashMap<>();
        LinkedHashMap<String , Integer> finalMap = new LinkedHashMap<>();
        Iterator<Integer> valueIt = mapValues.iterator();
        
        while(valueIt.hasNext() ){
            int val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();
            
            while(keyIt.hasNext()){
                String key = keyIt.next();
                int comp1 = passedMap.get(key);
                int comp2 = val;
                
                if(comp1 == comp2){
                    keyIt.remove();
                    sortedMap.put(key, val);
                    
                    break;
                }
            }
        }
        int counter = 0;
        Iterator it = sortedMap.entrySet().iterator();
        while(it.hasNext() && counter<size){
            Map.Entry pair = (Map.Entry) it.next();
            finalMap.put((String)pair.getKey(), (Integer)pair.getValue());
            counter++;
            it.remove();
            
        }
        return finalMap;
    }
}
