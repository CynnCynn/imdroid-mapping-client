package com.imdroid.pojo.bo;

public class ResponseResult {
    /**
     * if the response is okay or not
     */
    private boolean success;
    /**
     * The error message if the respone is fail
     */
    private String errorMsg;
    /**
     * The okay message if the response is successful.
     */
    private String okMsg;
    /**
     * The return object
     */
    private Object obj;

    /**
     * Default constructor
     *
     * @param success  if the response is okay or not
     * @param errorMsg The error message if the respone is fail
     * @param okMsg    The okay message if the response is successful.
     * @param obj      The okay message if the response is successful.
     */
    public ResponseResult(boolean success, String errorMsg, String okMsg, Object obj) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.okMsg = okMsg;
        this.obj = obj;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the errorMsg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * @param errorMsg the errorMsg to set
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * @return the okMsg
     */
    public String getOkMsg() {
        return okMsg;
    }

    /**
     * @param okMsg the okMsg to set
     */
    public void setOkMsg(String okMsg) {
        this.okMsg = okMsg;
    }

    /**
     * @return the obj
     */
    public Object getObj() {
        return obj;
    }

    /**
     * @param obj the obj to set
     */
    public void setObj(Object obj) {
        this.obj = obj;
    }

}
