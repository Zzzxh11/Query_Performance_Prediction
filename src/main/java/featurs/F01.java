/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featurs;

import com.github.andrewoma.dexx.collection.Pair;
import core.LanguageModel;
import core.SqlConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/*this class designed to get q and get resultlist of  
 *q and calculate lm of all doc and each doc and get kl divergence between them and calculate NQC score
 */
/**
 *
 * @author Parastoo
 */
public class F01 {
    public HashMap<String,Pair<String,String>> ResultList ;
    public double MaxScore;
    public double MinScore;
    
    public double TopScore;
    public double LowScore;
    
    public double TotalScore;
    
    public HashMap<String, Double>   GeneralLanguageModel;
    public HashMap<String, Double>   DocumentLanguageModel;
    public HashMap<String, Double>   KLDivergence;
    
    public double NQCPositive;
    public double NQCNegetive;
    
    public LanguageModel LM ;
    private final String dataset ;
    private final String rs;
    private final int docCount;
    private final int start;
    private final int end;
    private HashMap<String, String> documents;
    
    public F01(String ds ,String rs ,int docCount, int start,int end){
        this.rs =rs;
        this.docCount = docCount;
        ResultList = new HashMap<>();
        GeneralLanguageModel = new HashMap<>();
        DocumentLanguageModel = new HashMap<>();
        KLDivergence = new HashMap<>();
        LM = new LanguageModel();
        dataset = ds ;
        this.start= start;
        this.end = end;
        documents = new HashMap<>();
        
        
        
    }
    
    public void run(){
        for (int i = start; i <= end; i++) {
            ResultList = new HashMap<>();
            ResultList = getQueryResultList(Integer.toString(i));
            for (String key: ResultList.keySet()) {
                if(!documents.containsKey(ResultList.get(key)))
                    documents.put(key,ResultList.get(key).component1());
            }
        }
        LM.createGGLanguageModel(documents);
        
        for (int i = start; i <= end; i++) {
            ResultList = new HashMap<>();
            double Score = CalculateNQCScore(Integer.toString(i));
            System.out.println("query "+i+" :"+ Score);
        }
        
    }
    
    private double CalculateNQCScore (String queryId){
        ResultList = getQueryResultList(queryId);
        StringBuilder generalDocument =new StringBuilder();
        for(String key: ResultList.keySet()){
            generalDocument.append(","+ResultList.get(key).component1());
        }
        String content = generalDocument.toString().substring(1);
        GeneralLanguageModel = LM.createLanguageModel(content); 
        setScore(MaxScore,MinScore);
        
        int posCount = 0;
        int negCount = 0;
        double result = 0;
            
        for(String key : ResultList.keySet()){
            double score = Double.parseDouble(ResultList.get(key).component2());
            
//            if( score >= TopScore)
//            {
                DocumentLanguageModel = LM.createLanguageModel(ResultList.get(key).component1());
                double klScore = calculateKLDivergence(GeneralLanguageModel , DocumentLanguageModel);
                NQCPositive += Math.pow(klScore,2);;
                posCount++;
//            }
            
//            if( score <= LowScore)
//            {
//                DocumentLanguageModel = LM.createLanguageModel(ResultList.get(key).component1());
//                double klScore = calculateKLDivergence(GeneralLanguageModel , DocumentLanguageModel);
//                NQCNegetive += Math.pow(klScore,2);
//                negCount ++;
//            }
        }
        
        result = NQCPositive + NQCNegetive;
        result = result / (posCount + negCount);
        result = Math.sqrt(result);
        result = result / (double) (calculateKLDivergence(GeneralLanguageModel,LanguageModel.GGModel));
        
        
        return result;
    }

    private HashMap<String, Pair<String,String>> getQueryResultList(String queryId) {
        //set min and max score
        String query = "select d.title ,d.entity,s.score "
                + "from document_"+dataset+" d join "+rs+"_"+dataset+" s on d.title = s.document_id "
                + "where s.query_id = '"+queryId+"'"; 
        
        MinScore = Integer.MAX_VALUE ;
        MaxScore = Integer.MIN_VALUE ;
        TotalScore = 0;
        int counter = 0;
        ResultSet resultset =  SqlConnection.select(query);
        try {
            while(resultset.next()){
                
                String docId = resultset.getString(1);
                String docEntities = resultset.getString(2);
                String score = resultset.getString(3);
                Pair<String,String> pair = new Pair<>(docEntities,score);
                ResultList.put(docId,pair);
                TotalScore += Double.parseDouble(score);
                
                if (Double.parseDouble(score)>MaxScore)
                    MaxScore = Double.parseDouble(score);
                
                if(Double.parseDouble(score)<MinScore)
                    MinScore = Double.parseDouble(score);
                counter++;
                if(counter>=docCount)
                    break;
            }
            TotalScore = TotalScore / (double) ResultList.size();
        } catch (SQLException ex) {
            Logger.getLogger(F01.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ResultList;
    }

    private void setScore(double MaxScore, double MinScore) {
        
        double average = Math.abs(MaxScore-MinScore);
        average = average /(double) 4;
        TopScore = MaxScore - average;
        LowScore = MinScore + average;
        
    }

    private double calculateKLDivergence(HashMap<String, Double> generalLanguageModel, HashMap<String, Double> documentLanguageModel) {
        double result = 0;
        double probD = 0;
        double probG = 0;
        try{
            for (String key : documentLanguageModel.keySet()) {
                probD = documentLanguageModel.get(key);


                if(generalLanguageModel.containsKey(key) && probD !=0){
                    probG = generalLanguageModel.get(key);
                    result = result + ( probD * (Math.log(probD/(double)probG ) / Math.log(2) ));
                }
                
            }
        }
        catch(Exception ex){
            System.out.println(ex);
        }
           
        return result;
    }
    

    
}
