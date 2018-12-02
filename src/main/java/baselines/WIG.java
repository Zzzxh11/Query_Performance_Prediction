/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;



import java.util.List;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author zamani
 */
public class WIG implements PerformancePredictor {
    Retrieval retrieval;
    
    public WIG(Retrieval retrieval) {
        this.retrieval = retrieval;
    }
    
    @Override
    public double predict(String queryText, Parameters parameters) throws Exception {
        Node root = StructuredQuery.parse(queryText);
        Parameters params = Parameters.create();
        params.set("requested", parameters.get("numDocs", 5));
        params.set("passageQuery", false);
        params.set("extentQuery", false);

        Node transformed = retrieval.transformQuery(root.clone(), params);
        List<ScoredDocument> results = retrieval.executeQuery(transformed, params).scoredDocuments;
        if (results.size() == 0)
            throw new Exception("There is no result.");
        String[] qTerms = queryText.toLowerCase().split(" ");
        
        Parameters colParams = Parameters.create();
        colParams.set("requested", 1);
        colParams.set("passageQuery", false);
        colParams.set("extentQuery", false);
        colParams.set("mu", 1e100);
        Node colTransformed = retrieval.transformQuery(root.clone(), colParams);
        double colScore = retrieval.executeQuery(colTransformed, colParams).scoredDocuments.get(0).score;
        
        double score = 0.;
        for (ScoredDocument sd : results) {
            score += (sd.getScore() - colScore);
        }
        return score/(results.size())*Math.sqrt(qTerms.length);
    }
}
