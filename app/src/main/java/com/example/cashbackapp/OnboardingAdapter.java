package com.example.cashbackapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final String[] titles = {
            "Все ваши кэшбэк-категории \nв одном месте",
            "Анализируйте выгоду",
            "Планируйте покупки"
    };

    private final String[] descriptions = {
            "Добавляйте категории кэшбэка из разных банков и храните их в одном месте.",
            "Смотрите, с карты какого банка лучше всего оплатить покупку, чтобы получить максимальный кэшбэк.",
            "Используйте данные для максимальной выгоды при планировании покупок. Оптимизируйте свои расходы."
    };

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_onboarding,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.setOnboardingData(titles[position], descriptions[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
        }

        void setOnboardingData(String title, String description) {
            textTitle.setText(title);
            textDescription.setText(description);
        }
    }
}