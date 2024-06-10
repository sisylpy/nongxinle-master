package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dao.GbDepartmentDao;
import com.nongxinle.dao.SysUserDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerDao;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeAppSupplier;


@Service("gbDistributerService")
public class GbDistributerServiceImpl implements GbDistributerService {
    @Autowired
    private GbDistributerDao gbDistributerDao;
    @Autowired
    private GbDistributerUserService gbDistributerUserService;

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerModuleService gbDistributerModuleService;
    @Autowired
    private QyGbDisCorpUserService gbDisCorpUserService;
    @Autowired
    private QyGbDisCorpService qyGbDisCorpService;
    @Autowired
    private NxDistributerGbDistributerService nxDistributerGbDistributerService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService dgfService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;

    @Override
    public GbDistributerEntity queryObject(Integer gbDistributerId) {
        return gbDistributerDao.queryObject(gbDistributerId);
    }

    @Override
    public List<GbDistributerEntity> queryList(Map<String, Object> map) {
        return gbDistributerDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return gbDistributerDao.queryTotal(map);
    }

    @Override
    public void save(GbDistributerEntity gbDistributer) {
        gbDistributerDao.save(gbDistributer);
    }

    @Override
    public void update(GbDistributerEntity gbDistributer) {
        gbDistributerDao.update(gbDistributer);
    }

    @Override
    public void delete(Integer gbDistributerId) {
        gbDistributerDao.delete(gbDistributerId);
    }

    @Override
    public void deleteBatch(Integer[] gbDistributerIds) {
        gbDistributerDao.deleteBatch(gbDistributerIds);
    }

    @Override
    public Integer saveNewDistributerGb(GbDistributerEntity gbDistributerEntity) {


        //1.保存distributer
        gbDistributerEntity.setGbDistributerSettleDate(formatWhatDay(0));
        gbDistributerEntity.setGbDistributerSettleFullTime(formatFullTime());
        gbDistributerEntity.setGbDistributerSettleMonth(formatWhatMonth(0));
        gbDistributerEntity.setGbDistributerSettleWeek(getWeekOfYear(0).toString());
        gbDistributerEntity.setGbDistributerSettleYear(formatWhatYear(0));
        gbDistributerEntity.setGbDistributerManager("09:00");
        gbDistributerEntity.setGbDistributerSettleTimes("0");
        gbDistributerDao.save(gbDistributerEntity);

        //3，保存Dis用户
        Integer distributerId = gbDistributerEntity.getGbDistributerId();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerEntity.getGbDistributerUserEntity();
        gbDistributerUserEntity.setGbDiuDistributerId(distributerId);
        gbDistributerUserEntity.setGbDiuPrintDeviceId("-1");
        gbDistributerUserEntity.setGbDiuPrintBillDeviceId("-1");
        gbDistributerUserEntity.setGbDiuLoginTimes(0);
        gbDistributerUserService.save(gbDistributerUserEntity);

//        //4 保存Sys用户
//        SysUserEntity sysUser = new SysUserEntity();
//        List<Long> ids = new ArrayList<>();
//        long a = 1L; //
//        ids.add(a);
//        sysUser.setUsername(gbDistributerEntity.getGbDistributerUserEntity().getGbDiuWxPhone());
//        sysUser.setUserDisUserId(gbDistributerUserEntity.getGbDistributerUserId());
//        sysUser.setPassword("1");
//        sysUser.setUserDisId(distributerId);
//        sysUser.setStatus(1);
//        sysUser.setRoleIdList(ids);
//        sysUserService.save(sysUser);

        //模块
        GbDistributerModuleEntity gbDistributerModuleEntity = gbDistributerEntity.getGbDistributerModuleEntity();
        System.out.println("stocnamemememgbDistributerModuleEntitye" + gbDistributerModuleEntity);
        gbDistributerModuleEntity.setGbDmDistributerId(gbDistributerEntity.getGbDistributerId());
        gbDistributerModuleService.save(gbDistributerModuleEntity);

        //保存集采模块
        Integer appSupplierNumber = gbDistributerModuleEntity.getGbDmAppSupplierNumber();
        if (appSupplierNumber == 0) {
            saveDepartment(gbDistributerEntity, "配送商管理部门", getGbDepartmentTypeAppSupplier());
            saveGbDisFatherGoodsForApp(gbDistributerEntity);

        }

        //保存集采模块
        Integer stockNumber = gbDistributerModuleEntity.getGbDmStockNumber();
        if (stockNumber == 0) {
            saveDepartment(gbDistributerEntity, gbDistributerEntity.getGbDistributerName() + "的库房", getGbDepartmentTypeKufang());
        }
        //保存集采模块
        Integer purchaseNumber = gbDistributerModuleEntity.getGbDmPurchaseNumber();
        if (purchaseNumber == 0) {
            saveDepartment(gbDistributerEntity, gbDistributerEntity.getGbDistributerName() + "集采", getGbDepartmentTypeJicai());
        }
        //保存集采模块
        Integer centralKitchenNumber = gbDistributerModuleEntity.getGbDmCentralKitchenNumber();
        if (centralKitchenNumber == 0) {
            saveDepartment(gbDistributerEntity, gbDistributerEntity.getGbDistributerName() + "中央厨房", getGbDepartmentTypeKitchen());
        }

        return gbDistributerUserEntity.getGbDistributerUserId();
    }


