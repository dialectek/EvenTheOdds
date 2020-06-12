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
import android.widget.AdapterView;
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
    private Spinner m_serverSpinner;
    private ArrayAdapter m_adapterForServerSpinner;
    private String server_hint = "<add server>";
    private static String mURLname;
    private Spinner m_caseSpinner;
    private ArrayAdapter m_adapterForCaseSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_view = inflater.inflate(R.layout.justice_fragment, container, false);
        m_context = m_view.getContext();
        return m_view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        m_serverSpinner = (Spinner)m_view.findViewById(R.id.server_spinner);
        m_serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateServerFile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        m_adapterForServerSpinner = new ArrayAdapter(m_view.getContext(), android.R.layout.simple_spinner_item);
        m_adapterForServerSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_serverSpinner.setAdapter(m_adapterForServerSpinner);
        Button connectServerButton = (Button)m_view.findViewById(R.id.connect_server_button);
        Button addServerButton = (Button)m_view.findViewById(R.id.add_server_button);
        Button removeServerButton = (Button)m_view.findViewById(R.id.remove_server_button);

        connectServerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                getCases();
            }
        });
        addServerButton.setOnClickListener(new View.OnClickListener()
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
                                                                    if (m_serverSpinner.getSelectedItem().toString().equals(server_hint)) {
                                                                        m_adapterForServerSpinner.clear();
                                                                    }
                                                                    boolean dup = false;
                                                                    for (int i = 0, j = m_serverSpinner.getCount(); i < j; i++)
                                                                    {
                                                                        if (m_adapterForServerSpinner.getItem(i).toString().equals(server)) {
                                                                            dup = true;
                                                                            break;
                                                                        }
                                                                    }
                                                                    if (!dup) {
                                                                        CharSequence textHolder = input.getText();
                                                                        m_adapterForServerSpinner.add(textHolder);
                                                                        updateServerFile();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                ).setNegativeButton("Cancel", null).show();
                                            }
                                        }
        );
        removeServerButton.setOnClickListener(new View.OnClickListener(){

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
                m_adapterForServerSpinner.add(textHolder);
            }
        } catch (Exception e) {
        }
        if (rd != null) {
            try {
                rd.close();
            } catch (Exception e) {}
        }
        if (m_adapterForServerSpinner.isEmpty()) {
            CharSequence textHolder = server_hint;
            m_adapterForServerSpinner.add(textHolder);
        }

        m_caseSpinner = (Spinner)m_view.findViewById(R.id.case_spinner);
        m_adapterForCaseSpinner = new ArrayAdapter(m_view.getContext(), android.R.layout.simple_spinner_item);
        m_adapterForCaseSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_caseSpinner.setAdapter(m_adapterForCaseSpinner);
        CharSequence textHolder = "<connect to server>";
        m_adapterForCaseSpinner.add(textHolder);
        Button selectCaseButton = (Button)m_view.findViewById(R.id.select_case_button);
        selectCaseButton.setEnabled(false);
        Button addCaseButton = (Button)m_view.findViewById(R.id.add_case_button);
        addCaseButton.setEnabled(false);

        selectCaseButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                selectCase();
            }
        });
        addCaseButton.setOnClickListener(new View.OnClickListener()
                                     {
                                         @Override
                                         public void onClick(View v)
                                         {
                                             final EditText input = new EditText(m_context);

                                             // Show new case input dialog
                                             new AlertDialog.Builder(m_context).
                                                     setTitle("Case:").
                                                     setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener()
                                                     {
                                                         public void onClick(DialogInterface dialog, int whichButton)
                                                         {
                                                             String caseVal = input.getText().toString();
                                                             boolean isEmpty = caseVal == null || caseVal.trim().isEmpty();
                                                             if (!isEmpty) {
                                                                 boolean dup = false;
                                                                 for (int i = 0, j = m_caseSpinner.getCount(); i < j; i++)
                                                                 {
                                                                     if (m_adapterForCaseSpinner.getItem(i).toString().equals(caseVal)) {
                                                                         dup = true;
                                                                         break;
                                                                     }
                                                                 }
                                                                 if (!dup) {
                                                                     CharSequence textHolder = input.getText();
                                                                     m_adapterForCaseSpinner.add(textHolder);
                                                                 }
                                                             }
                                                         }
                                                     }
                                             ).setNegativeButton("Cancel", null).show();
                                         }
                                     }
        );
    }

    private void getCases() {
        if (m_serverSpinner.getCount() > 0) {
            if (!m_serverSpinner.getSelectedItem().toString().equals(server_hint)) {
                mURLname = m_serverSpinner.getSelectedItem().toString() + "/EvenTheOdds/rest/service/get_cases";
                new HTTPgetTask().execute();
            }
        }
    }

    private void selectCase() {
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
        if (m_serverSpinner.getCount() > 0) {
            if (!m_serverSpinner.getSelectedItem().toString().equals(server_hint)) {
                String selected = m_serverSpinner.getSelectedItem().toString();
                int c = m_serverSpinner.getCount();
                ArrayList<String> servers = new ArrayList<String>();
                for (int i = 0; i < c; i++) {
                    String s = m_serverSpinner.getItemAtPosition(i).toString();
                    if (!s.equals(selected)) {
                        servers.add(s);
                    }
                }
                m_serverSpinner = (Spinner)m_view.findViewById(R.id.server_spinner);
                m_adapterForServerSpinner = new ArrayAdapter(m_view.getContext(), android.R.layout.simple_spinner_item);
                m_adapterForServerSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                m_serverSpinner.setAdapter(m_adapterForServerSpinner);
                for (String s : servers) {
                    CharSequence textHolder = s;
                    m_adapterForServerSpinner.add(textHolder);
                }
                updateServerFile();
                if (m_serverSpinner.getCount() == 0) {
                    CharSequence textHolder = server_hint;
                    m_adapterForServerSpinner.add(textHolder);
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
            int c = m_serverSpinner.getCount();
            if (c > 0) {
                String selected = m_serverSpinner.getSelectedItem().toString();
                pr.write(selected + "\n");
                for (int i = 0; i < c; i++) {
                    String current = m_serverSpinner.getItemAtPosition(i).toString();
                    if (!selected.equals(current)) {
                        pr.write(current + "\n");
                    }
                }
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
