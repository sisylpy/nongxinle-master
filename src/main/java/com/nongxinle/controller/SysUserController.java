package com.nongxinle.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.SysUserEntity;
import com.nongxinle.service.SysUserRoleService;
import com.nongxinle.service.SysUserService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import com.nongxinle.utils.ShiroUtils;

/**
 * 系统用户
 * 
 */
@RestController
@CrossOrigin
@RequestMapping("/sys/user")
public class SysUserController extends AbstractController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysUserRoleService sysUserRoleService;
	
	/**
	 * 所有用户列表
	 */
	@RequestMapping("/list")
	@RequiresPermissions("sys:user:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<SysUserEntity> userList = sysUserService.queryList(map);
		int total = sysUserService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(userList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	/**
	 * 获取登录的用户信息
	 */
	@RequestMapping("/info")
	public R info(){
		System.out.println("useiieieiinfofNxnxnxnxnxnx" + getUser());
		SysUserEntity user = getUser();
		String username = user.getUsername();

		SysUserEntity sysUserEntity = sysUserService.queryNxUserByUserName(username);
		Map<String, Object> map = new HashMap<>();
		map.put("user", getUser());
		map.put("disUser", sysUserEntity);
		return R.ok().put("data", map);
	}

	@RequestMapping("/infoGb")
	public R infoGb(){
		System.out.println("userrieiieieiinfofofof" + getUser());
		SysUserEntity user = getUser();
		String username = user.getUsername();

		Map<String, Object> mapU = new HashMap<>();
		mapU.put("username", username );
		SysUserEntity sysUserEntity = sysUserService.queryGbUserByUserName(mapU);
		Map<String, Object> map = new HashMap<>();
		map.put("user", getUser());
		map.put("disUser", sysUserEntity);
		return R.ok().put("data", map);
	}
	
	/**
	 * 修改登录用户密码
	 */
	@RequestMapping("/password")
	public R password(String password, String newPassword){
		if(StringUtils.isBlank(newPassword)){
			return R.error("新密码不为能空");
		}
		
		//sha256加密
		password = new Sha256Hash(password).toHex();
		//sha256加密
		newPassword = new Sha256Hash(newPassword).toHex();
				
		//更新密码
		int count = sysUserService.updatePassword(getUserId(), password, newPassword);
		if(count == 0){
			return R.error("原密码不正确");
		}
		
		//退出
		ShiroUtils.logout();
		
		return R.ok();
	}

	@RequestMapping(value = "/getUserInfoByUserId/{userId}")
	@ResponseBody
	public R getUserInfoByUserId(@PathVariable Long userId ) {
		System.out.println("getUserInfoByUserIdgetUserInfoByUserId");
		SysUserEntity user = sysUserService.queryObject(userId);

		//获取用户所属的角色列表
		List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
		user.setRoleIdList(roleIdList);

		return R.ok().put("user", user);

	}
//
	/**
	 * 用户信息
	 */
	@RequestMapping("/info/{userId}")
	@RequiresPermissions("sys:user:info")
	@CrossOrigin(origins = "*")
	public R info(@PathVariable("userId") Long userId){
		System.out.println("userIduserIduserId" + userId);

		SysUserEntity user = sysUserService.queryObject(userId);
		
		//获取用户所属的角色列表
		List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
		user.setRoleIdList(roleIdList);
		
		return R.ok().put("user", user);
	}

	@RequestMapping("/infoGbUser")
	public R infoGbUser(){

		return R.ok().put("user", getUser());
	}
	
	/**
	 * 保存用户
	 */
	@RequestMapping("/save")
	@RequiresPermissions("sys:user:save")
	public R save(@RequestBody SysUserEntity user){
		if(StringUtils.isBlank(user.getUsername())){
			return R.error("用户名不能为空");
		}
		if(StringUtils.isBlank(user.getPassword())){
			return R.error("密码不能为空");
		}
		
		sysUserService.save(user);
		
		return R.ok();
	}
	
	/**
	 * 修改用户
	 */
	@RequestMapping("/update")
	@RequiresPermissions("sys:user:update")
	public R update(@RequestBody SysUserEntity user){
		if(StringUtils.isBlank(user.getUsername())){
			return R.error("用户名不能为空");
		}
		
		sysUserService.update(user);
		
		return R.ok();
	}
	
	/**
	 * 删除用户
	 */
	@RequestMapping("/delete")
	@RequiresPermissions("sys:user:delete")
	public R delete(@RequestBody Long[] userIds){
		if(ArrayUtils.contains(userIds, 1L)){
			return R.error("系统管理员不能删除");
		}
		
		if(ArrayUtils.contains(userIds, getUserId())){
			return R.error("当前用户不能删除");
		}
		
		sysUserService.deleteBatch(userIds);
		
		return R.ok();
	}
}