    private void saveGbDisFatherGoodsForApp(GbDistributerEntity gbDistributerEntity) {
        GbDistributerFatherGoodsEntity greatGrand = new GbDistributerFatherGoodsEntity();
        String greatGrandName1 = "配送商品";
        greatGrand.setGbDfgFatherGoodsName(greatGrandName1);
        greatGrand.setGbDfgDistributerId(gbDistributerEntity.getGbDistributerId());
        greatGrand.setGbDfgFatherGoodsLevel(0);
        greatGrand.setGbDfgFathersFatherId(0);
        greatGrand.setGbDfgFatherGoodsColor("#187e6e");
        greatGrand.setGbDfgNxGoodsId(0);
        greatGrand.setGbDfgFatherGoodsSort(0);
        dgfService.save(greatGrand);

    }

    private void saveDepartment(GbDistributerEntity gbDistributerEntity, String modulName, Integer type) {
        //保存集采部门
        GbDepartmentEntity departmentEntity = new GbDepartmentEntity();
        departmentEntity.setGbDepartmentDisId(gbDistributerEntity.getGbDistributerId());
        departmentEntity.setGbDepartmentFatherId(0);
        departmentEntity.setGbDepartmentType(type);
        gbDistributerEntity.setGbDistributerSettleDate(formatWhatDay(0));
        departmentEntity.setGbDepartmentSettleFullTime(formatFullTime());
        departmentEntity.setGbDepartmentSettleDate(formatWhatDay(0));
        departmentEntity.setGbDepartmentSettleMonth(formatWhatMonth(0));
        departmentEntity.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
        departmentEntity.setGbDepartmentSettleYear(formatWhatYear(0));
        departmentEntity.setGbDepartmentSettleTimes("0");
        departmentEntity.setGbDepartmentSubAmount(0);
        departmentEntity.setGbDepartmentIsGroupDep(1);
        departmentEntity.setGbDepartmentAttrName(modulName);
        departmentEntity.setGbDepartmentName(modulName);
        departmentEntity.setGbDepartmentPrintSet(0);
        gbDepartmentService.save(departmentEntity);

        if (gbDistributerEntity.getInviteNxDisId() != null) {
            NxDistributerGbDistributerEntity sisy = new NxDistributerGbDistributerEntity();
            sisy.setNxDgdGbDepId(departmentEntity.getGbDepartmentId());
            sisy.setNxDgdGbDistributerId(gbDistributerEntity.getGbDistributerId());
            sisy.setNxDgdNxDistributerId(gbDistributerEntity.getInviteNxDisId());
            sisy.setNxDgdGbGoodsPrice(1);
            sisy.setNxDgdStatus(0);
            sisy.setNxDgdGbPayMethod(1);
            nxDistributerGbDistributerService.save(sisy);
        }
    }

