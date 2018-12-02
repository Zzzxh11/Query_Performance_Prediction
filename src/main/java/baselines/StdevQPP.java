/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;


import java.util.ArrayList;
import java.util.List;

import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;
import util.StandardDeviation;

/**
 *
 * @author zamani
 */
public class StdevQPP implements PerformancePredictor{
    Retrieval retrieval;
    
    public StdevQPP(Retrieval retrieval) {
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
        List<ScoredDocument> results = retrieval.executeQuery(transformed, params).scoredDocuments;
        List<Double> scores = new ArrayList();
        for (ScoredDocument sd : results) {
            if (parameters.get("StdevApproach").equals("fixed"))
                scores.add(sd.getScore());
            else if (parameters.get("StdevApproach").equals("cutOf")) {
                if (Math.exp(sd.getScore()) < Math.exp(results.get(0).getScore())*parameters.get("StdevCutOf", 0.05))
                    break;
                scores.add(sd.getScore());
            }
        }
        double result = StandardDeviation.calculateSD(scores);
        if (parameters.get("NormalizeStdev", true))
            result /= Math.sqrt(queryText.split(" ").length);
        
        return result;
    }
}
