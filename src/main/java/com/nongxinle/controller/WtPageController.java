package com.nongxinle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 系统页面视图
 * 
 */
@Controller
public class WtPageController {

	@RequestMapping("wt/{url}.html")
	public String page(@PathVariable("url") String url){

		return "wt/" + url + ".html";
	}


}
