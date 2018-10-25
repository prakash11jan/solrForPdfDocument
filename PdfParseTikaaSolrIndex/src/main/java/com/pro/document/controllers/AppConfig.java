package com.pro.document.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pro.document.service.DocumentIndexService;
import com.pro.document.service.DocumentSearchService;
import com.pro.document.service.impl.DocumentIndexServiceImpl;
import com.pro.document.service.impl.DocumentSearchServiceImpl;

@Configuration
public class AppConfig {
	
	@Bean
	DocumentIndexService DocumentIndexService()
	{
	    return new DocumentIndexServiceImpl();
	}
	
	@Bean
	DocumentSearchService DocumentSearchService()
	{
	    return new DocumentSearchServiceImpl();
	}

}
