package org.trv.alex.remotefilemanager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityTAG";
    private static final int PERM_EXT_STORAGE_CODE = 1;

    private static final String PREF_URL_KEY = "prefUrlKey";
    private static final String CURRENT_URL_KEY = "currentUrlKey";
    private static final String LIST_KEY = "listKey";

    private List<FileProperties> mFilesList = new ArrayList<>();

    private ListView mFilesListView;
    private ArrayAdapter<FileProperties> mArrayAdapter;

    private HandlerThread mNetworkHanderThread;
    private Handler mNetworkHandler;

    private int mSelectedItemPos;

    private String mPrefURL;
    private String mCurrentURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PREF_URL_KEY)) {
                mPrefURL = savedInstanceState.getString(PREF_URL_KEY);
            }
            if (savedInstanceState.containsKey(CURRENT_URL_KEY)) {
                mCurrentURL = savedInstanceState.getString(CURRENT_URL_KEY);
            }
            if (savedInstanceState.containsKey(LIST_KEY)) {
                mFilesList = savedInstanceState.getParcelableArrayList(LIST_KEY);
            }
        }

        mArrayAdapter =
                new ArrayAdapter<FileProperties>(this, android.R.layout.simple_list_item_2,
                        android.R.id.text1, mFilesList) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);
                        text1.setText(mFilesList.get(position).getName());
                        text2.setText(mFilesList.get(position).getSize());
                        return view;
                    }
                };
        mFilesListView = findViewById(R.id.files_list_view);
        mFilesListView.setAdapter(mArrayAdapter);
        mFilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = mFilesList.get(position).getURL();
                if (mFilesList.get(position).isDirectory()) {
                    mCurrentURL = url;
                    getRemoteFileList(mCurrentURL);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), mFilesList.get(position).getType());
                    if (intent.resolveActivity(getPackageManager()) == null) {
                        Toast.makeText(MainActivity.this,
                                R.string.no_app_found_string,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(intent);
                    }
                }
            }
        });

        mNetworkHanderThread = new HandlerThread("Network");
        mNetworkHanderThread.start();
        mNetworkHandler = new Handler(mNetworkHanderThread.getLooper());

        registerForContextMenu(mFilesListView);

    }

    public void updateList() {
        mArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        mSelectedItemPos = pos;
        switch (item.getItemId()) {
            case R.id.context_download:
                if (ContextCompat
                        .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    downloadFile(mFilesList.get(pos));
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERM_EXT_STORAGE_CODE);
                }
                return true;
            case R.id.context_open_in_browser:
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent intent = builder.build();
                intent.launchUrl(this, Uri.parse(mFilesList.get(pos).getURL()));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_refresh:
                getRemoteFileList(mCurrentURL);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERM_EXT_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile(mFilesList.get(mSelectedItemPos));
            } else {
                Toast.makeText(this, R.string.not_granted_perm, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PREF_URL_KEY, mPrefURL);
        outState.putString(CURRENT_URL_KEY, mCurrentURL);
        outState.putParcelableArrayList(LIST_KEY, new ArrayList<Parcelable>(mFilesList));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsFragment.PREF_URL, "");
        if (!url.equals(mPrefURL)) {
            mPrefURL = url;
            mCurrentURL = mPrefURL;
            getRemoteFileList(mCurrentURL);
        }
    }

    public void getRemoteFileList(final String url) {
        mFilesList.clear();
        updateList();
        mNetworkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    URLConnection connection = new URL(url).openConnection();
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder html = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        html.append(scanner.nextLine());
                    }
//                    Document doc = Jsoup.connect(url).get();
                    Document doc = Jsoup.parse(html.toString());
                    Elements elements = doc.select("tbody > tr");
                    for (Element element : elements) {
                        String name = element.select(".n").text();
                        String type = element.select(".t").text();
                        String href = element.select(".n > a").attr("href");
                        String absoluteURL = new URL(new URL(url), href).toString();
                        String modified = element.select(".m").text();
                        String size = element.select(".s").text();
                        FileProperties fp = new FileProperties(absoluteURL, name, modified, size, type);
                        mFilesList.add(fp);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateList();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void downloadFile(FileProperties file) {
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(file.getURL()));
        request.setTitle(file.getName());
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getName());
        manager.enqueue(request);
    }
}
