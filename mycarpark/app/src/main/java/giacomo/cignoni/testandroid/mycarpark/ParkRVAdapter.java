package giacomo.cignoni.testandroid.mycarpark;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ParkRVAdapter extends ListAdapter<Park, RecyclerView.ViewHolder>  {

        public static class ParkViewHolder extends RecyclerView.ViewHolder {
            //needed for calling methods from main
            protected MainActivity mainActivity;
            Park park;

            protected TextView textParkAddrLine1;
            protected TextView textParkAddrLine2;
            protected TextView textTime;


            public ParkViewHolder(View itemView, MainActivity mainActivity) {
                super(itemView);
                this.mainActivity = mainActivity;
                textParkAddrLine1 = itemView.findViewById(R.id.textview_addr_line1);
                textParkAddrLine2 = itemView.findViewById(R.id.textview_addr_line2);
                textTime = itemView.findViewById(R.id.textview_time);
            }

            public void bind(Park p) {
                this.park = p;

                textParkAddrLine1.setText(p.getAddress().getThoroughfare());
                textParkAddrLine2.setText(p.getAddress().getLocality()+ " " + p.getParkedCarId());
                textTime.setText(MainActivity.getDate(p.getStartTime(), "dd/MM/yyyy HH:mm")
                        + " - " + MainActivity.getDate(p.getEndTime(), "dd/MM/yyyy HH:mm"));

            }

            static ParkViewHolder create(ViewGroup parent, MainActivity ma) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.park_rv_item, parent, false);
                return new ParkViewHolder(view, ma);
            }
        }

    public static class CurrentParkViewHolder extends ParkViewHolder implements View.OnClickListener {
        private Button buttonDismiss;


        public CurrentParkViewHolder(View itemView, MainActivity mainActivity) {
            super(itemView, mainActivity);
            buttonDismiss = itemView.findViewById(R.id.button_dismiss_park);

        }

        public void bind(Park p) {
           super.bind(p);
           if( super.park.getEndTime() == 0) {
               super.textTime.setText(MainActivity.getDate(p.getStartTime(), "dd/MM/yyyy HH:mm"));
           }
        }

        static CurrentParkViewHolder create(ViewGroup parent, MainActivity ma) {
            Log.d("mytag", "create CurrentParkViewholder ");

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.curr_park_rv_item, parent, false);
            return new CurrentParkViewHolder(view, ma);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_dismiss_park: super.mainActivity.dismissPark();
            }
        }
    }


    MainActivity mainActivity;

    public ParkRVAdapter(@NonNull DiffUtil.ItemCallback<Park> diffCallback, MainActivity mainActivity) {
        super(diffCallback);
        this.mainActivity = mainActivity;
    }

    @Override
    public int getItemViewType(int position) {
        Park parkToBind = getItem(position);
        //current park if endTime == 0 (not initialized)
        Log.d("mytag", "parktobind endtime: "+parkToBind.getEndTime());

        if (parkToBind.getEndTime() == 0) return 0;
        else return  1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //viewType == 0 if current park
        if (viewType == 0) return CurrentParkViewHolder.create(parent, mainActivity);
        else return ParkViewHolder.create(parent, mainActivity);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Park parkToBind = getItem(position);
        if (holder.getItemViewType() == 0) {
            //bind current park
            CurrentParkViewHolder currParkViewHolder = (CurrentParkViewHolder) holder;
            currParkViewHolder.bind(parkToBind);
        } else {
            //bind non-current park
            ParkViewHolder parkViewHolder = (ParkViewHolder) holder;
            parkViewHolder.bind(parkToBind);
        }
    }


    static class ParkDiff extends DiffUtil.ItemCallback<Park> {
        @Override
        public boolean areItemsTheSame(@NonNull Park oldItem, @NonNull Park newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Park oldItem, @NonNull Park newItem) {
            return oldItem.getParkId() == newItem.getParkId();
        }
    }
}

