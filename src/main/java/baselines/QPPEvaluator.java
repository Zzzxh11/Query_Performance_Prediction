/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baselines;

import com.github.andrewoma.dexx.collection.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author zamani
 */
public class QPPEvaluator {
    
    public static List <Pair<String, Double>> readPerfFromFile (String filename, String targetMetric) throws FileNotFoundException {
        List <Pair<String, Double>> result = new ArrayList <> ();
        Scanner in = new Scanner(new File(filename));
        int i = 1;
        while (in.hasNextLine()){
            String metric = in.next();
            String qid = in.next();
            if (qid.equals("all"))
                break;
//            String val = in.next();
//            System.err.println(i);
//            i++;
//            Double.parseDouble(val);
            double val = in.nextDouble();
            if (metric.equals(targetMetric))
                result.add(new Pair<String, Double>(qid, val));
        }
        in.close();
        return result;
    }
    
    public String[] evalAll(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        String[] result = new String[3];
        //System.err.println("Spearmann Corr: " + computeSpearmannCorrelation(a, b));
        result[0] = "Spearmann Corr: " +computeSpearmannCorrelation(a, b);
        //System.err.println("Pearson Corr: " + computePearsonCorrelation(a, b));
        result[1] = "Pearson Corr: " + computePearsonCorrelation(a, b);
        //System.err.println("Kendall's Tau: " + computeKendallsTauDistance(a, b));
        result[2] = "Kendall's Tau: " + computeKendallsTauDistance(a, b);
        return  result;
    }
    
    public double computeSpearmannCorrelation(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        Comparator comparator = new Comparator<Pair<String, Double>>(){
            @Override
            public int compare(Pair<String, Double> p1, Pair<String, Double> p2) {
                return p2.component2().compareTo(p1.component2());
            }
        };
        Collections.sort(a, comparator);
        Collections.sort(b, comparator);
        
        Map <String, Integer> first = getMapRank(a);
        Map <String, Integer> second = getMapRank(b);
        
        double cov = 0, var1 = 0, var2 = 0;
        for (int i=0; i<a.size(); i++) {
            for (int j=i+1; j<a.size(); j++) {
                String q_i = a.get(i).component1();
                String q_j = a.get(j).component1();
                cov += ((first.get(q_i) - first.get(q_j)) * (second.get(q_i) - second.get(q_j)));
                var1 += ((first.get(q_i) - first.get(q_j)) * (first.get(q_i) - first.get(q_j)));
                var2 += ((second.get(q_i) - second.get(q_j)) * (second.get(q_i) - second.get(q_j)));
            }
        }
        return cov / Math.sqrt(var1 * var2);
    }
    
    public double computePearsonCorrelation(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        Map <String, Double> first = pairToMap(a);
        Map <String, Double> second = pairToMap(b);
        
        double cov = 0, var1 = 0, var2 = 0;
        for (int i=0; i<a.size(); i++) {
            for (int j=i+1; j<a.size(); j++) {
                String q_i = a.get(i).component1();
                String q_j = a.get(j).component1();
                cov += ((first.get(q_i) - first.get(q_j)) * (second.get(q_i) - second.get(q_j)));
                var1 += ((first.get(q_i) - first.get(q_j)) * (first.get(q_i) - first.get(q_j)));
                var2 += ((second.get(q_i) - second.get(q_j)) * (second.get(q_i) - second.get(q_j)));
            }
        }
        return cov / Math.sqrt(var1 * var2);
    }
    
    public double computeKendallsTauDistance(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        Map <String, Double> first = pairToMap(a);
        Map <String, Double> second = pairToMap(b);
        
        int concordant = 0, discordant = 0;
        for (int i=0; i<a.size(); i++) {
            for (int j=i+1; j<a.size(); j++) {
                String q_i = a.get(i).component1();
                String q_j = a.get(j).component1();
                if ((first.get(q_i) - first.get(q_j))*(second.get(q_i) - second.get(q_j)) < 0)
                    discordant++;
                if ((first.get(q_i) - first.get(q_j))*(second.get(q_i) - second.get(q_j)) > 0)
                    concordant++;
            }
        }
        double z = (double)(a.size()*(a.size()-1))/2.;
        return (double)(concordant-discordant)/z;
    }
    
    public Map <String, Integer> getMapRank (List <Pair<String, Double>> sortedList) {
        Map <String, Integer> result = new HashMap <> ();
        int rank = 0;
        double previousScore = -1000;
        for (int i=0; i<sortedList.size(); i++) {
            if (sortedList.get(i).component2() != previousScore) {
                rank++;
                previousScore = sortedList.get(i).component2();
            }
            result.put(sortedList.get(i).component1(), rank);
        }
        return result;
    }
    
    public Map <String, Double> pairToMap (List <Pair<String, Double>> list) {
        Map <String, Double> result = new HashMap <> ();
        for (int i=0; i<list.size(); i++) {
            result.put(list.get(i).component1(), list.get(i).component2());
        }
        return result;
    }
}
