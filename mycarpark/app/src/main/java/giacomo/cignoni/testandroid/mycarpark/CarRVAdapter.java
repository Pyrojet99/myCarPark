package giacomo.cignoni.testandroid.mycarpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class CarRVAdapter extends ListAdapter<Car, CarRVAdapter.CarViewHolder>  {

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        private TextView carName;

        public CarViewHolder(View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.text_car_name);
        }

        public void bind(String stringAddr1) {
            carName.setText(stringAddr1);
        }

        static CarViewHolder create(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.car_rv_item, parent, false);
            return new CarViewHolder(view);
        }
    }



    public CarRVAdapter(@NonNull DiffUtil.ItemCallback<Car> diffCallback) {
        super(diffCallback);
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return CarViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        Car current = getItem(position);
        holder.bind(current.getName());
    }

    static class CarDiff extends DiffUtil.ItemCallback<Car> {

        @Override
        public boolean areItemsTheSame(@NonNull Car oldItem, @NonNull Car newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Car oldItem, @NonNull Car newItem) {
            return oldItem.getCarId() == newItem.getCarId();
        }
    }
}

