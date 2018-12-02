/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa.tools;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.queryparser.classic.ParseException;

import be.vanoosten.esa.EnwikiFactory;
import be.vanoosten.esa.WikiAnalyzer;
import be.vanoosten.esa.WikiFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import static org.apache.lucene.util.Version.LUCENE_48;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mohaddeseh
 */
public class ESAtester_EN {

    public static void main(String[] args) throws IOException {
        //String t1 = (String)args[0];
        //String t2 = (String)args[1];
        ESAtester_EN esa = new ESAtester_EN();
//        esa.outputESA("ESAData.txt");
//        String t1 = "London";
//        String t2 = "London";
//        try {
//            double esa = calcESA(t1, t2);
//            System.out.println("T1: " + t1 + " T2: " + t2 + " ESA: " + esa);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public ArrayList<String> outputESA(ArrayList<String> entityPair) throws FileNotFoundException, IOException {
//        String saveAddress = "./output/" + "ESA.txt";
      //  String saveExceptionAddress = "./output/exception/" + "ESA.txt";
        String output = "";
//        writeOutputToFile(output, saveAddress);
    //    writeOutputToFile(output, saveExceptionAddress);

//        BufferedReader br = new BufferedReader(new FileReader(filename));
        String count ="";
        ArrayList<String> entityPairResult = new ArrayList<>();
        for(String line : entityPair) {
            String entity1 = line.split(" ")[0];
            String entity2 = line.split(" ")[1];
            count = line.split(" ")[2];

            try {
                double esa = calcESA(entity1, entity2);
                output = entity1 + " " + entity2 + " " + esa +" "+count ;
                entityPairResult.add(output);
              //  System.out.println("T1: " + t1 + " T2: " + t2 + " ESA: " + esa);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

//            // System.out.println(entity);
//            if (output.contains("exception")) {
//                fileWriterContinue(output, saveExceptionAddress);
//
//            } else {
//                fileWriterContinue(output, saveAddress);
//            }
//            //   System.out.println(output);
        }
//        
//        System.out.println("F01.txt" + " done");
        return entityPairResult;

    }

    public static double calcESA(String t1, String t2) throws Exception {
        WikiFactory factory = new EnwikiFactory();
        CharArraySet stopWords = factory.getStopWords();

        String loc_index = "C:\\Development\\esa\\enwiki";
        WikiAnalyzer wa = new WikiAnalyzer(LUCENE_48, stopWords);
        Vectorizer vec = new Vectorizer(new File(loc_index), wa);

        SemanticSimilarityTool sst = new SemanticSimilarityTool(vec);

        double sim = sst.findSemanticSimilarity(t1, t2);
        factory.close();
        return sim;
    }

    private void writeOutputToFile(String content, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void fileWriterContinue(String content, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(content);
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
