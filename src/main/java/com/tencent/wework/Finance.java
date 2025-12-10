package com.tencent.wework;

/* sdk data structures
typedef struct Slice_t {
    char* buf;
    int len;
} Slice_t;

typedef struct MediaData {
    char* outindexbuf;
    int out_len;
    char* data;    
    int data_len;
    int is_finish;
} MediaData_t;
*/

public class Finance {
    public native static long NewSdk();

    /**
     * Initialize SDK
     * Return value = 0 means API call success
     * 
     * @param [in]  sdk         SDK pointer returned by NewSdk
     * @param [in]  corpid      Enterprise ID, e.g.: wwd08c8exxxx5ab44d
     * @param [in]  secret      Chat content archive Secret
     *                        
     * @return Whether initialization is successful
     *      0   - success
     *      !=0 - failure
     */
    public native static int Init(long sdk, String corpid, String secret);

    /**
     * Get chat record data
     * Return value = 0 means API call success
     * 
     * @param [in]  sdk         SDK pointer returned by NewSdk
     * @param [in]  seq         Start seq for pulling messages
     * @param [in]  limit       Number of messages to pull in one call, max 1000
     * @param [in]  proxy       Proxy request URL
     * @param [in]  passwd      Proxy account password
     * @param [out] chatDatas   Returned chat data

     * @return Whether call is successful
     *      0   - success
     *      !=0 - failure    
     */        
    public native static int GetChatData(long sdk, long seq, long limit, String proxy, String passwd, long timeout, long chatData);

    /**
     * Get media message data
     * Return value = 0 means API call success
     * 
     * @param [in]  sdk         SDK pointer returned by NewSdk
     * @param [in]  sdkFileid   SDK file ID from media messages
     * @param [in]  proxy       Proxy request URL
     * @param [in]  passwd      Proxy account password
     * @param [in]  indexbuf    Media message slice index
     * @param [out] media_data  Returned media data
     
     * @return Whether call is successful
     *      0   - success
     *      !=0 - failure
     */
    public native static int GetMediaData(long sdk, String indexbuf, String sdkField, String proxy, String passwd, long timeout, long mediaData);

    /**
     * @brief Decrypt data
     * @param [in]  encrypt_key, encrypt_key from getchatdata
     * @param [in]  encrypt_msg, content from getchatdata
     * @param [out] msg, decrypted message
     * @return Whether call is successful
     *      0   - success
     *      !=0 - failure
     */
    public native static int DecryptData(long sdk, String encrypt_key, String encrypt_msg, long msg);
    
    public native static void DestroySdk(long sdk);
    public native static long NewSlice();
    
    /**
     * @brief Free slice, used with NewSlice
     * @return 
     */
    public native static void FreeSlice(long slice);

    /**
     * @brief Get slice content
     * @return content
     */
    public native static String GetContentFromSlice(long slice);

    /**
     * @brief Get slice content length
     * @return length
     */
    public native static int GetSliceLen(long slice);
    
    public native static long NewMediaData();
    public native static void FreeMediaData(long mediaData);

    /**
     * @brief Get mediadata outindex
     * @return outindex
     */
    public native static String GetOutIndexBuf(long mediaData);
    
    /**
     * @brief Get mediadata data content
     * @return data
     */
    public native static byte[] GetData(long mediaData);
    
    public native static int GetIndexLen(long mediaData);
    public native static int GetDataLen(long mediaData);

    /**
     * @brief Check if mediadata is finished
     * @return 1 finished, 0 not finished
     */
    public native static int IsMediaDataFinish(long mediaData);

    static {
        System.loadLibrary("WeWorkFinanceSdk_Java");
    }
}