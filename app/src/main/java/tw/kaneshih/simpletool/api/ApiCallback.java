package tw.kaneshih.simpletool.api;

public interface ApiCallback<T> {
    void onResult(ApiResult<T> result);
}