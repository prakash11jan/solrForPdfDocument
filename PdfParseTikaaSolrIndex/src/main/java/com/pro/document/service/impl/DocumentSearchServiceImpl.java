package com.pro.document.service.impl;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.pro.document.service.DocumentSearchService;
import com.pro.document.utils.DocumentUtill;

@Service
@PropertySource({ "classpath:application.properties" })
public class DocumentSearchServiceImpl implements DocumentSearchService{

	@Autowired
    private Environment env; 
	
	@Override
	public SolrDocumentList searchDocuments(String term) {
		SolrDocumentList results = null;
		if(StringUtils.isEmpty(term)){
			term="XC25";
		}
		HttpSolrClient solrClient = DocumentUtill.getClient(env.getProperty("solr.url"));
		SolrQuery query = new SolrQuery();
        //query.setQuery("sony digital camera");
        //String queryParams = "(content:(XC25 OR *XC25 OR *XC25* OR XC25*)) AND (prakField_text:(Prak* OR *prak* OR *Prak)) AND fq={!tag=prak}(prakField_text:Prakash*) AND facet.field={!ex=prak}prakField_text";
        String queryParams = "(content:(XC25 OR *XC25 OR *XC25* OR XC25*)) AND (prakField_string:(Prak* OR *prak* OR *Prak))";
        //String queryParams = "(prakField_text:(Prakash^200.0 OR Prakash*^100.0)) AND (content:(XC25^200.0 OR XC25~^100.0))";
       //String queryParams = "(prakField_text:(Prakash~^100.0)) AND (content:(XC25~^100.0))";
        query.setQuery(queryParams);
        //query.setFilterQueries("content:*XC25*","id:*210*");
        query.setStart(0);
      //  query.setMoreLikeThisFields("content");
      //  query.addTermsField("prakField_text");
        query.setRows(200);
        query.setFacetMinCount(1);
        //query.setParam("qf", "content,prakField_text");
        query.setFields("id,docType,prakField_string");
        
        //facet
        query.setFacet(true);
        query.addFacetField("prakField_string");
        //query.setFacetPrefix("prak");
        
        query.set("defType", "edismax");

        QueryResponse response;
		try {
			QueryRequest req = new QueryRequest(query);
			//req.setBasicAuthCredentials("solr", "SolrRocks");
			System.out.println("Query------------------>"+query.toQueryString());
			response = req.process(solrClient,"master_Test");
		
		
			results = response.getResults();
        List<FacetField> fflist = response.getFacetFields();
        for(FacetField ff : fflist){
            String ffname = ff.getName();
            int ffcount = ff.getValueCount();
            List<Count> counts = ff.getValues();
            for(Count c : counts){
                String facetLabel = c.getName();
                long facetCount = c.getCount();
                System.out.println(facetLabel +"( "+facetCount+" )");
            }
        }
        System.out.println(results.size());
        for (SolrDocument solrDocument : results) {
        	//solrDocument.getFieldValue("path");
        	//client.deleteByQuery("id:"+solrDocument.getFieldValue("id"));
        	//client.commit();
        	String a = (String) solrDocument.getFieldValue("docType");
        	System.out.println(a);
		} 
        
		} catch (SolrServerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        return results;
	}

}
