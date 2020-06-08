// Justice fragment.

package com.dialectek.even_the_odds;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import com.google.gson.JsonObject;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class JusticeFragment extends Fragment {

    private View m_view;
    private Context m_context;
    private Spinner m_myDynamicSpinner;
    private ArrayAdapter m_adapterForSpinner;
    private String server_hint = "<add server>";
    private static String mURLname;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_view = inflater.inflate(R.layout.justice_fragment, container, false);
        m_context = m_view.getContext();
        return m_view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        m_myDynamicSpinner = (Spinner)m_view.findViewById(R.id.dynamicSpinner);
        m_adapterForSpinner = new ArrayAdapter(m_view.getContext(), android.R.layout.simple_spinner_item);
        m_adapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_myDynamicSpinner.setAdapter(m_adapterForSpinner);
        Button connectButton = (Button)m_view.findViewById(R.id.ConnectBtn);
        Button addButton = (Button)m_view.findViewById(R.id.AddBtn);
        Button removeButton = (Button)m_view.findViewById(R.id.RemoveBtn);

        connectButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                getCases();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                final EditText input = new EditText(m_context);

                                                // Show new server input dialog
                                                new AlertDialog.Builder(m_context).
                                                        setTitle("Server:").
                                                        setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener()
                                                        {
                                                            public void onClick(DialogInterface dialog, int whichButton)
                                                            {
                                                                String server = input.getText().toString();
                                                                boolean isEmpty = server == null || server.trim().isEmpty();
                                                                if (!isEmpty) {
                                                                    if (m_myDynamicSpinner.getSelectedItem().toString().equals(server_hint)) {
                                                                        m_adapterForSpinner.clear();
                                                                    }
                                                                    boolean dup = false;
                                                                    for (int i = 0, j = m_myDynamicSpinner.getCount(); i < j; i++)
                                                                    {
                                                                        if (m_adapterForSpinner.getItem(i).toString().equals(server)) {
                                                                            dup = true;
                                                                            break;
                                                                        }
                                                                    }
                                                                    if (!dup) {
                                                                        CharSequence textHolder = input.getText();
                                                                        m_adapterForSpinner.add(textHolder);
                                                                        updateServerFile();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                ).setNegativeButton("Cancel", null).show();
                                            }
                                        }
        );
        removeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                removeServer();
            }
        });

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new FileReader(new File(MainActivity.mServersFile)));
            String line;
            while ((line = rd.readLine()) != null) {
                CharSequence textHolder = line;
                m_adapterForSpinner.add(textHolder);
            }
        } catch (Exception e) {
        }
        if (rd != null) {
            try {
                rd.close();
            } catch (Exception e) {}
        }
        if (m_adapterForSpinner.isEmpty()) {
            CharSequence textHolder = server_hint;
            m_adapterForSpinner.add(textHolder);
        }
    }

    private void getCases() {
        if (m_myDynamicSpinner.getCount() > 0) {
            if (!m_myDynamicSpinner.getSelectedItem().toString().equals(server_hint)) {
                mURLname = m_myDynamicSpinner.getSelectedItem().toString() + "/EvenTheOdds/rest/service/get_cases";
                new HTTPgetTask().execute();
            }
        }
    }

    private static class HTTPgetTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(mURLname);
                urlConnection = (HttpURLConnection) url.openConnection();

                int code = urlConnection.getResponseCode();
                if (code != 200) {
                    throw new IOException("Invalid response from server: " + code);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    Log.i("data", line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }
    }

    private static class HTTPpostTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                JsonObject postData = new JsonObject();
                postData.addProperty("name", "morpheus");
                postData.addProperty("job", "leader");

                URL url = new URL("https://reqres.in/api/users");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        out, "UTF-8"));
                writer.write(postData.toString());
                writer.flush();

                int code = urlConnection.getResponseCode();
                if (code != 201) {
                    throw new IOException("Invalid response from server: " + code);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    Log.i("data", line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }
    }

    private void removeServer() {
        if (m_myDynamicSpinner.getCount() > 0) {
            if (!m_myDynamicSpinner.getSelectedItem().toString().equals(server_hint)) {
                String selected = m_myDynamicSpinner.getSelectedItem().toString();
                int c = m_myDynamicSpinner.getCount();
                ArrayList<String> servers = new ArrayList<String>();
                for (int i = 0; i < c; i++) {
                    String s = m_myDynamicSpinner.getItemAtPosition(i).toString();
                    if (!s.equals(selected)) {
                        servers.add(s);
                    }
                }
                m_myDynamicSpinner = (Spinner)m_view.findViewById(R.id.dynamicSpinner);
                m_adapterForSpinner = new ArrayAdapter(m_view.getContext(), android.R.layout.simple_spinner_item);
                m_adapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                m_myDynamicSpinner.setAdapter(m_adapterForSpinner);
                for (String s : servers) {
                    CharSequence textHolder = s;
                    m_adapterForSpinner.add(textHolder);
                }
                updateServerFile();
                if (m_myDynamicSpinner.getCount() == 0) {
                    CharSequence textHolder = server_hint;
                    m_adapterForSpinner.add(textHolder);
                }
            }
        }
    }

    private void updateServerFile()
    {
        FileWriter writer = null;
        try {
            writer = new FileWriter(MainActivity.mServersFile);
            PrintWriter pr = new PrintWriter(writer);
            int c = m_myDynamicSpinner.getCount();
            for (int i = 0; i < c; i++) {
                pr.write(m_myDynamicSpinner.getItemAtPosition(i).toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {}
        }
    }
}
