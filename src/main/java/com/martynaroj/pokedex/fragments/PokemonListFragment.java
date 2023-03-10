package com.martynaroj.pokedex.fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.martynaroj.pokedex.R;
import com.martynaroj.pokedex.SecondActivity;
import com.martynaroj.pokedex.base.BaseFragment;
import com.martynaroj.pokedex.fragments.adapters.PokemonListAdapter;
import com.martynaroj.pokedex.interfaces.NetworkListener;
import com.martynaroj.pokedex.interfaces.OnItemListener;
import com.martynaroj.pokedex.models.PokemonResponse;
import com.martynaroj.pokedex.models.PokemonUrl;
import com.martynaroj.pokedex.retrofit.Rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PokemonListFragment extends BaseFragment implements OnItemListener, NetworkListener {


    private SensorManager mSensorManager;
    private Sensor mAcceleometer;
    private double accelerationCurrentValue;
    private double accelerationPreviousValue;

    private RecyclerView recyclerView;
    private List<PokemonUrl> pokemonsList;
    private PokemonListAdapter adapter;
    private SearchView searchBar;
    private Button button;

    private Integer offset;
    private Integer limit;

    private boolean loading;
    private int firstVisibleItem, visibleItemCount, totalItemCount, previousTotal;
    private boolean root;


    public static PokemonListFragment newInstance() {
        return new PokemonListFragment();
    }

//========================================

    public PokemonListFragment() {
        pokemonsList = new ArrayList<>();
        previousTotal = 0;
        offset = 0;
        limit = 21;
        loading = true;
        root = true;
    }

//========================================

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_pokemon_list, container, false);

        findViews(rootView);
        setSearchBar();
        setButton();
        setAdapter();
        fetchData(null);

        return rootView;
    }

//========================================

    private void setSearchBar() {
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setIconified(false);
            }
        });
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s==null || s.equals("")) {
                    root = true;
                    offset = 0;
                    limit = 21;
                }
                else {
                    root = false;
                    offset = 0;
                    limit = 964;
                }
                recyclerView.scrollTo(0,0);
                adapter.clear();
                fetchData(s);
                return false;
            }
        });
    }
    //==========================================
    public void setButton() {
        button.setText("Go to SecondActivity");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SecondActivity.class);
                startActivity(intent);
            }
        });
    }





//========================================

    private void fetchData(final String s) {
        if (isNetworkConnected(getContext())) {
            pokemonsList.clear();
            Rest.getRest().getListPokemons(String.valueOf(offset), String.valueOf(limit)).enqueue(new Callback<PokemonResponse>() {
                @Override
                public void onResponse(Call<PokemonResponse> call, Response<PokemonResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        pokemonsList.addAll(response.body().getResults());
                        adapter.addList(pokemonsList);
                        adapter.getFilter().filter(s);
                    }
                }

                @Override
                public void onFailure(Call<PokemonResponse> call, Throwable t) {
                }
            });
        } else {
            showNetworkAlert();
        }
    }

//========================================

    private void findViews(View rootView) {
        recyclerView = rootView.findViewById(R.id.list_pokemons_recyclerview);
        searchBar = rootView.findViewById(R.id.searchView);
        button = rootView.findViewById(R.id.Button);
    }

//========================================

    public void showNetworkAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        alertDialogBuilder
                .setMessage("No internet connection on your device.")
                .setTitle("No Internet Connection")
                .setCancelable(false)
                .setPositiveButton("Refresh",
                        (dialog, id) -> {});
        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            alert.dismiss();
            if (!isNetworkConnected(getContext()))
                alert.show();
            else
                fetchData(null);
        });
    }

//=====================================

    private void setAdapter() {
        adapter = new PokemonListAdapter(getContext(), this);
        recyclerView.setHasFixedSize(true);
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (!root)
                    previousTotal = 0;
                else {
                    if (loading && (totalItemCount > previousTotal)) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                    if (!loading && (visibleItemCount + firstVisibleItem) >= totalItemCount) {
                        offset += 21;
                        fetchData(null);
                        loading = true;
                    }
                }
            }
        });

    }

//=========================================




    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            accelerationCurrentValue = Math.sqrt((x * x + y * y + z * z));

            double changeInAccelleration = Math.abs(accelerationCurrentValue - accelerationPreviousValue);
            accelerationPreviousValue = accelerationCurrentValue;


            if (changeInAccelleration > 0.01) {
                Random random = new Random();

                // Update the UI to display the selected item
                getNavigationsInteractions().changeFragment(PokemonDetails.newInstance(pokemonsList.get(random.nextInt(pokemonsList.size()))), true);
            }
            }


        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void onItemClick(int position) {
        Random random = new Random();
        getNavigationsInteractions().changeFragment(PokemonDetails.newInstance(adapter.getPokemon(position)), true);
    }
}
