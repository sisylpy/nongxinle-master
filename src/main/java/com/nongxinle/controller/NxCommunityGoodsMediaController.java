package com.nongxinle.controller;

/**
 * 社区商品媒体资源Controller
 *
 * @author lpy
 * @date 2026-04-12
 */

import com.nongxinle.entity.NxCommunityGoodsMediaEntity;
import com.nongxinle.service.NxCommunityGoodsMediaService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.R;
import com.nongxinle.utils.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("api/nxcommunitygoodsmedia")
public class NxCommunityGoodsMediaController {

    @Autowired
    private NxCommunityGoodsMediaService nxCommunityGoodsMediaService;

    /**
     * 获取商品的所有图片
     */
    @RequestMapping(value = "/getGoodsMediaList/{goodsId}")
    @ResponseBody
    public R getGoodsMediaList(@PathVariable Integer goodsId) {
        List<NxCommunityGoodsMediaEntity> mediaList = nxCommunityGoodsMediaService.getMediaListByGoodsId(goodsId);
        return R.ok().put("data", mediaList);
    }

    /**
     * 获取商品主图
     */
    @RequestMapping(value = "/getGoodsPrimaryMedia/{goodsId}")
    @ResponseBody
    public R getGoodsPrimaryMedia(@PathVariable Integer goodsId) {
        NxCommunityGoodsMediaEntity primaryMedia = nxCommunityGoodsMediaService.getPrimaryMedia(goodsId);
        return R.ok().put("data", primaryMedia);
    }

    /**
     * 删除单张图片
     */
    @RequestMapping(value = "/delMedia/{mediaId}")
    @ResponseBody
    public R delMedia(@PathVariable Integer mediaId) {
        nxCommunityGoodsMediaService.removeMediaById(mediaId);
        return R.ok();
    }

    /**
     * 上传单张商品图片
     */
    @RequestMapping(value = "/uploadGoodsMedia")
    @ResponseBody
    public R uploadGoodsMedia(@RequestParam("file") MultipartFile file,
                              @RequestParam("goodsId") Integer goodsId,
                              @RequestParam("mediaType") Integer mediaType,
                              @RequestParam("isPrimary") Integer isPrimary,
                              @RequestParam(value = "sort", required = false, defaultValue = "0") Integer sort,
                              HttpSession session) {
        try {
            // 1. 查询现有图片数量，用于排序
            List<NxCommunityGoodsMediaEntity> existingList = nxCommunityGoodsMediaService.getMediaListByGoodsId(goodsId);
            int maxSort = 0;
            for (NxCommunityGoodsMediaEntity existing : existingList) {
                if (existing.getNxCommGoodsMediaSort() != null && existing.getNxCommGoodsMediaSort() > maxSort) {
                    maxSort = existing.getNxCommGoodsMediaSort();
                }
            }

            // 2. 如果是主图，先取消旧的主图
            if (isPrimary != null && isPrimary == 1) {
                nxCommunityGoodsMediaService.clearPrimaryByGoodsId(goodsId);
                sort = 0; // 主图排序固定为0
            } else {
                sort = maxSort + 1; // 新图片排在最后
            }

            // 3. 上传图片（复用 goodsImage 目录，与现有社区商品图片一致）
            String subDir = "goodsImage";
            UploadFile.upload(session, subDir, file);
            // 返回相对路径给前端
            String filePath = subDir + "/" + file.getOriginalFilename();

            // 4. 构建媒体实体
            NxCommunityGoodsMediaEntity media = new NxCommunityGoodsMediaEntity();
            media.setNxCommGoodsId(goodsId.longValue());
            media.setNxCommGoodsMediaType(mediaType);
            media.setNxCommGoodsMediaUrl(filePath);
            media.setNxCommGoodsMediaIsPrimary(isPrimary);
            media.setNxCommGoodsMediaSort(sort);
            media.setNxCommGoodsMediaStatus(1);
            media.setNxCommGoodsMediaFileSize(file.getSize());
            media.setNxCommGoodsMediaFileName(file.getOriginalFilename());

            // 5. 保存到数据库
            nxCommunityGoodsMediaService.saveMedia(media);

            return R.ok().put("data", media);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量保存商品图片（替换模式：先删后插）
     */
    @RequestMapping(value = "/saveGoodsMediaList", method = RequestMethod.POST)
    @ResponseBody
    public R saveGoodsMediaList(@RequestBody List<NxCommunityGoodsMediaEntity> mediaList) {
        try {
            nxCommunityGoodsMediaService.saveMediaBatch(mediaList);
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("保存失败: " + e.getMessage());
        }
    }


}
