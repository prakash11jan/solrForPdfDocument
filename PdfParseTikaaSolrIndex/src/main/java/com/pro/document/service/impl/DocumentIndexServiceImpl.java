package com.pro.document.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.pro.document.service.DocumentIndexService;
import com.pro.document.utils.DocumentUtill;

@Service
@PropertySource({ "classpath:application.properties" })
public class DocumentIndexServiceImpl implements DocumentIndexService {

	public final static String STATUS_SUCCESS = "SUCCESS";
	public final static String STATUS_RUNNING = "RUNNING";
	public final static String STATUS_FAILED = "FAILED";
	//private static AtomicInteger indexingState = new AtomicInteger(0);
	private static AtomicBoolean indexingState = new AtomicBoolean();
	static String SOLR_URL = "http://localhost:8983/solr"; 
	
	@Autowired
    private Environment env; 
	
	@Override
	public String indexDocuments() {
		if(indexingState.compareAndSet(false, true)){
			System.out.println("index Run state is--->"+indexingState.get());
		HttpSolrClient solrClient = DocumentUtill.getClient(env.getProperty("solr.url"));
		try {
        	UpdateRequest rreq = new UpdateRequest();
        	 List<SolrInputDocument> docs = getPdf();
        	 
        	 rreq.add(docs);
        	// rreq.setBasicAuthCredentials("solr", "SolrRocks");
        	 rreq.commit(solrClient,env.getProperty("solr.document.core.name"));
		} catch (SolrServerException | IOException | SAXException | TikaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			indexingState.compareAndSet(true,false);
			System.out.println("index Run state is--->"+indexingState.get());
			return STATUS_FAILED;
			
		}
		
		 System.out.println("Completed the indexing.......");
		 indexingState.compareAndSet(true,false);
		 System.out.println("index Run state is--->"+indexingState.get());
		 return STATUS_SUCCESS;
		}
		return STATUS_RUNNING;
	}
	
	private  List<SolrInputDocument> getPdf() throws IOException, SAXException, TikaException {
		// File folder = new File("C:/eclipse/workspace/springMvc/SolrTest/resources");
		List<SolrInputDocument> docs = new ArrayList<>();
		String root = env.getProperty("document.root.directory");
		String[] folders = env.getProperty("document.types").split(",");
		System.out.println("Rooooooooooooooooooooooooooooooooooot---->"+root);
		 //String[] folders = {"Technical", "Marketing","Warranty"};
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
	      System.out.println("Processing......."+file.getName());
	      PDFParser pdfparser = new PDFParser(); 
	      
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
