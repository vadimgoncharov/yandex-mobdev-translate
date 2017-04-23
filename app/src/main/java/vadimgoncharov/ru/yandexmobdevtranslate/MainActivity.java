package vadimgoncharov.ru.yandexmobdevtranslate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.widget.AdapterView.*;

public class MainActivity extends AppCompatActivity {

    private Button mButtonLangFrom;
    private Button mButtonLangTo;
    private Button mButtonLangToggle;
    private String mLangFrom = "en";
    private String mLangTo = "ru";
    private TextView mOutputText;
    private EditText mInputText;
    private OkHttpClient mHttpClient = new OkHttpClient();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };

    String run(String url) throws IOException {
        String lang = mLangFrom + "-" + mLangTo;
        Uri builtUri = Uri.parse("https://translate.yandex.net/api/v1.5/tr.json/translate?")
                .buildUpon()
                .appendQueryParameter("key", "trnsl.1.1.20170423T093038Z.3a4d0fb39aafc7e4.a8d74bdbe4e8f71c38716c4ff910f6615cfeea95")
                .appendQueryParameter("text", mInputText.getText().toString())
                .appendQueryParameter("lang", lang)
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

        mOutputText = (TextView) findViewById(R.id.output_text);
        mInputText = (EditText) findViewById(R.id.input_text);
        mButtonLangFrom = (Button) findViewById(R.id.button_lang_from);
        mButtonLangTo = (Button) findViewById(R.id.button_lang_to);
        mButtonLangToggle = (Button) findViewById(R.id.button_lang_toggle);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LayoutInflater inflater = getLayoutInflater();
        final View convertView = (View) inflater.inflate(R.layout.custom_dialog, null);
        final ListView mainListView = (ListView) convertView.findViewById(R.id.list_custom);
        ArrayList<String> langsList = new ArrayList<String>();
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, langsList);


        TreeMap<String, String> langs = getLangsList();
        if (langs != null) {
            for (Map.Entry<String,String> entry : langs.entrySet()) {
                String value = entry.getValue();
                String key = entry.getKey();
                listAdapter.add(key + ": " + value);
            }
        }

        mainListView.setAdapter( listAdapter );
        final AlertDialog.Builder langChooserDialog = new AlertDialog.Builder(MainActivity.this);
        langChooserDialog.setView(convertView);
        langChooserDialog.setTitle("Translate from");
        final AlertDialog show = langChooserDialog.create();
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mLangFrom = listAdapter.getItem(i);
                show.dismiss();
                System.out.println(mLangFrom);
            }
        });


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

        mButtonLangFrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                show.show();
            }
        });
    }

    private TreeMap<String, String> getLangsList() {

        String langsJsonString = getLangsJsonString();
        JSONObject langsJson = null;
        HashMap<String, String> langsHashmap = new HashMap<String, String>();

        try {
            langsJson = new JSONObject(langsJsonString);

            try {
                JSONObject langsObjs = langsJson.getJSONObject("langs");
                for(int i = 0; i<langsObjs.length(); i++){
                    String k = langsObjs.names().getString(i);
                    String v = (String) langsObjs.get(langsObjs.names().getString(i));
                    langsHashmap.put(k, v);
                }
                TreeMap<String, String> langsTreemap = new TreeMap<String, String>(langsHashmap);
                return langsTreemap;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getLangsJsonString() {
        InputStream is = getResources().openRawResource(R.raw.langs_en);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                String jsonString = writer.toString();
                return jsonString;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
