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
public class QF implements PerformancePredictor {
    Retrieval retrieval;
    
    public QF(Retrieval retrieval) {
        this.retrieval = retrieval;
    }

    @Override
    public double predict(String queryText, Parameters parameters) throws Exception {
        parameters.set("requested", parameters.get("numDocs", 50));
        Node root = StructuredQuery.parse(queryText);
        Node queryTransformed = retrieval.transformQuery(root.clone(), parameters.clone());
        List <ScoredDocument> origResults = retrieval.executeQuery(queryTransformed, parameters.clone()).scoredDocuments;
        Node expandedQuery = null;
        try {
            expandedQuery = new RelevanceModel1(retrieval).expand(root.clone(), parameters.clone());
        } catch (Exception ex){
            return 0;
        }
        Node expandedQuerytransformed = retrieval.transformQuery(expandedQuery.clone(), parameters.clone());
        List <ScoredDocument> expandedResults = retrieval.executeQuery(expandedQuerytransformed, parameters.clone()).scoredDocuments;
        Set <String> docNames = new HashSet <> ();
        for (ScoredDocument sd : expandedResults)
            docNames.add(sd.documentName);
        int overlap = 0;
        for (ScoredDocument sd : origResults)
            if (docNames.contains(sd.documentName))
                overlap++;
        
        return (double)overlap/origResults.size();
    }
    
}
