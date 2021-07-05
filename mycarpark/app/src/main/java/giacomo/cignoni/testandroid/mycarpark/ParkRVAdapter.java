package giacomo.cignoni.testandroid.mycarpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ParkRVAdapter extends ListAdapter<Park, ParkRVAdapter.ParkViewHolder>  {

        public static class ParkViewHolder extends RecyclerView.ViewHolder {
            private TextView parkAddrLine1;
            private TextView parkAddrLine2;

            public ParkViewHolder(View itemView) {
                super(itemView);
                parkAddrLine1 = itemView.findViewById(R.id.addr_line1);
                parkAddrLine2 = itemView.findViewById(R.id.addr_line2);
            }

            public void bind(String stringAddr1, String stringAddr2) {
                parkAddrLine1.setText(stringAddr1);
                parkAddrLine2.setText(stringAddr2);
            }

            static ParkViewHolder create(ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.park_rv_item, parent, false);
                return new ParkViewHolder(view);
            }
        }



    public ParkRVAdapter(@NonNull DiffUtil.ItemCallback<Park> diffCallback) {
        super(diffCallback);
    }

    @Override
    public ParkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ParkViewHolder.create(parent);
    }

        @Override
        public void onBindViewHolder(ParkViewHolder holder, int position) {
        Park parkToBind = getItem(position);
        holder.bind(parkToBind.getAddress().getThoroughfare(), parkToBind.getAddress().getLocality()+" "+parkToBind.getParkedCarId());
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

