/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;

import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author zamani
 */
public class QPPUtil {
    public static double colQLScore(Retrieval retrieval, Node query) throws Exception {
        Parameters colParams = Parameters.create();
        colParams.set("requested", 1);
        colParams.set("passageQuery", false);
        colParams.set("extentQuery", false);
        colParams.set("mu", 1e100);
        Node colTransformed = retrieval.transformQuery(query.clone(), colParams);
        return retrieval.executeQuery(colTransformed, colParams).scoredDocuments.get(0).score;
    }
}
