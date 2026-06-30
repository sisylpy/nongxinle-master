-- 社区商品：介绍视频（存相对路径，与图片同一套 EXTERNAL_IMAGE_DIR）
ALTER TABLE nx_community_goods
    ADD COLUMN nx_cg_goods_intro_video VARCHAR(1024) NULL COMMENT '介绍视频相对路径'
        AFTER nx_cg_nx_goods_top_file_path;
