package cn.hlh.rpc.enumeration;

/**
 * 标识数据包类型，用于编码和反编码
 */
public enum PackageType implements CommonCode{
    REQUESR_PACK(0,"调用请求"),
    RESPONSE_PACK(1,"调用响应");

    private int packageTypeCode;
    private String packageTypeMsg;
    private PackageType(int packageTypeCode, String packageTypeMsg){
        this.packageTypeCode = packageTypeCode;
        this.packageTypeMsg = packageTypeMsg;
    }
    @Override
    public int getCode() {
        return packageTypeCode;
    }

    @Override
    public String getMsg() {
        return packageTypeMsg;
    }

    @Override
    public CommonCode setMsg(String packageTypeMsg) {
        this.packageTypeMsg = packageTypeMsg;
        return this;
    }
}
