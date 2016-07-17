package tw.kaneshih.simpletool.api;

public class ApiResult<T> {
    boolean result = false;
    T data;
    String errCode;
    String errMsg;

    public boolean isSuccess() {
        return result;
    }

    public boolean isSystemLevelError() {
        return ApiTask.isSystemLevelError(errCode);
    }

    /**
     * @return if {@link #isSuccess()} returns true, then this is non-null (guaranteed by ApiTask core)
     */
    public T getData() {
        return data;
    }

    /**
     * @return if {@link ApiTask#isSystemLevelError(String)} returns true, then it's not from server
     */
    public String getErrorCode() {
        return errCode;
    }

    public String getErrorMsg() {
        return errMsg;
    }

    // @formatter:off
    @Override
    public String toString() {
        return "ApiResult{" +
                "result=" + result +
                ", data=" + data +
                ", errCode='" + errCode + '\'' +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
    // @formatter:on
}