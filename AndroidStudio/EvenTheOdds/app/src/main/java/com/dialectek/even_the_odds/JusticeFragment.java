package com.dialectek.even_the_odds;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class JusticeFragment extends Fragment {


    View view;
    Button justiceButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.justice_fragment, container, false);
        justiceButton = (Button) view.findViewById(R.id.button_justice);
        justiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Justice Fragment", Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }
}
