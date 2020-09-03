package cn.hlh.rpc.enumeration;

public enum ResponseCode implements CommonCode {
    SUCCESS(200,"方法调用成功"),
    HEART_BEAT(300,"心跳包"),
    FAIL(500,"方法调用失败"),
    METHOD_NOT_FOUND(501,"未找到指定方法"),
    CLASS_NOT_FOUND(502,"未找到指定服务实现类");

    private int responseCode;
    private String responseMsg;

    private ResponseCode(int responseCode, String responseMsg){
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
    }
    @Override
    public int getCode() {
        return responseCode;
    }

    @Override
    public String getMsg() {
        return responseMsg;
    }

    @Override
    public CommonCode setMsg(String responseMsg) {
        this.responseMsg = responseMsg;
        return this;
    }
}
