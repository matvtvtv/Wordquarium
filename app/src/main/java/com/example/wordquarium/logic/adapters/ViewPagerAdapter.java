package com.example.wordquarium.logic.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.wordquarium.ui.fragments.CryptogramFragment;
import com.example.wordquarium.ui.fragments.ChainFragment;
import com.example.wordquarium.ui.fragments.WordlyFragment;

public class ViewPagerAdapter  extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChainFragment();
            case 1:
                return new WordlyFragment();
            case 2:
                return new CryptogramFragment();
            default:

                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
