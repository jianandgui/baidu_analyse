package cn.edu.swpu.cins.event.analyse.platform.model.view;

public class ResultData<T> {

    /*请求数据*/
    private T data;

    /*返回结果*/
    private boolean ifSuccess;
    private String msg;




    public ResultData(boolean ifSuccess, T data) {
        this.ifSuccess = ifSuccess;
        this.data = data;
    }

    public ResultData(boolean ifSuccess, String msg) {
        this.ifSuccess = ifSuccess;
        this.msg = msg;
    }

    public ResultData(T data) {
        this.data = data;
        this.ifSuccess = true;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isIfSuccess() {
        return ifSuccess;
    }

    public void setIfSuccess(boolean ifSuccess) {
        this.ifSuccess = ifSuccess;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
