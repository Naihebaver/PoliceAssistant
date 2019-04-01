package naihebaver.policeassistantapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import naihebaver.policeassistantapp.Activities.HistoryActivity;
import naihebaver.policeassistantapp.Activities.SendActivity;
import naihebaver.policeassistantapp.Models.Violation;
import naihebaver.policeassistantapp.R;


public class HistoryAdapter extends RecyclerView.Adapter {

    private List<Violation> list;
    Context context;

    public HistoryAdapter(List<Violation> _list, Context context) {
        list = _list;
        this.context = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_history, parent, false);
        vh = new HistoryHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HistoryHolder) {

            final Violation item = (Violation) list.get(position);

            if(item.getReg_number() != null){
                ((HistoryHolder) holder).reg_number.setText(item.getReg_number());
            }
            if(item.getAddress() != null){
                ((HistoryHolder) holder).tv_place.setText(item.getAddress());
            }

            if(item.getText_type() != null){
                ((HistoryHolder) holder).typeOffense.setText(item.getText_type());
            }


            ((HistoryHolder) holder).card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(item.getKey() != null){
                        try{
                            HistoryActivity activity = (HistoryActivity) context;
                            Intent intent = new Intent(activity, SendActivity.class);
                            intent.putExtra("key_h", item.getKey());
                            intent.putExtra("from_history", "from_history");
                            intent.putExtra("date", item.getDate());
                            //intent.putExtra("from_h", item.getFrom_user());
                            activity.startActivity(intent);
                        }catch (Exception e){}
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HistoryHolder extends RecyclerView.ViewHolder {
        public TextView reg_number, tv_place, typeOffense;
        CardView card;

        public HistoryHolder(View v) {
            super(v);
            reg_number = v.findViewById(R.id.tv_reg_number);
            tv_place = v.findViewById(R.id.tv_place);
            typeOffense = v.findViewById(R.id.typeOffense);
            card = v.findViewById(R.id.card);
        }
    }
}
