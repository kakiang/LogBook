package com.hervekakiang.logbook.ue;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.R;

public class UeListFragment extends Fragment {

    private UEListAdapter mAdapter;

    public static UeListFragment newInstance() {
        return new UeListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ue_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewUE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_ue);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddUeFragment addUeFragment = new AddUeFragment();
                addUeFragment.show(getChildFragmentManager(), "AddUeFragment");
            }
        });

        mAdapter = new UEListAdapter();
        recyclerView.setAdapter(mAdapter);

        UEViewModel mViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);

        mViewModel.getListUEs().observe(getViewLifecycleOwner(), ues -> {
            mAdapter.submitList(ues);
            Log.d("UEListFragment", "Size of list of UEs: " + ues.size());
        });
    }
}