    @Override
    public GbDistributerEntity queryDistributerInfo(Integer gbDepartmentDisId) {

        return gbDistributerDao.queryDisInfo(gbDepartmentDisId);
    }

    @Override
    public List<GbDistributerEntity> queryListAll() {

        return gbDistributerDao.queryListAll();
    }

    @Override
    public void kaitongGbDis(Integer id) {
        gbDistributerDao.kaitongGbDis(id);
    }

    @Override
    public Integer saveNewDistributerGbWork(GbDistributerEntity gbDistributerEntity, JSONObject jsonObject) {
        //1.保存distributer
        gbDistributerEntity.setGbDistributerSettleDate(formatWhatDay(0));
        gbDistributerEntity.setGbDistributerSettleFullTime(formatFullTime());
        gbDistributerEntity.setGbDistributerSettleMonth(formatWhatMonth(0));
        gbDistributerEntity.setGbDistributerSettleWeek(getWeekOfYear(0).toString());
        gbDistributerEntity.setGbDistributerSettleYear(formatWhatYear(0));
        gbDistributerEntity.setGbDistributerManager("09:00");
        gbDistributerEntity.setGbDistributerSettleTimes("0");
        gbDistributerDao.save(gbDistributerEntity);

        //3，保存Dis用户
        Integer distributerId = gbDistributerEntity.getGbDistributerId();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerEntity.getGbDistributerUserEntity();
        gbDistributerUserEntity.setGbDiuDistributerId(distributerId);
        gbDistributerUserEntity.setGbDiuPrintDeviceId("-1");
        gbDistributerUserEntity.setGbDiuPrintBillDeviceId("-1");
        gbDistributerUserEntity.setGbDiuLoginTimes(0);
        gbDistributerUserService.save(gbDistributerUserEntity);

//        //4 保存Sys用户
//        SysUserEntity sysUser = new SysUserEntity();
//        List<Long> ids = new ArrayList<>();
//        long a = 1L; //
//        ids.add(a);
//        sysUser.setUsername(gbDistributerEntity.getGbDistributerUserEntity().getGbDiuWxPhone());
//        sysUser.setUserDisUserId(gbDistributerUserEntity.getGbDistributerUserId());
//        sysUser.setPassword("1");
//        sysUser.setUserDisId(distributerId);
//        sysUser.setStatus(1);
//        sysUser.setRoleIdList(ids);
//        sysUserService.save(sysUser);

        //模块
        GbDistributerModuleEntity gbDistributerModuleEntity = gbDistributerEntity.getGbDistributerModuleEntity();
        System.out.println("stocnamemememgbDistributerModuleEntitye" + gbDistributerModuleEntity);
        gbDistributerModuleEntity.setGbDmDistributerId(gbDistributerEntity.getGbDistributerId());
        gbDistributerModuleService.save(gbDistributerModuleEntity);

        //保存集采模块
        Integer appSupplierNumber = gbDistributerModuleEntity.getGbDmAppSupplierNumber();
        if (appSupplierNumber == 0) {
            saveDepartment(gbDistributerEntity, "配送商管理部门", getGbDepartmentTypeAppSupplier());
        }

        //保存集采模块
        Integer stockNumber = gbDistributerModuleEntity.getGbDmStockNumber();
        if (stockNumber == 0) {
            saveDepartment(gbDistributerEntity, gbDistributerEntity.getGbDistributerName() + "的库房", getGbDepartmentTypeKufang());
        }
        //保存集采模块
        Integer purchaseNumber = gbDistributerModuleEntity.getGbDmPurchaseNumber();
        if (purchaseNumber == 0) {
            saveDepartment(gbDistributerEntity, gbDistributerEntity.getGbDistributerName() + "集采", getGbDepartmentTypeJicai());
        }
        //保存集采模块
        Integer centralKitchenNumber = gbDistributerModuleEntity.getGbDmCentralKitchenNumber();
        if (centralKitchenNumber == 0) {
            saveDepartment(gbDistributerEntity, gbDistributerEntity.getGbDistributerName() + "中央厨房", getGbDepartmentTypeKitchen());
        }

        //
        // 3，如果没有注册过
        System.out.println("jsonObjectjsonObjectwwwwwSSSS=====" + jsonObject);
        String openUserId = jsonObject.getString("open_userid");
        String corpId = jsonObject.getString("corpid");
        String sessionKey = jsonObject.getString("session_key");
        QyGbDisCorpEntity qyNxDisCorpEntity = qyGbDisCorpService.queryQyCropByCropId(corpId);
        Integer qyNxDisQyCorpId = qyNxDisCorpEntity.getQyGbDisCorpId();
        QyGbDisCorpUserEntity userEntity = new QyGbDisCorpUserEntity();
        userEntity.setQyGbDisCorpOpenUserId(openUserId);
        userEntity.setQyGbDisCorpQyCorpId(qyNxDisQyCorpId);
        userEntity.setQyGbDisCorpSessionKey(sessionKey);
        userEntity.setQyGbDistributerId(gbDistributerEntity.getGbDistributerId());
        userEntity.setQyGbDisCorpUserJoinDate(formatWhatDay(0));

        gbDisCorpUserService.save(userEntity);

        gbDistributerUserEntity.setGbDiuQyCorpUserId(userEntity.getQyGbDisCorpUserId());

        gbDistributerUserService.update(gbDistributerUserEntity);


        return gbDistributerUserEntity.getGbDistributerUserId();

    }

