/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featurs;

import core.SqlConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Parastoo
 */
public class F02_TermBased {
    private String dataset;
    private String resultset;
    private int Start;
    private int end;
    private int docNumber;
    private double rate;
    private boolean avg;

    public F02_TermBased(String dataset, String resultset, int Start, int end, int docNumber, double rate, boolean avg) {
        this.dataset = dataset;
        this.resultset = resultset;
        this.Start = Start;
        this.end = end;
        this.docNumber = docNumber;
        this.rate = rate;
        this.avg = avg;
    }
    
    public void createHashmMapEntities() throws SQLException, IOException{
        String query = "SELECT d.entity,rs.document_id,rs.rank from "+resultset+"_"+dataset+" rs left join document_"+dataset+
                        " d on rs.document_id = d.title " +
                        "left join query_"+dataset+" q on rs.query_id = q.id where rs.rank < 51 and q.entity not like \'\' ";
        
        ResultSet resultset = SqlConnection.select(query);
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./result/"+resultset+"_"+dataset+"_docs_donot_have_entities.txt")));
        String docEntities = "";
        
        
        while(resultset.next()){
            if(resultset.getString(2)== null){
                bw.write("doc doesn't have entity : "+resultset.getString(3)+"\n");
                bw.flush();
            }
            else
            {
                docEntities  = docEntities + ","+ resultset.getString(1);
            }
        }
        ArrayList<String> documentEntities = new ArrayList(Arrays.asList(docEntities.substring(3).split(",")));
        HashMap<String,Integer> entities = new HashMap<>();
        for(String entity:documentEntities){
            entity = entity.replace(" ", "");
            if(!entities.containsKey(entity))
                entities.put(entity,1);
            else 
                entities.put(entity,entities.get(entity)+1);
        }
        
        
        
        
    }
    
    
}
