/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;

import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author zamani
 */
public interface PerformancePredictor {
    public double predict(String queryText, Parameters parameters) throws Exception;
}
