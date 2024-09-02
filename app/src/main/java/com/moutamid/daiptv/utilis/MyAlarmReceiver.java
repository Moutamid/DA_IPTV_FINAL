package com.moutamid.daiptv.utilis;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;

import com.fxn.stash.Stash;
import com.google.android.material.snackbar.Snackbar;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.database.AppDatabase;
import com.moutamid.daiptv.models.EPGModel;
import com.moutamid.daiptv.models.UserModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "MyAlarmReceiver";
    AppDatabase database;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: MyAlarmReceiver");

        database = AppDatabase.getInstance(context);

        long time = Stash.getLong(Constants.IS_TODAY, 0);
        LocalDate date;
        if (time != 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                date = Instant.ofEpochMilli(time)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                LocalDate today = LocalDate.now();
                boolean isToday = date.equals(today);
                Log.d(TAG, "onCreate: ISTODAY " + isToday);
                if (!isToday) {
                    database.epgDAO().Delete();
                }
            }
        } else {
            database.epgDAO().Delete();
        }

        List<EPGModel> epg = database.epgDAO().getEPG();
        if (epg.isEmpty()) {
            getEpg();
        }
    }

    private void getEpg() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        Log.d(TAG, "get: LOADING");
        String url = ApiLinks.base() + "xmltv.php?username=" + userModel.username + "&password=" + userModel.password;
        Log.d(TAG, "getEpg: " + url);
        Log.d(TAG, "base: " + ApiLinks.base());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                // Handle failure
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: ERROR " + response.message());
                }

                String xmlContent = response.body().string();

                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(xmlContent)));

                    // Get the root element
                    Element root = document.getDocumentElement();
                    // Get a NodeList of programme elements
                    NodeList programmeList = root.getElementsByTagName("programme");
                    Log.d(TAG, "programmeList: " + programmeList.getLength());

                    for (int i = 0; i < programmeList.getLength(); i++) {
                        Node programmeNode = programmeList.item(i);
                        if (programmeNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element programmeElement = (Element) programmeNode;

                            // Get attributes
                            String start = programmeElement.getAttribute("start");
                            String stop = programmeElement.getAttribute("stop");
                            String channel = programmeElement.getAttribute("channel");

                            // Get child elements
                            String title = programmeElement.getElementsByTagName("title").item(0).getTextContent();

                            EPGModel epgModel = new EPGModel(start, stop, channel, title);
                            database.epgDAO().insert(epgModel);
                        }

                        if (i == programmeList.getLength() - 1) {
                            Log.d(TAG, "onResponse: DONE");
                            Stash.put(Constants.IS_TODAY, System.currentTimeMillis());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
