package giacomo.cignoni.testandroid.mycarpark;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ParkRVAdapter extends ListAdapter<Park, RecyclerView.ViewHolder>  {

        public static class ParkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //needed for calling methods from main
            protected MainActivity mainActivity;
            Park park;

            protected TextView textParkAddrLine1;
            protected TextView textParkAddrLine2;
            protected TextView textTime;
            protected CardView cardView;
            protected ImageButton buttonMore;


            public ParkViewHolder(View itemView, MainActivity mainActivity) {
                super(itemView);
                this.mainActivity = mainActivity;
                textParkAddrLine1 = itemView.findViewById(R.id.textview_addr_line1);
                textParkAddrLine2 = itemView.findViewById(R.id.textview_addr_line2);
                textTime = itemView.findViewById(R.id.textview_time);
                cardView = itemView.findViewById(R.id.cardview_park);
                buttonMore = itemView.findViewById(R.id.button_more_park);

                cardView.setOnClickListener(this);
                buttonMore.setOnClickListener(this);
            }

            public void bind(Park p) {
                this.park = p;
                //initialize address textviews
                textParkAddrLine1.setText("");
                textParkAddrLine2.setText("");

                if (p.getAddress().getLocality() != null) {
                    //uses locality if present
                    textParkAddrLine2.setText(p.getAddress().getLocality()+ " " + p.getParkedCarId());
                }
                if (p.getAddress().getThoroughfare() != null) {
                    //uses thoroughfare if present
                    String addrLine1 = p.getAddress().getThoroughfare();
                    if (p.getAddress().getSubThoroughfare() != null) {
                        //uses subThoroughfare if present
                        addrLine1 = addrLine1 + ", " + p.getAddress().getSubThoroughfare();
                    }
                    textParkAddrLine1.setText(addrLine1);
                }
                if (p.getAddress().getThoroughfare() == null
                        && p.getAddress().getLocality() == null) {
                    //if both thoroughfare and locality are null, uses lat and long instead
                    textParkAddrLine1.setText("Lat: " + p.getAddress().getLatitude());
                    textParkAddrLine2.setText("Long: " + (p.getAddress().getLongitude()));
                }

                //sets end and start time
                textTime.setText(MainActivity.getDate(p.getStartTime(), "dd/MM/yyyy HH:mm")
                        + " - " + MainActivity.getDate(p.getEndTime(), "dd/MM/yyyy HH:mm"));

            }

            static ParkViewHolder create(ViewGroup parent, MainActivity ma) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.park_rv_item, parent, false);
                return new ParkViewHolder(view, ma);
            }

            protected void showMorePopup() {
                PopupMenu popup = new PopupMenu(mainActivity, buttonMore);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.more_park_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_item_park_remove: {
                            mainActivity.deletePark(this.park);
                            return true;
                        }
                        default:
                            return false;
                    }
                });
                popup.show();
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cardview_park: {
                        //adds marker when tapping on old park card
                        mainActivity.addOldParkMarker(this.park);
                        break;
                    }
                    case R.id.button_more_park: {
                        this.showMorePopup();
                        break;
                    }
                }

            }
        }

    public static class CurrentParkViewHolder extends ParkViewHolder implements View.OnClickListener {
        private Button buttonDismiss;


        public CurrentParkViewHolder(View itemView, MainActivity mainActivity) {
            super(itemView, mainActivity);
            buttonDismiss = itemView.findViewById(R.id.button_dismiss_park);
            buttonDismiss.setOnClickListener(this);
        }

        @Override
        public void bind(Park p) {
           super.bind(p);
           if (super.park.getEndTime() == 0) {
               super.textTime.setText(MainActivity.getDate(p.getStartTime(), "dd/MM/yyyy HH:mm"));
               //adds marker on map
               super.mainActivity.addCurrParkMarker(p);
           }
        }

        static CurrentParkViewHolder create(ViewGroup parent, MainActivity ma) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.curr_park_rv_item, parent, false);
            return new CurrentParkViewHolder(view, ma);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cardview_park: {
                    super.mainActivity.centerCameraOnMarker(super.park.getParkId());
                    break;
                }
                case R.id.button_dismiss_park: {
                    super.mainActivity.dismissPark(super.park);
                    break;
                }
                case R.id.button_more_park: {
                    super.showMorePopup();
                    break;
                }
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
            //if first item in recyclerview, set bottomSheet collapsed height to item height
            if (position == 0) setBottomSheetHeight(currParkViewHolder.itemView);

        } else {
            //bind non-current park
            ParkViewHolder parkViewHolder = (ParkViewHolder) holder;
            parkViewHolder.bind(parkToBind);
            //if first item in recyclerview, set bottomSheet collapsed height to item height
            if (position == 0) setBottomSheetHeight(parkViewHolder.itemView);
        }

    }

    /*
    Measures itemView height and sets bottomSheet height to measured height
     */
    private void setBottomSheetHeight(View itemView) {
        itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int height = itemView.getMeasuredHeight();
        mainActivity.getBottomSheetBehavior().setPeekHeight(height + 72);
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

