package giacomo.cignoni.testandroid.mycarpark;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

public class ParkRVAdapter extends PagedListAdapter<Park, RecyclerView.ViewHolder> {

        public static class ParkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //needed for calling methods from main
            protected MainActivity mainActivity;

            protected Park park;

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
                //reset address textviews
                textParkAddrLine1.setText("");
                textParkAddrLine2.setText("");

                if(p != null) {
                    if (p.getAddress().getLocality() != null) {
                        //uses locality if present
                        textParkAddrLine2.setText(p.getAddress().getLocality() + " " + p.getParkedCarId());
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
                        textParkAddrLine1.setText(mainActivity.getString(R.string.latitude_park_item, p.getAddress().getLatitude()));
                        textParkAddrLine2.setText(mainActivity.getString(R.string.longitude_park_item, p.getAddress().getLongitude()));
                    }

                    //sets end and start time
                    textTime.setText(mainActivity.getString(R.string.date_park_item,
                            Utils.getDateFromMillis(p.getStartTime(), "dd/MM/yyyy HH:mm"),
                            Utils.getDateFromMillis(p.getEndTime(), "dd/MM/yyyy HH:mm")));
                }
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
                        mainActivity.getMapUtility().addOldParkMarker(this.park);
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
        private ViewSwitcher viewSwitcherAlarm;
        private ImageButton buttonAddAlarm;
        private Chip chipAlarm;
        private boolean isSwitcherShowingButton;


        public CurrentParkViewHolder(View itemView, MainActivity mainActivity) {
            super(itemView, mainActivity);
            isSwitcherShowingButton = true;
            buttonDismiss = itemView.findViewById(R.id.button_dismiss_park);
            viewSwitcherAlarm = itemView.findViewById(R.id.viewswitcher_alarm);
            chipAlarm = itemView.findViewById(R.id.chip_alarm);
            buttonAddAlarm = itemView.findViewById(R.id.button_add_alarm);
            buttonDismiss.setOnClickListener(this);
            buttonAddAlarm.setOnClickListener(this);
            chipAlarm.setOnCloseIconClickListener(this);
        }

        @Override
        public void bind(Park p) {
           super.bind(p);
           if (super.park.getEndTime() == 0) {
               super.textTime.setText(Utils.getDateFromMillis(p.getStartTime(), "dd/MM/yyyy HH:mm"));
               //adds marker on map
               super.mainActivity.getMapUtility().addCurrParkMarker(p);
           }

           if (p.getAlarmTime() != 0) {
               //if alarm time has been set
               chipAlarm.setText(Utils.getDateFromMillis(p.getAlarmTime(), "HH:mm dd/MM"));
               if (isSwitcherShowingButton) {
                   //flip the switcher to show the chip
                   viewSwitcherAlarm.showNext();
                   isSwitcherShowingButton = false;
               }
           }
           else {
               //alarm has not been set
               if(!isSwitcherShowingButton) {
                   //flip the switch to show add alarm button
                   viewSwitcherAlarm.showNext();
                   isSwitcherShowingButton = true;
               }
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
                    super.mainActivity.getMapUtility().centerCameraOnMarker(super.park.getParkId());
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
                case R.id.button_add_alarm: {
                    //shows dialog
                    mainActivity.getAlarmUtility().showDialog(park);
                    break;
                }
                case R.id.chip_alarm: {
                    viewSwitcherAlarm.showNext();
                    isSwitcherShowingButton = true;
                    mainActivity.getAlarmUtility().removeAlarm(park);
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
        //from https://stackoverflow.com/questions/8200896/how-to-find-the-width-of-the-a-view-before-the-view-is-displayed
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

