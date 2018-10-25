package com.pro.document.controllers;

import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.pro.document.service.DocumentIndexService;
import com.pro.document.service.DocumentSearchService;
import com.pro.document.service.SolrIndexer;


@Controller
public class DocumentSolrIndexController {
	@Autowired
	DocumentIndexService DocumentIndexService;
	
	@Autowired
	DocumentSearchService DocumentSearchService;
	@GetMapping ("/playagame")
	public String playGame(
			@RequestParam(name="choice", required=false) 
			    String theChoice, 
			       Model model) {
		
		
		if (theChoice == null) {
			return "index";
		}
		
		String theOutcome = "error";
		if (theChoice.equals("rock")) {
			theOutcome = "tie";
		}
		if (theChoice.equals("paper")) {
			theOutcome = "win";
		}
		if (theChoice.equals("scissors")) {
			theOutcome = "loss";
		}
		
		model.addAttribute("outcome", theOutcome);
		return "results";
		
	}
	
	@GetMapping ("/index")
	public String indexDocs(Model model) {
		System.out.println("fffffffffffffffffffffffffffffffffffffffffffffffff");
		String result =DocumentIndexService.indexDocuments();
		if("RUNNING".equals(result)){
			return "running";
		}
		return "results";
	}
	
	@RequestMapping(value = "/fetchDocs", method ={ RequestMethod.GET }, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public SolrDocumentList fetchDocs(Model model, @RequestParam(name="q", required=false) String term)
	{
		return DocumentSearchService.searchDocuments(term);
	}
}
