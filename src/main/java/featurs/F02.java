/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featurs;

//import com.github.andrewoma.dexx.collection.Pair;
import be.vanoosten.esa.tools.ESAtester_EN;
import com.github.andrewoma.dexx.collection.Pair;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.mapping.Collection;
import query_performance_prediction.Query_performance_prediction;

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
    
    public int r;
    private ArrayList<String> queryEntities;
    private ArrayList<String> documentEntities;
    public HashMap<String, Double> predictions = new HashMap<>();
    public HashMap<String,Integer> collection ;
    public HashMap<String,Integer> entities ;
    public HashMap<String,Double> colScores;
    public HashMap<String,String> entityTitle ;
    public BufferedWriter resultwriter;
    public ArrayList<String> docs = new ArrayList<>();
    public HashMap<String,String> queries = new HashMap<>();
    public String qEntities ;
    public boolean avg;
    public String filename;
    public HashMap<String,String> queryEntity;
    public BufferedWriter collectionScoreBw;
    public ESAtester_EN ee ;
    private float rate;
    
    
    public F02(String ds ,String rs ,int docCount, int start,int end,float rate , boolean avg){
        this.dataset = ds;
        this.rs = rs;
        this.docCount = docCount;
        this.start = start;
        this.end = end;
        this.rate = rate;
        this.avg = avg;
        try {
            this.filename= "./crossfold/"+dataset+"/result/"+rs+"-"+dataset+"-"+docCount+"docs-"+rate+"percentEntities-"+avg+"avg-F02.txt";
            File f = new File(filename);
            if(!f.exists())
                resultwriter = new BufferedWriter(
                    new FileWriter(f));
        } catch (IOException ex) {
            Logger.getLogger(F02.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.ee = new ESAtester_EN();
        
    }
    
    public void run() throws IOException, SQLException{
        
        if(resultwriter != null){
            
//            String colFile = "./crossfold/"+dataset+"/result/"+rs+"-"+dataset+"collectionScore-F02.txt";
//            colScores = Query_performance_prediction.readPerfFromFils(colFile);
            createEntitiyTitle();
            
            for (int i = start; i <= end; i++) {

                    docs = new ArrayList<>();
                    qEntities ="";
                    readTopDocument(Integer.toString(i));
//                    CalculateSimilarityScoreByDoc(Integer.toString(i));
                    CalculateSimilarityScore(Integer.toString(i));

            }
            resultwriter.close();
        }
    }
    
    private double CalculateSimilarityScoreByDoc(String queryId) {
        double score = 0;
        try {
            qEntities = queries.get(queryId);
            queryEntities = getTitle(qEntities); 
            
            if(!queryEntities.isEmpty()){
                for(String doc : docs){
                    documentEntities = getTitle(doc);
                    double docScore = calculateDocScore(queryEntities,documentEntities);
                    score += docScore;
                }
                
                predictions.put(queryId,score);
                resultwriter.write(queryId+ " "+ score);
                resultwriter.write("\n");
                resultwriter.flush();
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(F02.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(Exception ex){
            System.out.println(queryId+" has problem");
        }
        return score;
    }
    
    private LinkedHashMap<String, Integer> sortHashMapByValues( HashMap<String, Integer> passedMap, double size,int r){
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
        while(it.hasNext() &&  counter<size  ){
            Map.Entry pair = (Map.Entry) it.next();
            if( (Integer)pair.getValue() >=r)
            {   finalMap.put((String)pair.getKey(), (Integer)pair.getValue());
                counter++;
                
            }
            it.remove();
            
        }
        return finalMap;
    }
    
    public void runCollection() throws SQLException, IOException{
        
            createEntitiyTitle();
            
            for (int i = start; i <= end; i++) {

                    readTopDocument(Integer.toString(i));
                    System.out.println(i);
            }
            String colFileName = "./crossfold/"+dataset+"/result/"+rs+"-"+dataset+"collectionScore-F02.txt";
            resultwriter = new BufferedWriter(new FileWriter(new File(colFileName)));
            for (int i = start; i <= end; i++) {

//                    CalculateSimilarityScoreByDoc(Integer.toString(i));
                CalculateSimilarityScore(Integer.toString(i));
            }
            resultwriter.close();
    }

    private void createEntitiyTitle() throws SQLException {
        entityTitle = new HashMap<>();
        String  query = "SELECT entity, title  from entity_"+dataset;
        ResultSet resultset2 = SqlConnection.select(query);


        while(resultset2.next()){

            String entity = resultset2.getString(1);
            String title  = resultset2.getString(2);

            if(!entityTitle.containsKey(entity))
                entityTitle.put(entity, title);

        }
    }

    private void readTopDocument(String queryId) throws IOException, SQLException {
       String query = "SELECT q.entity,d.entity,rs.document_id,rs.rank from "+rs+"_"+dataset+" rs left join document_"+dataset+
                        " d on rs.document_id = d.title " +
                        "left join query_"+dataset+" q on rs.query_id = q.id where q.id = "+queryId +
                " and q.entity not like \'\'  order by rs.rank limit 20 "; 
        
        
        int counter = 0;
        ResultSet resultset = SqlConnection.select(query);
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./result/"+rs+"_"+dataset+"_docs_donot_have_entities.txt")));
       
        while(resultset.next()){

            qEntities = resultset.getString(1);
            qEntities = qEntities.replace(" ", "");
            qEntities = qEntities.replace("\t", "");
            if(!queries.containsKey(queryId))
                queries.put(queryId, qEntities);
            if(resultset.getString(2)== null){
                bw.write("doc doesn't have entity : "+resultset.getString(3)+"\n");
                bw.flush();
            }
            else
            {
                docs.add(resultset.getString(2));
                counter++;
                if(counter>=docCount)
                    break;
                }


        }
        bw.close();
        
    }

    private double calculateDocScore(ArrayList<String> queryEntities, ArrayList<String> docEntities) throws IOException {
        
        double tempScore = 0;
        entities = new HashMap<>();
        for(String entity:docEntities){
            if(entity.length()!= 0 && !entity.equals("null")){
                if(!entities.containsKey(entity))
                    entities.put(entity,1);
                else 
                    entities.put(entity,entities.get(entity)+1);
            }
        }
        
        customizeEntityList();
        ArrayList<String> entityPair = new ArrayList<>();
        for(String qTitle: queryEntities)
        {
            for( String dTitle :entities.keySet() ){

                    entityPair.add(qTitle+" "+dTitle+" "+entities.get(dTitle));

            }
            
        }
        
        ArrayList<String> entityPairResult =ee.outputESA(entityPair);
        double count = 0;

        for(String str:entityPairResult){
            System.out.println(str);
            if(str.contains(" null")){
                System.out.println("here");
                
            }
            else {

                    tempScore += Double.parseDouble(str.split(" ")[2]) * Double.parseDouble(str.split(" ")[3]);
                    count+= Double.parseDouble(str.split(" ")[3]);

            }

        }
        System.out.println("numberOfEntities: "+entities.size());
        if (count !=0)
            tempScore = tempScore/count;
        else 
            tempScore=0;
        
        return tempScore;
            
    }

    private void customizeEntityList() {
        r=0;
        for(String key:entities.keySet()){
            r +=entities.get(key);       
        }
        r = r/entities.size();
        System.out.println("r is"+r);
        if(!avg)
            r=1;
        if(rate<=1)
            entities = sortHashMapByValues(entities, ((entities.size()*rate))+1,r);
        else
            entities = sortHashMapByValues(entities, rate,r);

    }

    private ArrayList<String> getTitle(String entities) {
        ArrayList<String> temp = new ArrayList(Arrays.asList(entities.split(",")));
        ArrayList<String> result = new ArrayList<>();
        for (String entity : temp) {
            entity = entity.replace(" ", "");
            entity = entity.replace("\t", "");
            if(entityTitle.containsKey(entity) && entity.length() !=0 && entity != null ){
                if(entityTitle.get(entity).length() !=0 && entityTitle.get(entity) != null)
                    result.add(entityTitle.get(entity));
            }
        }
        return result;
    }

    private double CalculateSimilarityScore(String queryId) {
        double score = 0;
        try {
            qEntities = queries.get(queryId);
            queryEntities = getTitle(qEntities); 
            
            if(!queryEntities.isEmpty()){
                String combination ="";
                for(String doc : docs){
                    combination += doc+",";
                    
                }
                documentEntities = getTitle(combination);
                score = calculateDocScore(queryEntities,documentEntities);
                predictions.put(queryId,score);
                resultwriter.write(queryId+ " "+ score);
                resultwriter.write("\n");
                resultwriter.flush();
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(F02.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(Exception ex){
            ex.printStackTrace();
            System.out.println(queryId+" has problem");
        }
        return score;
    
    }


}
