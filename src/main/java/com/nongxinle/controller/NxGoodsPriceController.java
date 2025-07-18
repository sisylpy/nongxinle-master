package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 03-09 08:28
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxGoodsPriceService;


@RestController
@RequestMapping("api/nxgoodsprice")
public class NxGoodsPriceController {
	@Autowired
	private NxGoodsPriceService nxGoodsPriceService;
	

	
}
