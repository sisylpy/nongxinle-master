package com.nongxinle.controller;

/**
 * @author lpy
 * @date 05-23 14:26
 */

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityCardEntity;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.service.NxCommunityGoodsService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityCardEntity;
import com.nongxinle.service.NxCommunityCardService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxcommunitycard")
public class NxCommunityCardController {
    @Autowired
    private NxCommunityCardService nxCommunityCardService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;


    @RequestMapping(value = "/comGetCardDetail/{id}")
    @ResponseBody
    public R comGetCardDetail(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        NxCommunityCardEntity communityCardEntity = nxCommunityCardService.queryObject(id);

//		NxCommunityCardEntity communityCardEntity = nxCommunityCardService.queryCardDetail(map);
        System.out.println("edeetktkkt" + communityCardEntity);
        return R.ok().put("data", communityCardEntity);
    }

    @RequestMapping(value = "/comSaveOneCard", method = RequestMethod.POST)
    @ResponseBody
    public R comSaveOneCard(@RequestBody NxCommunityCardEntity card) {
        System.out.println("safnfnff" + card);
        card.setNxCcStatus(0);
        card.setNxCcUserCount(0);
        nxCommunityCardService.save(card);
        return R.ok();
    }


    @RequestMapping(value = "/delComCard", method = RequestMethod.POST)
    @ResponseBody
    public R delComCard(Integer id, HttpSession session) {

        NxCommunityCardEntity communityCardEntity = nxCommunityCardService.queryObject(id);
        Map<String, Object> map = new HashMap<>();
        map.put("cardId", id);
        List<NxCommunityGoodsEntity> nxCommunityGoodsEntities = nxCommunityGoodsService.queryComGoodsByParams(map);
        if (nxCommunityGoodsEntities.size() > 0) {
            return R.error(-1, "有商品不能删除");
        } else {
            String oldPath = communityCardEntity.getNxCcFilePath();
            if (oldPath != null && !oldPath.trim().isEmpty()) {
                String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
                File file1 = new File(oldAbsolutePath);
                if (file1.exists()) {
                    file1.delete();
                }
            }

            nxCommunityCardService.delete(id);
            return R.ok();
        }
    }


    @RequestMapping(value = "/comGetCardList", method = RequestMethod.POST)
    @ResponseBody
    public R comGetCardList(Integer commId, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
//		map.put("status", 0);
        System.out.println("caididiidiiddfddaf" + map);
        List<NxCommunityCardEntity> communityCardEntities = nxCommunityCardService.queryCardListByParams(map);
        return R.ok().put("data", communityCardEntities);
    }

    @RequestMapping(value = "/comUpdateCard", method = RequestMethod.POST)
    @ResponseBody
    public R comUpdateCard(@RequestBody NxCommunityCardEntity goodsCard) {

        nxCommunityCardService.update(goodsCard);

        return R.ok().put("data", goodsCard);
    }


    @RequestMapping(value = "/updateCardWithFile", method = RequestMethod.POST)
    @ResponseBody
    public R updateCardWithFile(@RequestParam("file") MultipartFile file,
                                @RequestParam("id") Integer id,
                                HttpSession session) {
        //1,上传图片
        String newUploadName = "goodsImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;

        System.out.println("nefifiififiififpapapa" + filePath);


        NxCommunityCardEntity communityCardEntity = nxCommunityCardService.queryObject(id);
        String oldPath = communityCardEntity.getNxCcFilePath();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File file1 = new File(oldAbsolutePath);
            if (file1.exists()) {
                file1.delete();
            }
        }


        communityCardEntity.setNxCcFilePath(filePath);

        nxCommunityCardService.update(communityCardEntity);

        return R.ok();
    }


}
