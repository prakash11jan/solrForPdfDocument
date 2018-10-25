package com.lennoxpro.document.utils;

import org.apache.solr.client.solrj.impl.HttpSolrClient;

public class LennoxDocumentUtill {
	
	public static HttpSolrClient getClient(String solrUrl) {
		HttpSolrClient solrClient=new HttpSolrClient.Builder(solrUrl).build();
		return solrClient;
	}

}
