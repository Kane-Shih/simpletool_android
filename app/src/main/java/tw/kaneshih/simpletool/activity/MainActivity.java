package tw.kaneshih.simpletool.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import tw.kaneshih.simpletool.R;
import tw.kaneshih.simpletool.api.ApiCallback;
import tw.kaneshih.simpletool.api.ApiResult;
import tw.kaneshih.simpletool.api.ApiTask;
import tw.kaneshih.simpletool.utility.HttpUtil.Request;
import tw.kaneshih.simpletool.utility.UiUtil;
import tw.kaneshih.simpletool.view.CountDownView;

public class MainActivity extends AppCompatActivity {
    private UiUtil uiUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // --- enable logcat, separate by package
        tw.kaneshih.simpletool.api.Logcat.enableDebug(true);
        tw.kaneshih.simpletool.utility.Logcat.enableDebug(true);
        tw.kaneshih.simpletool.view.Logcat.enableDebug(true);

        // --- demo, CountDownView and UiUtil

        uiUtil = new UiUtil(this);

        final CountDownView countDownView = (CountDownView) findViewById(R.id.countDownView);
        countDownView.setOnTimesUpListener(new CountDownView.OnTimesUpListener() {
            @Override
            public void onTimesUp() {
                uiUtil.showToast("count down done");
            }
        });
        countDownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownView.isTimeUp()) {
                    uiUtil.showToast("count down again");
                    countDownView.startCountDown(65);
                } else {
                    if (countDownView.isCountingDown()) {
                        uiUtil.showToast("count down pause");
                        countDownView.pause();
                    } else {
                        uiUtil.showToast("count down resume");
                        countDownView.resume();
                    }
                }
            }
        });
        countDownView.setTextForTimesUp("Times up");
        countDownView.startCountDown(65);

        // -- demo, call api task's child
        FindFeedTask.search("Android Developer", callback);
        FindFeedTask.search("Juno Jupiter", callback);

        // -- TODO other demo
    }

    // make this static inner class if necessary
    private ApiCallback<JSONObject> callback = new ApiCallback<JSONObject>() {
        @Override
        public void onResult(ApiResult<JSONObject> result) {
            if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
                if (result.isSuccess()) {
                    uiUtil.showConfirmDialog("task result success", result.getData().toString(), "ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, null, null);
                } else {
                    uiUtil.showConfirmDialog("task result failed", result.getErrorMsg(), "ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, null, null);
                }
            }
        }
    };

    // extends ApiTask, just query something back from internet
    private static class FindFeedTask extends ApiTask<JSONObject> {
        private String keyword;

        private FindFeedTask(String keyword, ApiCallback<JSONObject> callback) {
            super(callback);
            this.keyword = keyword;
        }

        public static void search(String keyword, ApiCallback<JSONObject> callback) {
            new FindFeedTask(keyword, callback).execute();
        }

        @Override
        protected JSONObject getDataObjectFromCache() {
            return null;
        }

        @Override
        protected Request prepareRequest() {
            // this API is from https://developers.google.com/feed/v1/jsondevguide
            return new Request(Request.GET)
                    .setUrl("https://ajax.googleapis.com/ajax/services/feed/find")
                    .addQuery("v", "1.0")
                    .addQuery("q", keyword);
        }

        @Override
        protected JSONObject parseSuccessData(String data) {
            try {
                return new JSONObject(data);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void saveDataObjectToCache(JSONObject dataObject) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
