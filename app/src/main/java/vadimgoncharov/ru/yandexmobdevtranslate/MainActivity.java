package vadimgoncharov.ru.yandexmobdevtranslate;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView mOutputText;
    private EditText mInputText;
    private OkHttpClient mHttpClient = new OkHttpClient();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };

    String run(String url) throws IOException {
        Uri builtUri = Uri.parse("https://translate.yandex.net/api/v1.5/tr.json/translate?")
                .buildUpon()
                .appendQueryParameter("key", "trnsl.1.1.20170423T093038Z.3a4d0fb39aafc7e4.a8d74bdbe4e8f71c38716c4ff910f6615cfeea95")
                .appendQueryParameter("text", mInputText.getText().toString())
                .appendQueryParameter("lang", "en-ru")
                .appendQueryParameter("format", "html")
                .build();
        URL _url = new URL(builtUri.toString());

        Request request = new Request.Builder()
                .url(_url)
                .build();

        Response response = mHttpClient.newCall(request).execute();
        return response.body().string();
    }

    String dictRun() throws IOException {
        Uri builtUri = Uri.parse("https://dictionary.yandex.net/api/v1/dicservice.json/lookup?")
                .buildUpon()
                .appendQueryParameter("key", "dict.1.1.20170423T095751Z.17c6722cd4f7ab85.274e20a3edcd7997a997d97d925f35f400c428bc")
                .appendQueryParameter("lang", "en-ru")
                .appendQueryParameter("text", mInputText.getText().toString())
                .build();
        URL _url = new URL(builtUri.toString());

        Request request = new Request.Builder()
                .url(_url)
                .build();

        Response response = mHttpClient.newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        mOutputText = (TextView) findViewById(R.id.output_text);
        mInputText = (EditText) findViewById(R.id.input_text);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

       try {
            String response = dictRun();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mOutputText.setText(charSequence);
                try {
                    String response = dictRun();
                    JSONObject json = new JSONObject(response);
                    System.out.println(response);
                    System.out.println(json.getJSONArray("def").getJSONObject(0).getString("text"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

}
