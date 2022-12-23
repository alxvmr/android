package com.example.placeholder_table;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity {

    private int counter = 0;
    private Integer count_windows = 5;
    private TableLayout tableLayout;
    ArrayList<String> elements = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tableLayout = (TableLayout) findViewById(R.id.tableLayout);

        Collections.addAll(elements, "","","","","");
        table_update(elements);
    }

    @SuppressLint("SetTextI18n")
    public void table_update(ArrayList<String> arr){

        for (int i = 0, j = tableLayout.getChildCount(); i < j; i++){
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow){
                TableRow row = (TableRow) view;
                for (int x = 0; x < row.getChildCount(); x++) {
                    View row_view = row.getChildAt(x);
                    if (row_view instanceof TextView){
                        ((TextView) row_view).setText(arr.get(i));
                    }
                }
            }
        }
    }

    public void onAdd(View view) {
        counter += 1;
        int length_stack = elements.size();
        if (length_stack >= count_windows){
            elements.remove(count_windows - 1);
        }
        elements.add(0, Integer.toString(counter));
        table_update(elements);
    }
}