package tw.kaneshih.simpletool.api;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import tw.kaneshih.simpletool.utility.HttpUtil.Request;
import tw.kaneshih.simpletool.utility.HttpUtil.Response;
import tw.kaneshih.simpletool.utility.Validator;

public abstract class ApiTask<T> {
    private static final String TAG = "ApiTask";

    // -- system level error --
    private static final String SYS_ERROR_PREFIX = "SYS_ERR:";
    public static final String ERROR_NETWORK = SYS_ERROR_PREFIX + "NETWORK";
    public static final String ERROR_INVALID_PARAM = SYS_ERROR_PREFIX + "INVALID_PARAM";
    public static final String ERROR_RESPONSE_FORMAT = SYS_ERROR_PREFIX + "RESPONSE_FORMAT";

    // -- member fields --
    private ApiCallback<T> callback;
    private boolean isForceUpdate = false;

    public interface Status {
        int NOT_STARTED = 0;
        int RUNNING_BACKGROUND = 1;
        int RUNNING_CALLBACK = 2;
        int FINISHED = 3;
    }

    @IntDef({Status.NOT_STARTED, Status.RUNNING_BACKGROUND, Status.RUNNING_CALLBACK, Status.FINISHED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskStatus {
    }

    @TaskStatus
    private volatile int status;

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(
            getCorePoolSize(),
            new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, "ApiTaskBg");
                }
            });
    private static final InternalHandler HANDLER = new InternalHandler();

    private static class InternalHandler extends Handler {
        private InternalHandler() {
            super(Looper.getMainLooper());
        }
    }

    private volatile boolean isCancelled = false;

    private static int getCorePoolSize() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        if (coreCount <= 2) {
            Logcat.d(TAG, "EXECUTOR ThreadPool size: 5");
            return 5;
        } else {
            Logcat.d(TAG, "EXECUTOR ThreadPool size: 10");
            return 10;
        }
    }

    public static boolean isSystemLevelError(String errorCode) {
        return !Validator.isEmpty(errorCode) && errorCode.startsWith(SYS_ERROR_PREFIX);
    }

    private ApiTask() {
    }

    protected ApiTask(ApiCallback<T> callback) {
        setStatus(Status.NOT_STARTED);
        if (!Validator.isNull(callback)) {
            this.callback = callback;
        }
    }

    @TaskStatus
    public int getStatus() {
        return status;
    }

    private void setStatus(@TaskStatus int status) {
        this.status = status;
    }

    public void cancel() {
        isCancelled = true;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Must call before execute() if you don't want to get cache data
     *
     * @return current instance
     */
    public ApiTask<T> setForceUpdate() {
        isForceUpdate = true;
        return this;
    }

    private ApiCallback<T> getCallback() {
        return callback;
    }

    // -- template method (input parameters are ensured to be non-null) --
    protected abstract T getDataObjectFromCache();

    protected abstract Request prepareRequest();

    protected abstract T parseSuccessData(String data);

    protected abstract void saveDataObjectToCache(T dataObject);

    // -- functions for core --
    private static String getNetworkErrorCode(Response response) {
        // noinspection ThrowableResultOfMethodCallIgnored
        if (response.getThrowable() != null) {
            return ERROR_NETWORK;
        }
        if (!response.isOk()) {
            return ERROR_NETWORK;
        }
        if (Validator.isNull(response.getBody())) {
            return ERROR_NETWORK;
        }
        return null;
    }

    private static ApiResult setIfNetworkError(Response response, ApiResult apiResult) {
        String errorCode = getNetworkErrorCode(response);
        if (errorCode != null) {
            apiResult.result = false;
            apiResult.errCode = errorCode;
        }
        return apiResult;
    }

    // -- core --

    public final void execute() {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                setStatus(Status.RUNNING_BACKGROUND);
                Logcat.d(TAG, "RUNNING_BACKGROUND");
                final ApiResult<T> result = doInBackground();
                if (isCancelled) {
                    setStatus(Status.FINISHED);
                    Logcat.d(TAG, "FINISHED (by cancel)");
                } else {
                    boolean r = HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            setStatus(Status.RUNNING_CALLBACK);
                            Logcat.d(TAG, "RUNNING_CALLBACK");
                            onPostExecute(result);
                            setStatus(Status.FINISHED);
                            Logcat.d(TAG, "FINISHED");
                        }
                    });
                    if (!r) {
                        Logcat.e(TAG, "post msg failed!");
                    }
                }
            }
        });
    }

    private ApiResult<T> doInBackground() {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.result = true;

        if (!isForceUpdate) {
            T dataObject = getDataObjectFromCache();
            if (dataObject != null) {
                apiResult.result = true;
                apiResult.data = dataObject;
                return apiResult;
            }
        }

        Request request = prepareRequest();
        if (request == null) {
            apiResult.result = false;
            apiResult.errCode = ERROR_INVALID_PARAM;
            return apiResult;
        }

        Response response = request.execute();
        if (response == null) {
            apiResult.result = false;
            apiResult.errCode = ERROR_INVALID_PARAM;
            return apiResult;
        }
        String body = response.getBody();

        setIfNetworkError(response, apiResult);
        if (!apiResult.result) {
            return apiResult;
        }

        T dataObject = parseSuccessData(body);
        if (dataObject == null) {
            apiResult.result = false;
            apiResult.errCode = ERROR_RESPONSE_FORMAT;
            return apiResult;
        }
        saveDataObjectToCache(dataObject);

        apiResult.result = true;
        apiResult.data = dataObject;
        return apiResult;
    }

    private void onPostExecute(ApiResult<T> result) {
        if (!Validator.isNull(result)) {
            if (!result.result) {
                Logcat.e(TAG, getClass().getSimpleName() + " [" + result.errCode + "] " + result.errMsg);
            }
            ApiCallback<T> callback = getCallback();
            if (callback == null) {
                Logcat.d(TAG, "callback is recycled, quit");
            } else {
                Logcat.d(TAG, "notify callback!! " + getClass().getSimpleName());
                callback.onResult(result);
            }
        }
    }
}
