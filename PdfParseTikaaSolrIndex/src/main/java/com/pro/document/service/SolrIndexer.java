package com.pro.document.service;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

@Configuration
public class SolrIndexer {
	static String SOLR_URL = "http://localhost:8983/solr"; 
	

	public void executeAll() {
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		System.out.println("Building Solr server instance");
		
        HttpSolrClient solrClient=new HttpSolrClient.Builder(SOLR_URL).build();    
        
        //
        //String solrDir = "C:/solr/solr-7.3.0/server/solr/configsets/_default/conf";
        String solrDir = "C:/solr/proCluster/master";
        String baseSolrUrl = "http://localhost:8983/solr";
       //SolrClient solrclient = new HttpSolrClient(baseSolrUrl);
        HttpSolrClient client = new HttpSolrClient.Builder(baseSolrUrl).build();
        createCore();
        //
        System.out.println("Requesting core list"); 
        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminAction.STATUS);
       // request.setBasicAuthCredentials("solr", "SolrRocks");
        CoreAdminResponse cores=null;
       
       // solr.setParser(new XMLResponseParser());
       long from = System.currentTimeMillis();
       
       	//indexDocs();
        long to = System.currentTimeMillis();
        long res = to-from;
        //long minutes = (res / 1000) ;
        //long minutes = TimeUnit.MILLISECONDS.toMinutes(res);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(res);
       System.out.println("Total time taken in seconds::: "+seconds);
       
        // search query
        
       searchQuery("");
       // displayCores(cores); 
         
    }

	private void displayCores(CoreAdminResponse cores) {
		System.out.println(" Listing cores");
        List<String> coreList = new ArrayList<String>();
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            coreList.add(cores.getCoreStatus().getName(i));
            System.out.println(cores.getCoreStatus().getName(i));
        }

        
	}

	public void createCore() {
		HttpSolrClient client = getClient();
		String solrDir = "C:/solr/proCluster/master";
		
		CoreAdminRequest.Create createRequest = new CoreAdminRequest.Create();
        createRequest.setCoreName("master_Test");
        createRequest.setInstanceDir(solrDir);
			try {
				//createRequest.setBasicAuthCredentials("solr", "SolrRocks");
				CoreAdminResponse cores=createRequest.process(client);
				displayCores(cores);
			} catch (SolrServerException | IOException | RemoteSolrException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void searchQuery(String term) {
		if(StringUtils.isEmpty(term)){
			term="XC25";
		}
		HttpSolrClient solrClient = getClient();
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
			req.setBasicAuthCredentials("solr", "SolrRocks");
			response = req.process(solrClient,"master_Test");
		
		
        SolrDocumentList results = response.getResults();
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
        
        
        // search query

	}

	public CoreAdminResponse getCores() {
		HttpSolrClient solrClient = getClient();
		CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminAction.STATUS);
        CoreAdminResponse cores=null;
		try {
            cores = request.process(solrClient);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return cores;
	}

	public void indexDocs() {
		HttpSolrClient solrClient = getClient();
		try {
        	UpdateRequest rreq = new UpdateRequest();
        	 List<SolrInputDocument> docs = getPdf();
        	 
        	 rreq.add(docs);
        	// rreq.setBasicAuthCredentials("solr", "SolrRocks");
        	 rreq.commit(solrClient, "master_Test");
		} catch (SolrServerException | IOException | SAXException | TikaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		 System.out.println("Completed the indexing.......");
	}       
	
	
	private HttpSolrClient getClient() {
		HttpSolrClient solrClient=new HttpSolrClient.Builder(SOLR_URL).build();
		return solrClient;
	}

	private  List<SolrInputDocument> getPdf() throws IOException, SAXException, TikaException {
		// File folder = new File("C:/eclipse/workspace/springMvc/SolrTest/resources");
		List<SolrInputDocument> docs = new ArrayList<>();
		String root = "C:/solr/Docs";
		 String[] folders = {"Technical",
				 "Marketing","Warranty"};
	  for (String folder: folders) {
		 getDocuments(root,folder.toString(),docs);
		   }
	  System.out.println("Completed the All PDF parsing.......");
	   return docs;   
	}
	
	private List<SolrInputDocument> getDocuments(String root,String folder, List<SolrInputDocument> docs) throws IOException, SAXException, TikaException{
		 
		 File actualFolder = new File(root+"//"+folder);
		 getDocs(folder, docs, actualFolder,false);
		 File actualFolderAuth = new File(root+"//"+folder+"Authenticated");
		 getDocs(folder, docs, actualFolderAuth,true);
		 return docs;
	      
	}

	private void getDocs(String folder, List<SolrInputDocument> docs, File actualFolder, boolean isAuthenticated)
			throws FileNotFoundException, IOException, SAXException, TikaException {
		int i =0;
		for (File file: actualFolder.listFiles()) {
		      SolrInputDocument document = new SolrInputDocument();
		      Metadata metadata = new Metadata();
		      StringWriter textBuffer = new StringWriter();
				BodyContentHandler handler = new BodyContentHandler(textBuffer);
			      
		      //FileInputStream inputstream = new FileInputStream(new File(classLoader.getResource("210658.pdf").getFile()));
	      FileInputStream inputstream = new FileInputStream(new File(file.getPath()));
	      ParseContext pcontext = new ParseContext();
	      
	      //parsing the document using PDF parser
	      PDFParser pdfparser = new PDFParser(); 
	      System.out.println("Processing......."+file.getName());
	      pdfparser.parse(inputstream, handler, metadata,pcontext);
	      String[] metadataNames = metadata.names();
	      //getting the content of the document
	      document.addField("id", file.getName());
	      document.addField("path", file.getPath());
	      document.addField("docType", folder);
	        document.addField("content", handler.toString());
	        document.addField("authourized", isAuthenticated);
	        if(i>1){
	        document.addField("prakField_string", "prakash ram");
	        }else{
	        	document.addField("prakField_string", "haramahadev");
	        }
	        i++;
	        StringBuilder builder = new StringBuilder("meta not indexed as this is not required");
	       // for(String name : metadataNames) {
	        	//builder.append( metadata.get(name));
	        	//System.out.println(name+"::"+metadata.get(name));
	        
		     // }
	        document.addField("metaData", builder.toString());
	        docs.add(document);
	     // System.out.println("Contents of the PDF :" + builder.toString());
	      
	      //getting metadata of the document
	    //  System.out.println("Metadata of the PDF:");
	      }
		
	}
	
	

}
