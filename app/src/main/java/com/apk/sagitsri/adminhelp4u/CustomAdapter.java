package com.apk.sagitsri.adminhelp4u;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * Created by sagitsri on 29/10/19.
 */


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context mContext;
    private List<DataModel> albumList;



    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView lat, lon,tm,dt,mob;
        public Button dohelp;


        public MyViewHolder(View view) {
            super(view);
            lat = view.findViewById(R.id.latid);
            lon =  view.findViewById(R.id.lonid);
            tm = view.findViewById(R.id.tmid);
            dt = view.findViewById(R.id.dtid);
            mob = view.findViewById(R.id.mob);
            dohelp = view.findViewById(R.id.hlpbtn);
            dohelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final double lt;
                    final double ln;
                    String sndt;
                    String tm;
                    String mb;
                    lt = albumList.get(getAdapterPosition()).getLat();
                    ln = albumList.get(getAdapterPosition()).getLng();
                    sndt = albumList.get(getAdapterPosition()).getDt();
                    tm = albumList.get(getAdapterPosition()).getTm();
                    mb = albumList.get(getAdapterPosition()).getMB();
                    System.out.println(lt);
                    Intent i = new Intent(mContext,MainActivity.class);
                    i.putExtra("lat",lt);
                    i.putExtra("ln",ln);
                    i.putExtra("dt",sndt);
                    i.putExtra("tm",tm);
                    i.putExtra("mb",mb);
                    mContext.startActivity(i);
                }
            });


        }

//        @Override
//        public void onClick(View v) {
//            if (v.getId() == dohelp.getId()){
//            final double lt;
//            final double ln;
//            Integer sndt;
//            String tm;
//            lt = albumList.get(getAdapterPosition()).getLat();
//            ln = albumList.get(getAdapterPosition()).getLng();
//            sndt = albumList.get(getAdapterPosition()).getDt();
//            tm = albumList.get(getAdapterPosition()).getTm();
//            System.out.println(lt);
//            Intent i = new Intent(mContext,MainActivity.class);
//            mContext.startActivity(i);
//            v.getContext().startActivity(i);
//            itemView.getContext().startActivity(i);
//            }
//        }
    }


    public CustomAdapter(Context mContext, List<DataModel> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final DataModel album = albumList.get(position);
        holder.lat.setText(Double.toString(album.getLat()));
        holder.lon.setText(Double.toString(album.getLng()));
        holder.dt.setText(album.getDt());
        holder.tm.setText(album.getTm());
        holder.mob.setText(album.getMB());

    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }
}