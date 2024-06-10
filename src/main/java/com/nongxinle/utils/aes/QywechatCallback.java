package com.nongxinle.utils.aes;

    public enum QywechatCallback {
        TEST("自定义名称", "公司corpid", "通讯录token", "通讯录key");

        /**
         * 应用名.
         */
        private String name;

        /**
         * 企业ID.
         */
        private String corpid;

        /**
         * 回调url配置的token.
         */
        private String token;

        /**
         * 随机加密串.
         */
        private String encodingAESKey;


        QywechatCallback(final String name, final String corpid, final String token, final String encodingAESKey) {
            this.name = name;
            this.corpid = corpid;
            this.encodingAESKey = encodingAESKey;
            this.token = token;
        }

        public String getCorpid() {
            return corpid;
        }

        public String getName() {
            return name;
        }
        public String getToken() {
            return token;
        }

        public String getEncodingAESKey() {
            return encodingAESKey;
        }

}
