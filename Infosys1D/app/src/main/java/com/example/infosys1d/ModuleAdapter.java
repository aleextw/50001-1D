package com.example.infosys1d;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ArrayViewHolder> {

    Context context;
    LayoutInflater mInflater;
    JSONArray dataSource;

    ModuleAdapter(Context context, JSONArray arr) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.dataSource = arr;
    }

    static class ArrayViewHolder extends RecyclerView.ViewHolder{
        TextView moduleName, moduleID;
        ArrayViewHolder(View view){
            super(view);
            moduleName = view.findViewById(R.id.module_card_module_name);
            moduleID = view.findViewById(R.id.module_card_module_id);
        }

    }

    @NonNull
    @Override
    public ModuleAdapter.ArrayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.module, viewGroup, false);
        return new ModuleAdapter.ArrayViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleAdapter.ArrayViewHolder arrayViewHolder, int i) {
        try {
            arrayViewHolder.moduleName.setText(dataSource.getJSONObject(i).getString("module_name"));
            arrayViewHolder.moduleID.setText(dataSource.getJSONObject(i).getString("module_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.length();
    }
}
