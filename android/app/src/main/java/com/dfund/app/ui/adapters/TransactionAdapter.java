package com.dfund.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.dfund.app.R;
import com.dfund.app.data.local.TransactionEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<TransactionEntity> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public void setItems(List<TransactionEntity> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionEntity tx = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvBankName.setText(tx.getBankName() != null ? tx.getBankName() : "Bank Account");
        holder.tvCategory.setText(tx.getCategory() != null ? tx.getCategory() : "GENERAL");
        holder.tvDate.setText(dateFormat.format(new Date(tx.getTimestamp())));

        boolean isCredit = "CREDIT".equalsIgnoreCase(tx.getType());
        boolean isEmi = tx.isEmi();

        if (isCredit) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "+ ₹%.2f", tx.getAmount()));
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.growth_emerald));
            holder.tvCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.growth_emerald_light));
            holder.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.growth_emerald));
        } else if (isEmi) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "- ₹%.2f", tx.getAmount()));
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.gold_amber));
            holder.tvCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.gold_amber_light));
            holder.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.gold_amber));
        } else {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "- ₹%.2f", tx.getAmount()));
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.alert_coral));
            holder.tvCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.alert_coral_light));
            holder.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.alert_coral));
        }

        if (tx.getVpa() != null && !tx.getVpa().isEmpty()) {
            holder.tvDetails.setVisibility(View.VISIBLE);
            holder.tvDetails.setText("UPI: " + tx.getVpa());
        } else {
            holder.tvDetails.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName, tvCategory, tvDate, tvAmount, tvDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tv_bank_name);
            tvCategory = itemView.findViewById(R.id.tv_category_tag);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDetails = itemView.findViewById(R.id.tv_details);
        }
    }
}
