package com.film.movie.moviepopulareapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MovieAdapterDetails extends ArrayAdapter<Movie> {

    ArrayList<Movie> list;
    LayoutInflater vi;
    int Resource;

    public MovieAdapterDetails(Context context, int resource, ArrayList<Movie> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v==null)
            v = vi.inflate(Resource, null);
        final Movie movie=list.get(position);

        TextView textView_original_title = (TextView)v.findViewById(R.id.original_title);
        ImageView imageView_d = (ImageView) v.findViewById(R.id.imageViewfilm);
        TextView tx_overview=(TextView)v.findViewById(R.id.overview);
        TextView tx_vote_average=(TextView)v.findViewById(R.id.vote_average);
        TextView tx_release_date=(TextView)v.findViewById(R.id.release_date);
        RatingBar ratingBar=(RatingBar)v.findViewById(R.id.ratingBar);


        textView_original_title.setText(movie.getOriginal_title());
        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + list.get(position).getImage()).placeholder(R.drawable.ic_launcher).into(imageView_d);
        tx_overview.setText(movie.getOverview());
        tx_vote_average.setText(String.valueOf(movie.getVote_average())+"  Rating");
        tx_release_date.setText(movie.getRelease_date());
        ratingBar.setRating(movie.getVote_average());
        return v;


    }
}
