package com.film.movie.moviepopulareapp;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.HashSet;
import java.util.Set;


public class Fragment_details extends Fragment {
    ArrayList<Movie> MovieList;
    MovieAdapterDetails adapter;
    ArrayList<Movie> MovieListReview;
    MovieAdapterReview adapterReview;

    ArrayList<Movie> MovieListTrailer;
    MovieAdapterTrailer adapterTrailer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        MovieList = new ArrayList<Movie>();  //f/or detatails top
        MovieListReview = new ArrayList<Movie>();  ///for review
        MovieListTrailer = new ArrayList<Movie>();  ///for trailer
        ////////
        Movie movie=new Movie();
        Bundle arguments = getArguments();
        if (arguments != null)
        {
            final int movieID = getArguments().getInt("MovieID");
            String original_title =  getArguments().getString("original_title");
            String image =  getArguments().getString("poster_path");
            String overview= getArguments().getString("overview");
            int vote_average=  getArguments().getInt("vote_average");
            String release_date= getArguments().getString("release_date");
            movie.setId(movieID);
            movie.setImage(image);
            movie.setOriginal_title(original_title);
            movie.setOverview(overview);
            movie.setVote_average(vote_average);
            movie.setRelease_date(release_date);
            MovieList.add(movie);
           final ListView listView=(ListView)view.findViewById(R.id.listViewDetails);
            adapter = new MovieAdapterDetails(getActivity(), R.layout.details_items_list_view, MovieList);
            adapter.notifyDataSetChanged();
             listView.setAdapter(adapter);
            /////
            /////For review
            new JSONAsyncTask().execute("http://api.themoviedb.org/3/movie/" + movieID + "/reviews?api_key=610d2a960011e9203aaf2547007cd5f9");

            final ListView listViewReview=(ListView)view.findViewById(R.id.listViewReview);
            adapterReview = new MovieAdapterReview(getActivity(), R.layout.item_listview_review, MovieListReview);
            adapterReview.notifyDataSetChanged();
            listViewReview.setAdapter(adapterReview);
//
            ///for trailer
            new JSONAsyncTaskTailer().execute("http://api.themoviedb.org/3/movie/" + movieID + "/videos?api_key=610d2a960011e9203aaf2547007cd5f9");
            final ListView listViewTrailer=(ListView)view.findViewById(R.id.listViewTrailer);
            adapterTrailer = new MovieAdapterTrailer(getActivity(), R.layout.item_trailer_listview, MovieListTrailer);
            adapterTrailer.notifyDataSetChanged();
            listViewTrailer.setAdapter(adapterTrailer);

//
            listViewTrailer.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                        long id) {

                    Go_to_Link_Youtube(MovieListTrailer.get(position).getTrailer());
                }
            });


            Button b=(Button)view.findViewById(R.id.buttonLike);
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(Details_Movie.this, "Movie_id" + movieID, Toast.LENGTH_LONG).show();
                    SharedPreferences ss = getActivity().getSharedPreferences("db", 0);
                    Set<String> hs = ss.getStringSet("set", new HashSet<String>());
                    hs.add(String.valueOf(movieID));
                    SharedPreferences.Editor edit = ss.edit();
                    edit.putStringSet("set", hs);
                    edit.commit();

                }
            });



        } else {

        }

        return view;
    }

    public void Go_to_Link_Youtube(String Link)
    {

        Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(Link));
        this.startActivity(intent11);


    }

    ///for review
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
                URL url = new URL(params[0]);
                Connection = (HttpURLConnection) url.openConnection();
                Connection.connect();
                InputStream stream = Connection.getInputStream();
                reader = new BufferedReader((new InputStreamReader(stream)));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                 SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SortByRate", Context.MODE_PRIVATE);
                int sortPreference = sharedPreferences.getInt("PREF_SPINNER", 0);

                JSONObject jsono = new JSONObject(finalJson);
                JSONArray jarray = jsono.getJSONArray("results");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject object = jarray.getJSONObject(i);

                    Movie movie=new Movie();
                    movie.setAuthor(object.getString("author"));
                    movie.setReview(object.getString("content"));
                    MovieListReview.add(movie);
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
            adapterReview.notifyDataSetChanged();
            if(result == null)
                Toast.makeText(getActivity(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();

        }
    }


    ///for trailler
    class JSONAsyncTaskTailer extends AsyncTask<String, String, String>
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
                URL url = new URL(params[0]);
                Connection = (HttpURLConnection) url.openConnection();
                Connection.connect();
                InputStream stream = Connection.getInputStream();
                reader = new BufferedReader((new InputStreamReader(stream)));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                JSONObject jsono = new JSONObject(finalJson);
                JSONArray jarray = jsono.getJSONArray("results");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject object = jarray.getJSONObject(i);

                    Movie movie=new Movie();
                    movie.setTrailer("https://www.youtube.com/watch?v=" + object.getString("key"));
                    MovieListTrailer.add(movie);

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
            adapterTrailer.notifyDataSetChanged();
            if(result == null)
                Toast.makeText(getActivity(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();

        }
    }

}
