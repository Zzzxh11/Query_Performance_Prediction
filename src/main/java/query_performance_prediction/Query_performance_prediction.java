/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query_performance_prediction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import core.LanguageModel;
import core.SqlConnection;
import featurs.F01;
import featurs.F01_TermBased;
import featurs.F02;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Parastoo
 */
public class Query_performance_prediction {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        
        SqlConnection.getInstance();
        String dataset = "2009";
        String resultSet= "eqfe";
        int documentNumber = 3;
        int start = 1;
        int end = 200;
//        F02 f02 = new F02(dataset,resultSet,documentNumber,start,end);
//        f02.run();
        
        
        
        Query_performance_prediction  q = new Query_performance_prediction();
//        q.insertToDataBase(resultSet, dataset);
//        q.insertDocEntities();
//        q.insertDocEntities();
        q.mergeFiles();
//        q.insertToDataBase(resultSet, dataset);
//        q.createMap(resultSet,dataset);

    }
    public void  insertToDataBase(String rs,String dataset){
        MysqlDataSource dataSource ;
        Connection connection;
        Statement statement;
        dataSource = new MysqlDataSource();
        dataSource.setUser("root");
        dataSource.setPassword("137428sAm");
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("query_performance_prediction");
        dataSource.setUseUnicode(true);
        dataSource.setCharacterEncoding("UTF-8");
        
        
    
        
 
        String filePath = "./data/"+rs+"-"+dataset+".txt";
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            
            
            String line ;
            
            int lineNumber =1;
            
//            String createTable = " CREATE table IF NOT EXISTS "+rs+"_"+dataset+" (\n" +
//                                "     query_id varchar(700) ,\n" +
//                                "     document_id varchar(700)  ,\n" +
//                                "     rank int(11),\n" +
//                                "     score varchar(700),\n" +
//                                "     primary key (query_id, document_id)\n" +
//                                "     \n" +
//                                " )";
//            statement.executeUpdate(createTable);
            while((line = br.readLine() )!= null)  {
                if(lineNumber>0){
                    String[] lineSplit = line.split(" ");
                    if(lineSplit.length>1){


                        String query_id = lineSplit[0];
                        String document_id = lineSplit[2];
                        String rank = lineSplit[3];
                        String score = lineSplit[4];

                        try{
                            PreparedStatement updateSales = connection.prepareStatement(
                                "insert into "+rs+"_"+dataset+" (query_id,document_id,rank,score) values(?,?,?,?)");
                            updateSales.setString(1, query_id); 
                            updateSales.setString(2, document_id);
                            updateSales.setInt(3, Integer.parseInt(rank));
                            updateSales.setString(4, score);
                            int row = updateSales.executeUpdate();
                            if (row > 0) {
                                System.out.println("A contact was inserted");

                            } 

                        System.out.println(lineNumber);
                        }
                        catch(Exception ex){
                            System.out.println("repeated");
                            statement.close();
                            connection.close();
                            connection = dataSource.getConnection();
                            statement = connection.createStatement();
                        }
                    }
                }
                lineNumber ++;
            }
            
            
            connection.close();
            br.close();
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        
    }
    
    public void createMap(String rs, String dataset){
                
        
 
        String filePath = "./data/map-"+rs+"-"+dataset+".txt";
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            FileWriter writer = new FileWriter(new File("./data/map-"+rs+"_"+dataset+"-result.txt"));
            
            
            String line ;
            
            int lineNumber =1;
                    
            while((line = br.readLine() )!= null)  {
                if(line.contains("map")){
                   writer.write(line+"\n");
                }
                lineNumber ++;
                
            }
            
            
           
            br.close();
            writer.close();
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        
    }
    
    public void mergeFiles(){
        try(PrintWriter pw = new PrintWriter("./data/clueweb2009Merged.txt")) {
            File f = new File("C:\\Users\\Parastoo\\Documents\\course\\arshad\\term2\\queryexpantion\\clue09Fix\\data\\faezeh\\SELM\\Repository\\cluewebPoolFiles\\share\\TestData\\ClueWeb09PoolSourceFile_Fix");
            String[] s = f.list();
            if (s == null) {
                throw new IOException("Directory doesn't exist: " + f);
            }
            for (String s1 : s)
            {
                File f1 = new File(f, s1);
                if (!f1.isFile()) {
                    continue;
                }
                try (Reader reader = new FileReader(f1);
                        BufferedReader br = new BufferedReader(reader)) {
                    pw.println("<DOC>");
                    pw.println("<DOCNO>"+s1+"</DOCNO>");
                    pw.println("<TEXT>");
                    System.out.println(s1);
                    String line = br.readLine();
                    while (line != null)
                    {
                        pw.println(line);
                        line = br.readLine();
                    }
                    pw.println("</TEXT>");
                    pw.println("</DOC>");
                }
            }
        } catch (Throwable e) {
            e.getMessage();
        }
    }
    
    public void insertDocEntities(){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/Robust/document/missed/document_entities.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            
            String line ;
            
            int lineNumber =1;
            
//            String createTable = " CREATE table IF NOT EXISTS document_Robust (\n" +
//                                "     id int(11) ,\n" +
//                                "     title varchar(10000)  ,\n" +
//                                "     entity longtext,\n" +
//                                "     primary key (id)\n" +
//                                "     \n" +
//                                " )";
//            statement.executeUpdate(createTable);
            int docCount =438859;
            String lastId = "LA050690-0181_0";
            ArrayList<String> entList = new ArrayList<>();;
            line = br.readLine();
            while(line != null)  {
                try{
                    if(lineNumber>773257){
                        
                        System.out.println(lineNumber);
                        
                        String docId = line.split("\t")[0];
                        String entitiesWithConf = line.split("\t")[1];
                        if(docId.equals(lastId)){
                            String[] ent_conf = entitiesWithConf.split(" ");
                        
                            for (String temp : ent_conf) {
                                entList.add(temp.split("\\|")[0]);
                            }
//                            for (int i = 1; i < ent_conf.length; i++) {
//                                entList.add(ent_conf[i].split("\\|")[0]);
//                            }
                            line = br.readLine();
                            lineNumber++;
                        }
                        else{
                            String entities = String.join(",",entList);
                            PreparedStatement update =connection.prepareStatement( 
                                    "insert into document_robust (id,title,entity) values(?,?,?)");
                            update.setInt(1, docCount);
                            update.setString(2, lastId);
                            update.setString(3, entities);
                            int row2 = update.executeUpdate();
                            if(row2<=0){
                                System.out.println("problem");
                                line = br.readLine();
                                lineNumber++;
                            }
                            else{
                                docCount++;
                            }
                            update.close();
                            entList = new ArrayList<>();
                            lastId = docId;
                        }
                        
                        
//                        String entities = String.join(",",entList);
//                        String CheckExist = 
//                                    "SELECT * FROM document_Robust d WHERE d.title = '"+docId+"' ";
//                        
//                        ResultSet rs = statement.executeQuery(CheckExist);
//                        if (rs.next()) {
//                            entities = ","+entities;
//                            PreparedStatement update =connection.prepareStatement( "update document_Robust "
//                                    + "set entity = "
//                                    + "CONCAT(entity , ? )"
//                                    + "where title = ? ");
//                            
//                            update.setString(1, entities);
//                            update.setString(2, docId);
//                            int row2 = update.executeUpdate();
//                            if(row2<=0){
//                                System.out.println("problem");
//                            }
//                            update.close();
//                        } 
//                        else{
//                            PreparedStatement update =connection.prepareStatement( 
//                                    "insert into document_Robust (id,title,entity) values(?,?,?)");
//                            update.setInt(1, docCount);
//                            update.setString(2, docId);
//                            update.setString(3, entities);
//                            int row2 = update.executeUpdate();
//                            if(row2<=0){
//                                System.out.println("problem");
//                            }
//                            else{
//                                docCount++;
//                            }
//                            update.close();
//                        }
                    }  
                    else{
                        line = br.readLine();
                        lineNumber++;
                        if(lineNumber == 312517)
                            System.out.println(line);
                    }
                }
                catch(SQLException sx){
                    System.out.println("point 1: "+sx.getMessage());
                    line=br.readLine();
                    lineNumber++;
                }
                catch(Exception ex){
                    System.out.println("point 2: "+ex.getStackTrace());   
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    line=br.readLine();
                    lineNumber++;
                    

                }
                        
                
                
            }
            
            
            connection.close();
            br.close();
        }    
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }
    }
    
    public void insertQueryEntities(){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/Robust/query/query-entities-robust.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            
            String line ;
            
            int lineNumber =1;

            while((line= br.readLine())!= null)  {
                try{
                    
                    System.out.println(lineNumber);
                    String queryId = line.split(": ")[0];
                    String entities = line.split(": ")[1];


                    PreparedStatement update =connection.prepareStatement( 
                            "insert into query_Robust (id,title,entity) values(?,'',?)");
                    update.setString(1, queryId);
                    update.setString(2, entities);
                    int row2 = update.executeUpdate();
                    if(row2<=0){
                        System.out.println("problem");
                    }

                    update.close();
                    lineNumber++;
                        
               
                }
                catch(SQLException sx){
                    System.out.println("point 1: "+sx.getMessage());
                    
                }
                catch(Exception ex){
                    System.out.println("point 2: "+ex.getMessage());   
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    
                    

                }
                        
                
                
            }
            
            
            connection.close();
            br.close();
        }    
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }

    }
    
    public void createEntitySet(){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/Robust/query/query-entities-robust.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            
            String line ;
            
            int lineNumber =1;

            while((line= br.readLine())!= null)  {
                try{
                    
                    System.out.println(lineNumber);
                    String queryId = line.split(": ")[0];
                    String[] entities = line.split(": ")[1].split(",");
                    for (String str :entities) {
                        System.out.println(str);
                    }
                    lineNumber++;
                        
               
                }
                
                catch(Exception ex){
                    System.out.println("point 2: "+ex.getMessage());   
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    
                    

                }
                        
                
                
            }
            
            
            connection.close();
            br.close();
        }    
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }
    }
    
    public void insertEntities(){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/Robust/entities/result_unavailable.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            
            String line ;
            
            int lineNumber =1;
            int id =47738;
            while((line= br.readLine())!= null)  {
                try{
                    
                    System.out.println(lineNumber);
                    String entity = line.split(" ")[0];
                    String title = line.split(" ")[1];
                    title = title.replace(" ", "");
                    title = title.replace("!", "");
                    title = title.replace("\"", "");
                    title = title.replace("*", "");
                    title = title.replace("?", "");


                    PreparedStatement update =connection.prepareStatement( 
                            "insert into entity_Robust (id,title,entity,owner) values(?,?,?,'query')");
                    update.setInt(1, id);
                    update.setString(2, title);
                    update.setString(3, entity);
                    int row2 = update.executeUpdate();
                    if(row2<=0){
                        System.out.println("problem");
                        
                    }
                    id++;
                    update.close();
                    lineNumber++;
                        
               
                }
                catch(SQLException sx){
                    System.out.println("point 1: "+sx.getMessage());
                    
                }
                catch(Exception ex){
                    System.out.println("point 2: "+ex.getMessage());   
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    
                    

                }
                        
                
                
            }
            
            
            connection.close();
            br.close();
        }    
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }
    }
}
