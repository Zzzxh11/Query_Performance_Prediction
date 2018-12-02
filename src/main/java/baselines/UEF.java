/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.prf.RelevanceModel1;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author zamani
 */
public class UEF implements PerformancePredictor{
    Retrieval retrieval;
    
    public UEF(Retrieval retrieval) {
        this.retrieval = retrieval;
    }
    
    @Override
    public double predict(String queryText, Parameters parameters) throws Exception {
        Node root = StructuredQuery.parse(queryText);
        Parameters params = Parameters.create();
        params.set("requested", parameters.get("numDocs", 100));
        params.set("passageQuery", false);
        params.set("extentQuery", false);
        Node transformed = retrieval.transformQuery(root.clone(), params);
        List<ScoredDocument> qlResults = retrieval.executeQuery(transformed, params).scoredDocuments;
        
        params.set("requested", 10000);
        Node expandedQuery = new RelevanceModel1(retrieval).expand(root.clone(), parameters.clone());
        Node expandedTransformedQuery = retrieval.transformQuery(expandedQuery.clone(), params);
        List<ScoredDocument> rmResults = retrieval.executeQuery(expandedTransformedQuery, params).scoredDocuments;
        
        Map <String, Double> qlResultsMap = new HashMap <> ();
        List <Double> qlScores = new ArrayList <> ();
        List <Double> rmScores = new ArrayList <> ();
        
        for (ScoredDocument sd : qlResults) {
            qlResultsMap.put(sd.documentName, sd.score);
        }
        
        for (ScoredDocument sd : rmResults) {
            if (qlResultsMap.containsKey(sd.documentName)) {
                qlScores.add(qlResultsMap.get(sd.documentName));
                rmScores.add(sd.score);
            }
        }

        double sim = pearsonCorrelation(qlScores, rmScores);
        
        parameters.set("numDocs", parameters.get("fbDocs", 20));
        double wig = new WIG(retrieval).predict(queryText, parameters);
        return sim * wig;
    }
    
    private double pearsonCorrelation(List <Double> a, List <Double> b) {
        double cov = 0, var1 = 0, var2 = 0;
        for (int i=0; i<a.size(); i++) {
            for (int j=i+1; j<a.size(); j++) {
                cov += (a.get(i)-a.get(j))*(b.get(i)-b.get(j));
                var1 += (a.get(i)-a.get(j))*(a.get(i)-a.get(j));
                var2 += (b.get(i)-b.get(j))*(b.get(i)-b.get(j));
            }
        }
        return cov / Math.sqrt(var1 * var2);
    }
    
}
