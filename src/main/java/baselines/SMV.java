/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.prf.RelevanceModel1;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;
import util.StandardDeviation;

/**
 *
 * @author zamani
 */
public class SMV implements PerformancePredictor{
    Retrieval retrieval;
    
    public SMV(Retrieval retrieval) {
        this.retrieval = retrieval;
    }
    
    @Override
    public double predict(String queryText, Parameters parameters) throws Exception {
        Node root = StructuredQuery.parse(queryText);
        Parameters params = Parameters.create();
        params.set("requested", parameters.get("numDocs", 1000));
        params.set("passageQuery", false);
        params.set("extentQuery", false);

        Node transformed = retrieval.transformQuery(root.clone(), params);
        List<ScoredDocument> results = retrieval.executeQuery(transformed, params).scoredDocuments;
        List<Double> scores = new ArrayList();
        for (ScoredDocument sd : results) {
            scores.add(sd.getScore());
        }
        double mean = StandardDeviation.mean(scores);
        
        double result = 0;
        for (double score : scores) {
            result += score * Math.abs(Math.log(score/mean));
        }
        
        double colScore = QPPUtil.colQLScore(retrieval, root);
        
        return result/(colScore*scores.size());
    }
}
