package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 07-30 18:51
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerAliasEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.service.NxDistributerAliasService;
import com.nongxinle.service.NxDistributerGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxAliasEntity;
import com.nongxinle.service.NxAliasService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxalias")
public class NxAliasController {
	@Autowired
	private NxAliasService nxAliasService;
	@Autowired
	private NxDistributerGoodsService nxDistributerGoodsService;
	@Autowired
	private NxDistributerAliasService nxDistributerAliasService;

	@RequestMapping(value = "/saveAlias", method = RequestMethod.POST)
	@ResponseBody
	public R saveAlias (@RequestBody NxAliasEntity alias) {

		String nxAliasName = alias.getNxAliasName();
		String pinyin = hanziToPinyin(nxAliasName);
		String headPinyin = getHeadStringByString(nxAliasName, false, null);
		alias.setNxAliasPinyin(pinyin);
		alias.setNxAliasPy(headPinyin);
	    nxAliasService.save(alias);
		Integer nxAlsGoodsId = alias.getNxAlsGoodsId();

		Map<String, Object> map = new HashMap<>();
		map.put("nxGoodsId", nxAlsGoodsId);
		List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.queryDisGoodsByParams(map);
		if(distributerGoodsEntities.size() > 0){
			for(NxDistributerGoodsEntity distributerGoodsEntity: distributerGoodsEntities){
				Integer distributerGoodsId = distributerGoodsEntity.getNxDistributerGoodsId();
				NxDistributerAliasEntity aliasEntity = new NxDistributerAliasEntity();
				aliasEntity.setNxDaAliasName(alias.getNxAliasName());
				aliasEntity.setNxDaDisGoodsId(distributerGoodsId);
				aliasEntity.setNxDaNxAliasId(alias.getNxAliasId());
				aliasEntity.setNxDaAliasPinyin(pinyin);
				aliasEntity.setNxDaAliasPy(headPinyin);
				nxDistributerAliasService.save(aliasEntity);
			}
		}

		return R.ok().put("data",alias);
	}

	@RequestMapping(value = "/updateAlias", method = RequestMethod.POST)
	@ResponseBody
	public R updateAlias (@RequestBody NxAliasEntity alias) {

		String nxAliasName = alias.getNxAliasName();
		String pinyin = hanziToPinyin(nxAliasName);
		String headPinyin = getHeadStringByString(nxAliasName, false, null);
		alias.setNxAliasPinyin(pinyin);
		alias.setNxAliasPy(headPinyin);
	    nxAliasService.update(alias);

		Integer nxAlsGoodsId = alias.getNxAliasId();

		Map<String, Object> map = new HashMap<>();
		map.put("nxId", nxAlsGoodsId);
		System.out.println("mappapa" + map);
		List<NxDistributerAliasEntity> aliasEntities =  nxDistributerAliasService.queryAliasByParmas(map);
		if(aliasEntities.size() > 0){
			for(NxDistributerAliasEntity aliasEntity: aliasEntities){
				aliasEntity.setNxDaAliasName(alias.getNxAliasName());
				aliasEntity.setNxDaAliasPinyin(pinyin);
				aliasEntity.setNxDaAliasPy(headPinyin);
				nxDistributerAliasService.update(aliasEntity);
			}
		}

		return R.ok();
	}


	@RequestMapping(value = "/deleteAlias/{id}")
	@ResponseBody
	public R deleteAlias(@PathVariable Integer id) {
	    nxAliasService.delete(id);

		Map<String, Object> map = new HashMap<>();
		map.put("nxId", id);
		System.out.println("mappapa" + map);
		List<NxDistributerAliasEntity> aliasEntities =  nxDistributerAliasService.queryAliasByParmas(map);
		if(aliasEntities.size() > 0){
			for(NxDistributerAliasEntity aliasEntity: aliasEntities){
				nxDistributerAliasService.delete(aliasEntity.getNxDistributerAliasId());
			}
		}
	    return R.ok();
	}

	
}
