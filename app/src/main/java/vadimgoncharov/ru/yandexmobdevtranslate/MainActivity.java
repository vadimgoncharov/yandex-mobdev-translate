package vadimgoncharov.ru.yandexmobdevtranslate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;

public class MainActivity extends AppCompatActivity {

    private Button mButtonLangFrom;
    private Button mButtonLangTo;
    private Button mButtonLangToggle;
    private String mLangFrom = "English";
    private String mLangTo = "Russian";
    private TreeMap<String, String> mLangsList;
    private TextView mOutputText;
    private EditText mInputText;
    private OkHttpClient mHttpClient = new OkHttpClient();

    public void runDictAsync() throws Exception {
        Uri builtUri = Uri.parse("https://dictionary.yandex.net/api/v1/dicservice.json/lookup?")
                .buildUpon()
                .appendQueryParameter("key", getString(R.string.YANDEX_DICT_API_KEY))
                .appendQueryParameter("lang", getLangParamForApiRequest())
                .appendQueryParameter("text", mInputText.getText().toString())
                .build();
        URL _url = new URL(builtUri.toString());

        Request request = new Request.Builder()
                .url(_url)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOutputText.setText("");
                    }
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutputText.setText("");
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                };

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    String trText = json
                            .getJSONArray("def")
                            .getJSONObject(0)
                            .getJSONArray("tr")
                            .getJSONObject(0)
                            .getString("text");
                    String meanText = json
                            .getJSONArray("def")
                            .getJSONObject(0)
                            .getJSONArray("tr")
                            .getJSONObject(0)
                            .getJSONArray("mean")
                            .getJSONObject(0)
                            .getString("text");
                    String resultText = trText;
                    if (meanText.length() > 0) {
                        resultText += " (" + meanText + ")";
                    }
                    final String resultTextFinal = resultText;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutputText.setText("Dict: " + resultTextFinal);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutputText.setText("");
                        }
                    });
                }
            }
        });
    }

    public void runTranslateAsync() throws Exception {
        Uri builtUri = Uri.parse("https://translate.yandex.net/api/v1.5/tr.json/translate?")
                .buildUpon()
                .appendQueryParameter("key", getString(R.string.YANDEX_TR_API_KEY))
                .appendQueryParameter("text", mInputText.getText().toString())
                .appendQueryParameter("lang", getLangParamForApiRequest())
                .build();
        URL _url = new URL(builtUri.toString());

        Request request = new Request.Builder()
                .url(_url)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOutputText.setText("");
                    }
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutputText.setText("");
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                };

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    final String resultText = json.getJSONArray("text").getString(0);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutputText.setText("Translate: " + resultText);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutputText.setText("");
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);


        mLangsList = getLangsListInvertedKeyValue();
        mOutputText = (TextView) findViewById(R.id.output_text);
        mInputText = (EditText) findViewById(R.id.input_text);
        mButtonLangFrom = (Button) findViewById(R.id.button_lang_from);
        mButtonLangTo = (Button) findViewById(R.id.button_lang_to);
        mButtonLangToggle = (Button) findViewById(R.id.button_lang_toggle);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.settings_pref), MODE_PRIVATE);

        String prefLangFrom = pref.getString(getString(R.string.lang_from_saved), "English");
        String prefLangTo = pref.getString(getString(R.string.lang_to_saved), "Russian");

        setLangFrom(prefLangFrom);
        setLangTo(prefLangTo);

        final AlertDialog langFromChooserDialog = createLangChooserDialog(true, prefLangFrom);
        final AlertDialog langToChooserDialog = createLangChooserDialog(false, prefLangTo);

        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (stringContainsMultiplyWords(mInputText.getText().toString())) {
                        runTranslateAsync();
                    } else {
                        runDictAsync();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
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
                langFromChooserDialog.show();
            }
        });
        mButtonLangTo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                langToChooserDialog.show();
            }
        });

        mButtonLangToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String newLangFrom = mLangTo;
                String newLangTo = mLangFrom;

                setLangFrom(newLangFrom);
                setLangTo(newLangTo);

                // TODO change selectedItem in adapters
            }
        });
    }

    private Boolean stringContainsMultiplyWords(String str) {
        String[] name = str.split("\\s+");
        if (name.length > 1) {
            return true;
        } else {
            return false;
        }
    }

    private AlertDialog createLangChooserDialog(final boolean isFrom, String selectedLang) {
        LayoutInflater inflater = getLayoutInflater();
        final View convertView = (View) inflater.inflate(R.layout.custom_dialog, null);
        final ListView mainListView = (ListView) convertView.findViewById(R.id.list_custom);
        ArrayList<String> langsList = new ArrayList<String>();
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_checked, langsList);


        TreeMap<String, String> langs = getLangsList();
        if (langs != null) {
            for (Map.Entry<String,String> entry : langs.entrySet()) {
                String value = entry.getValue();
                String key = entry.getKey();
                listAdapter.add(value);
            }
        }


        mainListView.setAdapter( listAdapter );
        mainListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final AlertDialog.Builder langChooserDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        langChooserDialogBuilder.setView(convertView);
        if (isFrom) {
            langChooserDialogBuilder.setTitle("Translate from");
        } else {
            langChooserDialogBuilder.setTitle("Translate to");
        }
        final AlertDialog langChooserDialog = langChooserDialogBuilder.create();

        if (selectedLang != null) {
            for (int i = 0; i < langsList.size(); i++) {
                if (langsList.get(i).equals(selectedLang)) {
                    mainListView.setItemChecked(i, true);
                }
            }
        }

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String lang = listAdapter.getItem(i);
                if (isFrom) {
                    setLangFrom(lang);
                } else {
                    setLangTo(lang);
                }
                mainListView.setItemChecked(i, true);
                langChooserDialog.dismiss();
            }
        });

        return langChooserDialog;
    }

    private void setLangFrom(String lang) {
        mLangFrom = lang;
        mButtonLangFrom.setText(lang);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.settings_pref),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.lang_from_saved), lang);
        editor.commit();
    }

    private void setLangTo(String lang) {
        mLangTo = lang;
        mButtonLangTo.setText(lang);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.settings_pref),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.lang_to_saved), lang);
        editor.commit();
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

    private TreeMap<String, String> getLangsListInvertedKeyValue() {
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
                    langsHashmap.put(v, k);
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

    private String getLangParamForApiRequest() {
        String langFromKey = mLangsList.get(mLangFrom);
        String langToKey = mLangsList.get(mLangTo);

        String param =  langFromKey + "-" + langToKey;
        System.out.println(param);
        return param;
    }
}
