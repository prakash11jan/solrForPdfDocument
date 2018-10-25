package com.pro.document.service;

import org.apache.solr.common.SolrDocumentList;
import org.springframework.context.annotation.Configuration;


public interface DocumentSearchService {
	
	SolrDocumentList searchDocuments(String term);

}
