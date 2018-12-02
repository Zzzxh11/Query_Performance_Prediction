/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.prf.RelevanceModel1;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author zamani
 */

// Clarify Score
public class Clarity implements PerformancePredictor {
    Retrieval retrieval;
    
    public Clarity(Retrieval retrieval) {
        this.retrieval = retrieval;
    }

    @Override
    public double predict(String queryText, Parameters parameters) throws Exception {
        Node root = StructuredQuery.parse(queryText);
        Node expandedQuery = null;
        try {
            expandedQuery = new RelevanceModel1(retrieval).expand(root.clone(), parameters.clone());
        } catch (Exception ex){
            return 0;
        }
        
        // Generating a Map from each term to a real-valued weight for the query
        List <Map<String, Double>> queryMaps = new ArrayList<>();
        Map <String, Double> origQuery = nodeToMap(root);
        Map <String, Double> modifiedQuery = new HashMap <> ();
        for (String key : origQuery.keySet()) {
            if ((double)retrieval.getNodeStatistics(new Node("counts", key)).nodeFrequency > 0)
                modifiedQuery.put(key, origQuery.get(key));
        }
        queryMaps.add(normalize(modifiedQuery));
        
        queryMaps.add(normalize(nodeToMap(expandedQuery)));
        
        List <Double> weights = new ArrayList <>();
        weights.add(parameters.get("fbOrigWeight", 0.5));
        weights.add(1.0-parameters.get("fbOrigWeight", 0.5));
        Map <String, Double> queryMap = interpolateWeightedUnigrams(queryMaps, weights);
        
        if (queryMap.keySet().size() < 20)
            throw new Exception("bad query!");
        // Computing the score
        double score = 0.;
        for (String key : queryMap.keySet()) {
            double p_w_q = queryMap.get(key); // p(w|Q)
            double p_w_c = (double)retrieval.getNodeStatistics(new Node("counts", key)).nodeFrequency / (double)retrieval.getCollectionStatistics(new Node("lengths")).collectionLength; ; // p(w | C)
            if (p_w_c == 0)
                continue;
            score += (p_w_q * Math.log(p_w_q / p_w_c));
        }
                
        return score;
    }
    
    public static Map<String, Double> nodeToMap(Node node) {
        Map<String, Double> result = new HashMap <> ();
        for (int i=0; i<node.numChildren(); i++){
            String term = node.getChild(i).getNodeParameters().get("default", "");
            double score = node.getNodeParameters().get("" + i, 0.);
            result.put(term, score);
        }
        return result;
    }
    
    public static Map<String, Double> normalize(Map<String, Double> input) {
        double sum = 0.;
        double eps = 1e-20;
        for (String key : input.keySet())
            sum += (input.get(key) + eps);
        for (String key : input.keySet())
            input.put(key, (input.get(key) + eps)/sum);
        return input;
    }
    
    public Map<String, Double> interpolateWeightedUnigrams(List<Map<String, Double>> inputs, List<Double> weights){
        Map <String, Double> output = new HashMap <> ();
        for (int i=0; i<inputs.size(); i++) {
            double weight = weights.get(i);
            for (String key : inputs.get(i).keySet()) {
                if (!output.containsKey(key))
                    output.put(key, 0.);
                output.put(key, output.get(key) + weight * inputs.get(i).get(key));
            }
        }
        return output;
    }
}
