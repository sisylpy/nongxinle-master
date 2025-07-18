package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 09-20 00:57
 */

import java.util.HashMap;
import java.util.List;

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.service.NxCommunityService;
import com.nongxinle.service.NxCustomerUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerUserAddressEntity;
import com.nongxinle.service.NxCustomerUserAddressService;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcustomeruseraddress")
public class NxCustomerUserAddressController {
	@Autowired
	private NxCustomerUserAddressService nxCustomerUserAddressService;
	@Autowired
	private NxCustomerUserService nxCustomerUserService;
	@Autowired
	private NxCommunityService nxCommunityService;


	@RequestMapping(value = "/getCommityByAddressId/{id}")
	@ResponseBody
	public R getCommityByAddressId(@PathVariable Integer id) {
		NxCustomerUserAddressEntity addressEntity = nxCustomerUserAddressService.queryObject(id);
		String nxCuaLocation = addressEntity.getNxCuaLocation();
		List<NxCommunityEntity> communityEntities =  nxCommunityService.queryCommunityListByUserPoint(nxCuaLocation);

		return R.ok().put("data", communityEntities);
	}


	@RequestMapping(value = "/deleteUserAddress/{id}")
	@ResponseBody
	public R deleteUserAddress(@PathVariable Integer id) {
	    nxCustomerUserAddressService.delete(id);
	    return R.ok();
	}

	@RequestMapping(value = "/userGetAddress/{userId}")
	@ResponseBody
	public R userGetAddress(@PathVariable Integer userId) {
	   List<NxCustomerUserAddressEntity> addressEntities =  nxCustomerUserAddressService.queryAddressByUserId(userId);
	    return R.ok().put("data", addressEntities);
	}

	@RequestMapping(value = "/userSaveAddress", method = RequestMethod.POST)
	@ResponseBody
	public R userSaveAddress (@RequestBody NxCustomerUserAddressEntity address) {
		List<NxCustomerUserAddressEntity> addressEntities =  nxCustomerUserAddressService.queryAddressByUserId(address.getNxCuaCustomerUserId());
        if(addressEntities.size() == 0){
        	address.setNxCuaIsSelected(1);
		}else{
        	address.setNxCuaIsSelected(0);
		}
		nxCustomerUserAddressService.save(address);
		NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(address.getNxCuaCustomerUserId());

		NxCustomerUserAddressEntity addressEntity = nxCustomerUserAddressService.queryMainAddressByUserId(address.getNxCuaCustomerUserId());
        userEntity.setMainAddress(addressEntity);
		return R.ok().put("data", userEntity);
	}


	@RequestMapping(value = "/userEditAddress", method = RequestMethod.POST)
	@ResponseBody
	public R userEditAddress (@RequestBody NxCustomerUserAddressEntity address) {
		nxCustomerUserAddressService.update(address);
		return R.ok();
	}
}
