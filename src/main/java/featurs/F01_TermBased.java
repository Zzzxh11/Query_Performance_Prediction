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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Parastoo
 */
public class F01_TermBased {
    public HashMap<String,Pair<String,String>> ResultList ;
    public double MaxScore;
    public double MinScore;
    
    public double TopScore;
    public double LowScore;
    
    public double TotalScore;
    
    public double NQCPositive;
    public double NQCNegetive;
    
    public LanguageModel LM ;
    public String dataset ;
    public String rs;
    public int docCount;
    
    public F01_TermBased(String ds,String rs ,int docCount){
        this.rs =rs;
        this.docCount = docCount;
        ResultList = new HashMap<>();
        dataset = ds ;
        
        
    }
    
    public double CalculateNQCScore (String queryId){
        ResultList = getQueryResultList(queryId);
        double mislead = 0;
        for(String key: ResultList.keySet()){
            mislead += Double.parseDouble(ResultList.get(key).component2());
        }
        mislead = mislead / ResultList.size();
        
        int posCount = 0;
        int negCount = 0;
        double result = 0;
            
        for(String key : ResultList.keySet()){
            double score = Double.parseDouble(ResultList.get(key).component2());
            
            if( score > mislead)
            {
                NQCPositive += Math.pow(score-mislead,2);
                posCount++;
            }
            
            if( score <= mislead)
            {
                NQCNegetive += Math.pow(score-mislead,2);
                negCount ++;
            }
        }
        
        result = NQCPositive + NQCNegetive ;
        result = Math.pow(result,2);
        result = result / (posCount + negCount);
        result = Math.sqrt(result);
        //calculate score(D);
        
        
        return result;
    }

    private HashMap<String, Pair<String,String>> getQueryResultList(String queryId) {
        //set min and max score
        String query = "select d.title ,d.entity,s.score "
                + "from document_"+dataset+" d join ql_result s on d.title = s.document_id "
                + "where s.query_id = '"+queryId+"'"; 
        
        MinScore = Integer.MAX_VALUE ;
        MaxScore = Integer.MIN_VALUE;
        TotalScore = 0; 
        int counter = 0 ;
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
                if(counter>docCount)
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

    private double calculateKLDivergence(HashMap<String, Double> GeneralLanguageModel, HashMap<String, Double> DocumentLanguageModel) {
        double result = 0;
        double probD = 0;
        double probG = 0;
        for (String key : GeneralLanguageModel.keySet()) {
            probG = GeneralLanguageModel.get(key);
            
            if (probG == 0)
                probG = 0.000000001;
            
            if(DocumentLanguageModel.containsKey(key))
                probD = DocumentLanguageModel.get(key);
            else
                probD = 0.000000001;
            result = result + ( probG * (Math.log(probD/(double)probG ) / Math.log(2) ));
        }
        return result;
    }
    
}
