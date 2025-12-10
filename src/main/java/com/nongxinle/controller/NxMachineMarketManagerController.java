package com.nongxinle.controller;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxMachineMarketManagerEntity;
import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.NxMachineMarketManagerService;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.R;
import com.nongxinle.utils.UploadFile;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场管理员Controller
 *
 * @author lpy
 * @date 2025-10-17
 */
@RestController
@RequestMapping("/api/marketmanager")
public class NxMachineMarketManagerController {

    @Autowired
    private NxMachineMarketManagerService marketManagerService;

    @Autowired
    private SysCityMarketService sysCityMarketService;

    /**
     * 市场管理员注册（带文件上传）
     * 
     * @param file 头像文件
     * @param code 微信授权码
     * @param marketId 市场ID
     * @param managerName 管理员姓名
     * @param phone 手机号
     * @param role 角色（1-普通管理员 2-市场主管）
     * @param session HTTP会话
     * @return 注册结果
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R register(@RequestParam("file") MultipartFile file,
                     @RequestParam("code") String code,
                     @RequestParam("marketId") Integer marketId,
                     @RequestParam("managerName") String managerName,
                     @RequestParam("phone") String phone,
                     @RequestParam(value = "role", defaultValue = "1") Integer role,
                     HttpSession session) {
        
        System.out.println("========== 市场管理员注册开始 ==========");
        System.out.println("请求参数 - code: " + code);
        System.out.println("请求参数 - marketId: " + marketId);
        System.out.println("请求参数 - managerName: " + managerName);
        System.out.println("请求参数 - phone: " + phone);
        System.out.println("请求参数 - role: " + role);
        
        try {
            // 1. 验证市场是否存在
            SysCityMarketEntity market = sysCityMarketService.queryObject(marketId);
            if (market == null) {
                System.out.println("错误：市场不存在，marketId=" + marketId);
                return R.error("市场不存在");
            }
            
            // 2. 通过微信code获取openid（使用市场管理小程序的AppID）
            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getPrinterAlertAppId(); // 打印机管理小程序
            String secret = myAPPIDConfig.getPrinterAlertSecret();
            
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + 
                        "&secret=" + secret + 
                        "&js_code=" + code + 
                        "&grant_type=authorization_code";
            
            System.out.println("微信授权URL: " + url);
            
            // 发送请求，返回Json字符串
            String str = WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("微信响应: " + str);
            
            // 转成Json对象 获取openid
            JSONObject jsonObject = JSONObject.parseObject(str);
            
            if (jsonObject.get("openid") == null) {
                System.out.println("错误：获取openid失败，响应=" + str);
                
                // 解析微信返回的错误信息
                if (jsonObject.get("errcode") != null) {
                    Integer errcode = jsonObject.getInteger("errcode");
                    String errmsg = jsonObject.getString("errmsg");
                    System.out.println("微信错误码：" + errcode + "，错误信息：" + errmsg);
                    
                    // 针对常见错误给出友好提示
                    if (errcode == 40029) {
                        return R.error("授权码已失效，请重新获取授权");
                    } else if (errcode == 40163) {
                        return R.error("授权码已被使用，请重新获取");
                    } else {
                        return R.error("微信授权失败：" + errmsg);
                    }
                }
                
                return R.error("获取微信授权信息失败，请重试");
            }
            
            String openid = jsonObject.get("openid").toString();
            String unionid = jsonObject.get("unionid") != null ? jsonObject.get("unionid").toString() : null;
            System.out.println("获取到openid: " + openid);
            
            // 3. 检查openid是否已注册
            NxMachineMarketManagerEntity existManager = marketManagerService.queryByOpenid(openid);
            if (existManager != null) {
                System.out.println("错误：该微信用户已注册，managerId=" + existManager.getNxMmId());
                return R.error(-1, "该微信用户已注册为市场管理员，请直接登录");
            }
            
            // 4. 检查手机号是否已注册
            NxMachineMarketManagerEntity existPhone = marketManagerService.queryByPhone(phone);
            if (existPhone != null) {
                System.out.println("错误：该手机号已注册，managerId=" + existPhone.getNxMmId());
                return R.error(-1, "该手机号已被注册");
            }
            
            // 5. 上传头像文件
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);
            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            
            System.out.println("头像上传成功，路径: " + filePath);

            // 6. 检查该市场是否已有管理员，如果是第一个管理员，自动设置为市场主管
            List<NxMachineMarketManagerEntity> existingManagers = marketManagerService.queryByMarketId(marketId);
            Integer finalRole = role;
            
            if (existingManagers == null || existingManagers.isEmpty()) {
                // 市场的第一个管理员，自动设置为市场主管
                finalRole = 2;
                System.out.println("这是市场的第一个管理员，自动设置为市场主管（role=2）");
            } else {
                System.out.println("市场已有 " + existingManagers.size() + " 个管理员，使用前台提交的角色：" + role);
            }

            // 7. 创建市场管理员实体
            NxMachineMarketManagerEntity manager = new NxMachineMarketManagerEntity();
            manager.setNxMmMarketId(marketId);
            manager.setNxMmWxOpenid(openid);
            manager.setNxMmWxUnionid(unionid);
            manager.setNxMmWxNickname(managerName); // 暂时用姓名作为昵称
            manager.setNxMmWxAvatar(filePath);
            manager.setNxMmPhone(phone);
            manager.setNxMmName(managerName);
            manager.setNxMmRole(finalRole); // 使用处理后的角色
            manager.setNxMmStatus(-1); // 待开通（需要审核）
            
            // 7. 保存到数据库
            marketManagerService.save(manager);
            
            System.out.println("市场管理员注册成功，managerId=" + manager.getNxMmId());
            
            // 8. 查询完整的管理员信息（包含市场信息）
            NxMachineMarketManagerEntity savedManager = marketManagerService.queryObject(manager.getNxMmId());
            savedManager.setSysCityMarketEntity(market);
            
            // 9. 构建返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("managerId", savedManager.getNxMmId());
            resultData.put("managerName", savedManager.getNxMmName());
            resultData.put("phone", savedManager.getNxMmPhone());
            resultData.put("role", savedManager.getNxMmRole());
            resultData.put("avatar", savedManager.getNxMmWxAvatar());
            resultData.put("marketId", market.getSysCityMarketId());
            resultData.put("marketName", market.getSysCmMarketName());
            
            System.out.println("========== 市场管理员注册完成 ==========");
            
            return R.ok().put("data", resultData);
            
        } catch (Exception e) {
            System.out.println("市场管理员注册异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("注册失败：" + e.getMessage());
        }
    }
    
    /**
     * 市场管理员登录（微信小程序）
     * 
     * @param code 微信授权码
     * @return 登录结果
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public R login(@RequestParam("code") String code) {
        
        System.out.println("========== 市场管理员登录开始 ==========");
        System.out.println("请求参数 - code: " + code);
        
        try {
            // 1. 通过微信code获取openid（使用市场管理小程序的AppID）
            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getPrinterAlertAppId(); // 打印机管理小程序
            String secret = myAPPIDConfig.getPrinterAlertSecret();
            
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + 
                        "&secret=" + secret + 
                        "&js_code=" + code + 
                        "&grant_type=authorization_code";
            
            String str = WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("微信响应: " + str);
            
            JSONObject jsonObject = JSONObject.parseObject(str);
            
            if (jsonObject.get("openid") == null) {
                System.out.println("错误：获取openid失败，响应=" + str);
                
                // 解析微信返回的错误信息
                if (jsonObject.get("errcode") != null) {
                    Integer errcode = jsonObject.getInteger("errcode");
                    String errmsg = jsonObject.getString("errmsg");
                    System.out.println("微信错误码：" + errcode + "，错误信息：" + errmsg);
                    
                    // 针对常见错误给出友好提示
                    if (errcode == 40029) {
                        return R.error("授权码已失效，请重新登录");
                    } else if (errcode == 40163) {
                        return R.error("授权码已被使用，请重新登录");
                    } else {
                        return R.error("微信授权失败：" + errmsg);
                    }
                }
                
                return R.error("获取微信授权信息失败，请重试");
            }
            
            String openid = jsonObject.get("openid").toString();
            System.out.println("获取到openid: " + openid);
            
            // 2. 根据openid查询管理员
            NxMachineMarketManagerEntity manager = marketManagerService.queryByOpenid(openid);
            
            if (manager == null) {
                System.out.println("错误：管理员不存在");
                return R.error(-1, "您还未注册为市场管理员，请先注册");
            }
            
            // 3. 检查账号状态
            if (manager.getNxMmStatus() == -1) {
                System.out.println("错误：账号待开通，managerId=" + manager.getNxMmId());
                return R.error("您的账号待审核，请联系市场管理员开通");
            } else if (manager.getNxMmStatus() == 0) {
                System.out.println("错误：账号已禁用，managerId=" + manager.getNxMmId());
                return R.error("账号已被禁用，请联系系统管理员");
            } else if (manager.getNxMmStatus() != 1) {
                System.out.println("错误：账号状态异常，managerId=" + manager.getNxMmId() + ", status=" + manager.getNxMmStatus());
                return R.error("账号状态异常，请联系系统管理员");
            }
            
            // 4. 更新最后登录时间
            marketManagerService.updateLastLoginTime(manager.getNxMmId());
            
            // 5. 查询关联的市场信息
            SysCityMarketEntity market = sysCityMarketService.queryObject(manager.getNxMmMarketId());
            manager.setSysCityMarketEntity(market);
            
            // 6. 构建返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("managerId", manager.getNxMmId());
            resultData.put("managerName", manager.getNxMmName());
            resultData.put("phone", manager.getNxMmPhone());
            resultData.put("role", manager.getNxMmRole());
            resultData.put("avatar", manager.getNxMmWxAvatar());
            resultData.put("status", manager.getNxMmStatus());
            resultData.put("marketId", market != null ? market.getSysCityMarketId() : null);
            resultData.put("marketName", market != null ? market.getSysCmMarketName() : null);
            
            System.out.println("登录成功，managerId=" + manager.getNxMmId());
            System.out.println("========== 市场管理员登录完成 ==========");
            
            return R.ok().put("data", resultData);
            
        } catch (Exception e) {
            System.out.println("市场管理员登录异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("登录失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询管理员信息
     * 
     * @param managerId 管理员ID
     * @return 管理员信息
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public R info(@RequestParam("managerId") Integer managerId) {
        
        System.out.println("查询管理员信息，managerId=" + managerId);
        
        try {
            NxMachineMarketManagerEntity manager = marketManagerService.queryObject(managerId);
            
            if (manager == null) {
                return R.error("管理员不存在");
            }
            
            // 查询关联的市场信息
            SysCityMarketEntity market = sysCityMarketService.queryObject(manager.getNxMmMarketId());
            manager.setSysCityMarketEntity(market);
            
            // 构建返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("managerId", manager.getNxMmId());
            resultData.put("managerName", manager.getNxMmName());
            resultData.put("phone", manager.getNxMmPhone());
            resultData.put("role", manager.getNxMmRole());
            resultData.put("avatar", manager.getNxMmWxAvatar());
            resultData.put("status", manager.getNxMmStatus());
            resultData.put("marketId", market != null ? market.getSysCityMarketId() : null);
            resultData.put("marketName", market != null ? market.getSysCmMarketName() : null);
            resultData.put("createTime", manager.getNxMmCreateTime());
            resultData.put("lastLoginTime", manager.getNxMmLastLoginTime());
            
            return R.ok().put("data", resultData);
            
        } catch (Exception e) {
            System.out.println("查询管理员信息异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询市场下的所有管理员
     * 
     * @param marketId 市场ID
     * @return 管理员列表
     */
    @RequestMapping(value = "/listByMarket", method = RequestMethod.GET)
    @ResponseBody
    public R listByMarket(@RequestParam("marketId") Integer marketId) {
        
        System.out.println("查询市场管理员列表，marketId=" + marketId);
        
        try {
            List<NxMachineMarketManagerEntity> managers = marketManagerService.queryByMarketId(marketId);
            
            System.out.println("查询到 " + managers.size() + " 个管理员");
            
            return R.ok().put("list", managers).put("total", managers.size());
            
        } catch (Exception e) {
            System.out.println("查询市场管理员列表异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新管理员信息（不含头像）
     * 
     * @param managerId 管理员ID
     * @param managerName 管理员姓名（可选）
     * @param phone 手机号（可选）
     * @param role 角色（可选，1-普通管理员 2-市场主管）
     * @return 更新结果
     */
    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    @ResponseBody
    public R updateInfo(@RequestParam("managerId") Integer managerId,
                       @RequestParam(value = "managerName", required = false) String managerName,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "role", required = false) Integer role) {
        
        System.out.println("========== 更新管理员信息开始 ==========");
        System.out.println("请求参数 - managerId: " + managerId);
        System.out.println("请求参数 - managerName: " + managerName);
        System.out.println("请求参数 - phone: " + phone);
        System.out.println("请求参数 - role: " + role);
        
        try {
            // 1. 查询管理员是否存在
            NxMachineMarketManagerEntity manager = marketManagerService.queryObject(managerId);
            if (manager == null) {
                System.out.println("错误：管理员不存在");
                return R.error("管理员不存在");
            }
            
            // 2. 如果要修改手机号，检查手机号是否已被其他人使用
            if (phone != null && !phone.trim().isEmpty() && !phone.equals(manager.getNxMmPhone())) {
                NxMachineMarketManagerEntity existPhone = marketManagerService.queryByPhone(phone);
                if (existPhone != null && !existPhone.getNxMmId().equals(managerId)) {
                    System.out.println("错误：手机号已被其他管理员使用，existManagerId=" + existPhone.getNxMmId());
                    return R.error("该手机号已被其他管理员使用");
                }
            }
            
            // 3. 更新信息
            boolean hasUpdate = false;
            
            if (managerName != null && !managerName.trim().isEmpty()) {
                manager.setNxMmName(managerName);
                manager.setNxMmWxNickname(managerName); // 同时更新昵称
                hasUpdate = true;
                System.out.println("更新姓名为：" + managerName);
            }
            
            if (phone != null && !phone.trim().isEmpty()) {
                manager.setNxMmPhone(phone);
                hasUpdate = true;
                System.out.println("更新手机号为：" + phone);
            }
            
            if (role != null && (role == 1 || role == 2)) {
                // 如果要把市场主管降级为普通管理员，需要检查市场是否至少还有其他市场主管
                if (manager.getNxMmRole() == 2 && role == 1) {
                    // 查询该市场的所有管理员
                    List<NxMachineMarketManagerEntity> allManagers = marketManagerService.queryByMarketId(manager.getNxMmMarketId());
                    
                    // 统计市场主管的数量（只统计启用状态的）
                    long supervisorCount = allManagers.stream()
                        .filter(m -> m.getNxMmRole() != null && m.getNxMmRole() == 2 
                                  && m.getNxMmStatus() != null && m.getNxMmStatus() == 1)
                        .count();
                    
                    System.out.println("市场当前启用的市场主管数量: " + supervisorCount);
                    
                    // 如果只有1个市场主管，不允许降级
                    if (supervisorCount <= 1) {
                        System.out.println("错误：市场必须至少保留一个市场主管");
                        return R.error("操作失败：该市场必须至少保留一个市场主管，无法降级为普通管理员");
                    }
                }
                
                manager.setNxMmRole(role);
                hasUpdate = true;
                System.out.println("更新角色为：" + role + "（1-普通管理员 2-市场主管）");
            }
            
            if (!hasUpdate) {
                System.out.println("警告：没有提供任何更新字段");
                return R.error("请提供要更新的信息");
            }
            
            // 4. 保存更新
            marketManagerService.update(manager);
            
            System.out.println("管理员信息更新成功");
            System.out.println("========== 更新管理员信息完成 ==========");
            
            return R.ok("更新成功");
            
        } catch (Exception e) {
            System.out.println("更新管理员信息异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新管理员头像
     * 
     * @param file 新头像文件
     * @param managerId 管理员ID
     * @param session HTTP会话
     * @return 更新结果
     */
    @RequestMapping(value = "/updateAvatar", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateAvatar(@RequestParam("file") MultipartFile file,
                         @RequestParam("managerId") Integer managerId,
                         HttpSession session) {
        
        System.out.println("========== 更新管理员头像开始 ==========");
        System.out.println("请求参数 - managerId: " + managerId);
        
        try {
            // 1. 查询管理员是否存在
            NxMachineMarketManagerEntity manager = marketManagerService.queryObject(managerId);
            if (manager == null) {
                System.out.println("错误：管理员不存在");
                return R.error("管理员不存在");
            }
            
            // 2. 上传新头像
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);
            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            
            System.out.println("新头像上传成功，路径: " + filePath);
            
            // 3. 更新头像路径
            manager.setNxMmWxAvatar(filePath);
            marketManagerService.update(manager);
            
            System.out.println("管理员头像更新成功");
            System.out.println("========== 更新管理员头像完成 ==========");
            
            // 4. 返回新头像路径
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("avatar", filePath);
            
            return R.ok().put("data", resultData);
            
        } catch (Exception e) {
            System.out.println("更新管理员头像异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 审核/开通管理员账号
     * 
     * @param managerId 管理员ID
     * @param approveStatus 审核状态（1-通过并开通 0-拒绝/禁用）
     * @return 操作结果
     */
    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    @ResponseBody
    public R approve(@RequestParam("managerId") Integer managerId,
                    @RequestParam("approveStatus") Integer approveStatus) {
        
        System.out.println("========== 审核管理员账号开始 ==========");
        System.out.println("请求参数 - managerId: " + managerId);
        System.out.println("请求参数 - approveStatus: " + approveStatus);
        
        try {
            // 1. 查询管理员是否存在
            NxMachineMarketManagerEntity manager = marketManagerService.queryObject(managerId);
            if (manager == null) {
                System.out.println("错误：管理员不存在");
                return R.error("管理员不存在");
            }
            
            System.out.println("当前状态: " + manager.getNxMmStatus());
            System.out.println("所属市场ID: " + manager.getNxMmMarketId());
            
            // 2. 如果要禁用管理员，检查市场是否至少还有一个启用的管理员
            if (approveStatus == 0 && manager.getNxMmStatus() == 1) {
                // 查询该市场的所有管理员
                List<NxMachineMarketManagerEntity> allManagers = marketManagerService.queryByMarketId(manager.getNxMmMarketId());
                
                // 统计启用状态的管理员数量
                long enabledCount = allManagers.stream()
                    .filter(m -> m.getNxMmStatus() != null && m.getNxMmStatus() == 1)
                    .count();
                
                System.out.println("市场当前启用的管理员数量: " + enabledCount);
                
                // 如果只有1个启用的管理员，不允许禁用
                if (enabledCount <= 1) {
                    System.out.println("错误：市场必须至少保留一个启用的管理员");
                    return R.error("操作失败：该市场必须至少保留一个启用的管理员，无法禁用");
                }
            }
            
            // 3. 更新状态
            if (approveStatus == 1) {
                // 通过并开通
                manager.setNxMmStatus(1);
                System.out.println("审核通过，账号已开通");
            } else {
                // 拒绝或禁用
                manager.setNxMmStatus(0);
                System.out.println("账号已禁用");
            }
            
            marketManagerService.update(manager);
            
            System.out.println("========== 审核管理员账号完成 ==========");
            
            return R.ok("操作成功")
                .put("managerId", managerId)
                .put("newStatus", manager.getNxMmStatus());
            
        } catch (Exception e) {
            System.out.println("审核管理员账号异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("操作失败：" + e.getMessage());
        }
    }
    
}

