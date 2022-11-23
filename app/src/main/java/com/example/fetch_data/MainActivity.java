package com.example.fetch_data;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.fetch_data.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<HashMap> listView;
    ArrayAdapter<HashMap> listAdapter;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeitemlist();
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new fetchdata().start();
            }
        });
    }
    private void initializeitemlist() {
        listView=new ArrayList<>();
        listAdapter = new ArrayAdapter<HashMap>(this, android.R.layout.simple_list_item_1, listView);
        binding.listView.setAdapter(listAdapter);
    }
    class fetchdata extends Thread{

        String data = "";
        @Override
        public void run() {

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Fetch Data");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                }
            });

            try {
                // fetch json data from given url. Read data line by line
                URL url=new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null){
                    data=data + line;
                }

                if(!data.isEmpty()){
                // Convert data into JSONArray and store it in Hashmap.
                    JSONArray json = new JSONArray(data);
                    listView.clear();
                    //Use Hashmap and Arraylist to manage and sort our data
                    HashMap<Integer, ArrayList<HashMap<String,String>>> map=new HashMap<>();
                    for(int i=0; i<json.length(); i++){
                        // Create JSONObject for each given elements in data
                        JSONObject Id = json.getJSONObject(i);
                        String id= Id.getString("id");
                        JSONObject Listid = json.getJSONObject(i);
                        int listid = Listid.getInt("listId");
                        JSONObject names= json.getJSONObject(i);
                        String name = names.getString("name");

                        HashMap<String,String> item = new HashMap<>();
                            item.put("id", id);
                            item.put("name", name);

                       if(name!="null" && name!="" && !name.isEmpty() && !name.equals("null")){
                            if(map.containsKey(listid)) {
                                map.get(listid).add(item);

                            }else{
                                ArrayList<HashMap<String,String>> temp=new ArrayList<>();
                                temp.add(item);
                                map.put(listid, temp);
                            }
                       }
                    }
                    // Here Comparator used for comparing and sorting our Arraylist data
                    for(Map.Entry<Integer, ArrayList<HashMap<String, String>>> set: map.entrySet()){
                        ArrayList<HashMap<String,String>> sorted_list =set.getValue();

                        Comparator<HashMap<String, String>> valueComparator = new Comparator<HashMap<String,String>>() {

                            @Override
                            public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {

                                Integer temp1 = Integer.parseInt(o1.get("id"));
                                Integer temp2 = Integer.parseInt(o2.get("id"));

                                return temp1.compareTo(temp2);
                            }
                        };
                        Collections.sort(sorted_list, valueComparator);
                        set.setValue(sorted_list);
                    }
                    //Add Hashmap into our Arraylist listview for display
                    listView.add(map);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    listAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}