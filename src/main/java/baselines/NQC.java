/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;


import java.util.ArrayList;
import java.util.List;
import org.lemurproject.galago.core.retrieval.Results;
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
public class NQC implements PerformancePredictor{
    Retrieval retrieval;
    
    public NQC(Retrieval retrieval) {
        this.retrieval = retrieval;
    }
    
    @Override
    public double predict(String queryText, Parameters parameters) throws Exception {
        Node root = StructuredQuery.parse(queryText);
        Parameters NQCParams = Parameters.create();
        NQCParams.set("requested", parameters.get("numDocs", 100));
        NQCParams.set("passageQuery", false);
        NQCParams.set("extentQuery", false);

        Node transformed = retrieval.transformQuery(root.clone(), NQCParams);
        List<ScoredDocument> results = retrieval.executeQuery(transformed, NQCParams).scoredDocuments;
        if (results.size() == 0)
            throw new Exception("There is no result.");
        List<Double> scores = new ArrayList();
        for (ScoredDocument sd : results)
            scores.add(sd.getScore());
        double stdev = StandardDeviation.calculateSD(scores);
        
        double colScore = QPPUtil.colQLScore(retrieval, root);
        
        return stdev/colScore;
    }
}
