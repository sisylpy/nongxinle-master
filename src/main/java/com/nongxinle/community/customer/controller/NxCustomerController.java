package com.nongxinle.community.customer.controller;

/**
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerEntity;
import com.nongxinle.community.customer.service.NxCustomerRegistrationService;
import com.nongxinle.community.customer.service.NxCustomerService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcustomer")
public class NxCustomerController {
	@Autowired
	private NxCustomerService nxCustomerService;
	@Autowired
	private NxCustomerRegistrationService nxCustomerRegistrationService;

	 @RequestMapping(value = "/getCommunityCustomers", method = RequestMethod.POST)
	  @ResponseBody
	  public R getCommunityCustomers (Integer page, Integer limit, Integer nxCommunityId ) {

		 Map<String, Object> map = new HashMap<>();
		 map.put("offset", (page - 1) * limit);
		 map.put("limit", limit);
		 map.put("nxCommunityId", nxCommunityId);

		 List<NxCustomerEntity> nxCustomerList = nxCustomerService.queryCommunityCustomers(map);
		 int total = nxCustomerService.queryCustomerOfCommunityTotal(map);

		 PageUtils pageUtil = new PageUtils(nxCustomerList, total, limit, page);

		 return R.ok().put("page", pageUtil);
	  }

	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxcustomer:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);

		List<NxCustomerEntity> nxCustomerList = nxCustomerService.queryList(map);
		int total = nxCustomerService.queryTotal(map);

		PageUtils pageUtil = new PageUtils(nxCustomerList, total, limit, page);

		return R.ok().put("page", pageUtil);
	}

	@ResponseBody
	@RequestMapping("/info/{customerId}")
	@RequiresPermissions("nxcustomer:info")
	public R info(@PathVariable("customerId") Integer customerId){
		NxCustomerEntity nxCustomer = nxCustomerService.queryObject(customerId);
		return R.ok().put("nxCustomer", nxCustomer);
	}

    /**
     * 青青乡小程序新用户注册（唯一有效注册入口）
     * promotionCode：正式推广码（来自 nx_customer_promotion_code）
     */
    @RequestMapping(value = "/saveNewCustomerMix", method = RequestMethod.POST)
    @ResponseBody
    public R saveNewCustomerMix(String phoneCode, String openId, Integer commerceId,
                                Integer commId, String promotionCode) {
        try {
            Map<String, Object> data = nxCustomerRegistrationService.registerNewCustomerMix(
                    phoneCode, openId, commerceId, commId, promotionCode);
            return R.ok().put("data", data);
        } catch (IllegalStateException e) {
            return R.error(-1, e.getMessage());
        }
    }

	@ResponseBody
	@RequestMapping("/update")
	public R update(@RequestBody NxCustomerEntity nxCustomer){
		nxCustomerService.update(nxCustomer);
		return R.ok().put("data", nxCustomer);
	}

	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxcustomer:delete")
	public R delete(@RequestBody Integer[] customerIds){
		nxCustomerService.deleteBatch(customerIds);
		return R.ok();
	}
}