    @Override
    public List<GbDistributerEntity> queryGbDisCustomerBySellerOpenId(String openId) {

        return gbDistributerDao.queryGbDisCustomerBySellerOpenId(openId);
    }

    @Override
    public Integer saveNewDistributerGbForPeisong(GbDistributerUserEntity distributerUserEntity, Integer disId) {
        GbDistributerEntity gbDistributerEntity = new GbDistributerEntity();
        //1.保存distributer
        gbDistributerEntity.setGbDistributerName(distributerUserEntity.getGbDiuWxNickName());
        gbDistributerEntity.setGbDistributerSettleDate(formatWhatDay(0));
        gbDistributerEntity.setGbDistributerSettleFullTime(formatFullTime());
        gbDistributerEntity.setGbDistributerSettleMonth(formatWhatMonth(0));
        gbDistributerEntity.setGbDistributerSettleWeek(getWeekOfYear(0).toString());
        gbDistributerEntity.setGbDistributerSettleYear(formatWhatYear(0));
        gbDistributerEntity.setGbDistributerManager("09:00");
        gbDistributerEntity.setGbDistributerSettleTimes("0");
        gbDistributerEntity.setInviteNxDisId(disId);
        gbDistributerEntity.setGbDistributerBusinessType(23);
        gbDistributerDao.save(gbDistributerEntity);

        //3，保存Dis用户
        Integer distributerId = gbDistributerEntity.getGbDistributerId();
        distributerUserEntity.setGbDiuDistributerId(distributerId);
        distributerUserEntity.setGbDiuPrintDeviceId("-1");
        distributerUserEntity.setGbDiuPrintBillDeviceId("-1");
        distributerUserEntity.setGbDiuLoginTimes(0);
        gbDistributerUserService.save(distributerUserEntity);

        GbDistributerModuleEntity gbDistributerModuleEntity = new GbDistributerModuleEntity();
        gbDistributerModuleEntity.setGbDmAppSupplierNumber(0);
        gbDistributerModuleEntity.setGbDmDirectSalesNumber(0);
        gbDistributerModuleEntity.setGbDmPurchaseNumber(-1);
        gbDistributerModuleEntity.setGbDmStockNumber(-1);
        gbDistributerModuleEntity.setGbDmCentralKitchenNumber(-1);
        gbDistributerModuleEntity.setGbDmFranchiseeNumber(-1);
        gbDistributerModuleEntity.setGbDmFixedSupplierNumber(-1);
        gbDistributerModuleEntity.setGbDmDistributerId(gbDistributerEntity.getGbDistributerId());
        gbDistributerModuleService.save(gbDistributerModuleEntity);

        saveDepartment(gbDistributerEntity, "配送商管理部门", getGbDepartmentTypeAppSupplier());
        saveGbDisFatherGoodsForApp(gbDistributerEntity);


        return distributerUserEntity.getGbDistributerUserId();

    }


}
