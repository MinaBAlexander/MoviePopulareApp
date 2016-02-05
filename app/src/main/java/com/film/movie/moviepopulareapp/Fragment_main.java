package com.film.movie.moviepopulareapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import java.io.IOException;



public class Fragment_main extends Fragment  {


    ArrayList<Movie> MovieList;
    MovieAdapter adapter;
    Spinner spinner;
    ArrayAdapter<CharSequence> adapterSpinner;
    public static final String Default="N/A";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main,container,false);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



        ///for spinner
        spinner=(Spinner)view.findViewById(R.id.spinner);
        //ArrayAdapter
        adapterSpinner=ArrayAdapter.createFromResource(this.getActivity(),R.array.Preference,android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SharedPreferences sharedPreferences=this.getActivity().getSharedPreferences("SortByRate", Context.MODE_PRIVATE);

        spinner.setAdapter(adapterSpinner);
        spinner.setSelection(sharedPreferences.getInt("PREF_SPINNER", 0));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                new JSONAsyncTask().execute("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=610d2a960011e9203aaf2547007cd5f9");
                SharedPreferences preferences =getActivity().getSharedPreferences("SortByRate", Context.MODE_PRIVATE);
                preferences.edit().putInt("PREF_SPINNER", position).commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });


        MovieList = new ArrayList<Movie>();
        new JSONAsyncTask().execute("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=610d2a960011e9203aaf2547007cd5f9");
        final GridView gridView=(GridView)view.findViewById(R.id.gridView);
        adapter = new MovieAdapter(getActivity(), R.layout.fragment_main_list_item_display, MovieList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Fragment_details descriptionFragment = (Fragment_details) getFragmentManager()
                        .findFragmentById(R.id.fragment_details);

                Fragment_details newFragment = new Fragment_details();
                Bundle args = new Bundle();
                MovieAdapter.ViewHolder holder = (MovieAdapter.ViewHolder) view.getTag();
                args.putInt("MovieID", MovieList.get(position).getId());
                args.putString("original_title", MovieList.get(position).getOriginal_title());
                args.putString("poster_path", MovieList.get(position).getImage());
                args.putString("overview", MovieList.get(position).getOverview());
                args.putInt("vote_average", MovieList.get(position).getVote_average());
                args.putString("release_date", MovieList.get(position).getRelease_date());
                args.putInt("popularity", MovieList.get(position).getPopularity());

                newFragment.setArguments(args);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                if(descriptionFragment ==null) {
                    transaction.replace(R.id.bb, newFragment).commit();

                }
                else
                    transaction.replace(R.id.fragment_details, newFragment).commit();



            }
        });

        return view;
    }
    class JSONAsyncTask extends AsyncTask<String, String, String>
    {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading, please wait");
            dialog.setTitle("Connecting server");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection Connection = null;
            BufferedReader reader = null;
            try {
                URL url=new URL(params[0]);
                Connection=(HttpURLConnection)url.openConnection();
                Connection.connect();
                InputStream stream=Connection.getInputStream();
                reader =new BufferedReader((new InputStreamReader(stream)));
                StringBuffer buffer =new StringBuffer();
                String line="";
                while ((line=reader.readLine())!=null)
                {
                    buffer.append(line);
                }

                String finalJson= buffer.toString();

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SortByRate", Context.MODE_PRIVATE);
                int sortPreference = sharedPreferences.getInt("PREF_SPINNER", 0);

                JSONObject jsono = new JSONObject(finalJson);
                JSONArray jarray = jsono.getJSONArray("results");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject object = jarray.getJSONObject(i);

                    Movie movie = new Movie();
                    movie.setImage(object.getString("poster_path"));
                    movie.setId(object.getInt("id"));
                    movie.setOriginal_title(object.getString("original_title"));
                    movie.setOverview(object.getString("overview"));
                    movie.setVote_average(object.getInt("vote_average"));
                    movie.setVote_count(object.getInt("vote_count"));
                    movie.setRelease_date(object.getString("release_date"));
                    movie.setPopularity(object.getInt("popularity"));
                    MovieList.add(movie);
                }

                if (sortPreference == 0) {
                    MovieList.clear();

                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);
                        Movie movie = new Movie();
                        movie.setImage(object.getString("poster_path"));
                        movie.setId(object.getInt("id"));
                        movie.setOriginal_title(object.getString("original_title"));
                        movie.setOverview(object.getString("overview"));
                        movie.setVote_average(object.getInt("vote_average"));
                        movie.setVote_count(object.getInt("vote_count"));
                        movie.setRelease_date(object.getString("release_date"));
                        movie.setPopularity(object.getInt("popularity"));
                        MovieList.add(movie);
                    }

                    Comparator<Movie> ComByMostPopular = new Comparator<Movie>() {
                        @Override
                        public int compare(Movie lhs, Movie rhs) {
                            return lhs.getPopularity() - rhs.getPopularity();

                        }
                    };
                    Collections.sort(MovieList, ComByMostPopular);

                }

                else if (sortPreference == 1) {
                    MovieList.clear();
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);
                        Movie movie = new Movie();
                        movie.setImage(object.getString("poster_path"));
                        movie.setId(object.getInt("id"));
                        movie.setOriginal_title(object.getString("original_title"));
                        movie.setOverview(object.getString("overview"));
                        movie.setVote_average(object.getInt("vote_average"));
                        movie.setRelease_date(object.getString("release_date"));
                        movie.setPopularity(object.getInt("popularity"));
                        MovieList.add(movie);
                    }
                    Comparator<Movie> ComByMostPopular = new Comparator<Movie>() {
                        @Override
                        public int compare(Movie lhs, Movie rhs) {
                            return lhs.getVote_average() - rhs.getVote_average();

                        }
                    };
                    Collections.sort(MovieList, ComByMostPopular);
                }

                else if (sortPreference == 2) {
                    MovieList.clear();
                    SharedPreferences s = getActivity().getSharedPreferences("db", 0);
                    Set<String> xx = s.getStringSet("set", new HashSet<String>());
                    Iterator<String> it = xx.iterator();
                    while (it.hasNext()) {

                        String Movie_id = (String) it.next();
                        int MovieId = Integer.parseInt(Movie_id);

                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject object = jarray.getJSONObject(i);
                            Movie movie = new Movie();
                            if (object.getInt("id") == MovieId) {
                                movie.setImage(object.getString("poster_path"));
                                movie.setId(object.getInt("id"));
                                movie.setOriginal_title(object.getString("original_title"));
                                movie.setOverview(object.getString("overview"));
                                movie.setVote_average(object.getInt("vote_average"));
                                movie.setVote_count(object.getInt("vote_count"));
                                movie.setRelease_date(object.getString("release_date"));
                                movie.setPopularity(object.getInt("popularity"));
                                MovieList.add(movie);

                            }

                        }
                    }

                }





                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(Connection !=null) {
                    Connection.disconnect();
                }
                try {
                    if(reader!=null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        protected void onPostExecute(String result) {
            dialog.cancel();
            adapter.notifyDataSetChanged();
            if(result == null)
                Toast.makeText(getActivity(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();

        }
    }



}
