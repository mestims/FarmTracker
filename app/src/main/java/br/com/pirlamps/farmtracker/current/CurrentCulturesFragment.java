package br.com.pirlamps.farmtracker.current;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.pirlamps.farmtracker.BR;
import br.com.pirlamps.farmtracker.R;
import br.com.pirlamps.farmtracker.current.detail.CurrentCultureDetailFragment;
import br.com.pirlamps.farmtracker.databinding.FragmentCurrentCulturesBinding;
import br.com.pirlamps.farmtracker.foundation.joat.JoatAdapter;
import br.com.pirlamps.farmtracker.foundation.joat.JoatObject;
import br.com.pirlamps.farmtracker.foundation.model.CultureVO;
import br.com.pirlamps.farmtracker.foundation.util.JSONStringDate;
import br.com.pirlamps.farmtracker.main.MainActivity;
import br.com.pirlamps.farmtracker.foundation.util.TesteAdapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by root-matheus on 06/02/17.
 */

public class CurrentCulturesFragment extends Fragment {

    private JoatAdapter adapter;
    FragmentCurrentCulturesBinding binding;
    private CurrentCulturePresenter presenter;
    private DatabaseReference ref;
    List<CultureVO> cultures;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new JoatAdapter(getContext());
        presenter = new CurrentCulturePresenter();
        cultures = new ArrayList<>();
    }

    //TODO Remover o listener

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCurrentCulturesBinding.inflate(inflater,container,false);
        getCulturesData();


        this.prepareButtons();


        return binding.getRoot();
    }

    public void getCulturesData(){

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userUUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ref = FirebaseDatabase.getInstance().getReference()
                    .child("farmTracker")
                    .child("cultures")
                    .child(userUUID);

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cultures.clear();
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        cultures.add(child.getValue(CultureVO.class));
                    }

                    List<JoatObject> adapterList = new ArrayList<>();
                    for (CultureVO culture : cultures) {
                        adapterList.add(new JoatObject(R.layout.row_current_culture, BR.culture,culture,null));
                    }
                    adapter.setData(adapterList);
                    binding.outletCurrentCultureList.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void prepareButtons(){
        binding.outletCurrentCultureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CultureVO current = ((CultureVO) ((JoatObject) adapter.getItem(position)).getBindingObject());
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.mainFragment, CurrentCultureDetailFragment.newInstance(current), "CurrentCultureDetail");
                ft.addToBackStack("CurrentCultureDetail");
                ft.commit();
            }
        });


        binding.outletAddCultureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCulture();
            }
        });
    }

    public void newCulture(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_new_culture, (ViewGroup) getActivity().findViewById(R.id.activity_main),false);

//layout_root should be the name of the "top-level" layout node in the dialog_layout.xml file.
        final EditText cultureName = (EditText) layout.findViewById(R.id.outletCultureName);
        final EditText cultureLocation = (EditText) layout.findViewById(R.id.outletCultureLocation);

        //Building dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
        builder.setView(layout);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String cultureId = UUID.randomUUID().toString();
                String name = cultureName.getText().toString();
                String location = cultureLocation.getText().toString();
                String date = JSONStringDate.dateNow();
                Double budget = 0.0;
                CultureVO culture = new CultureVO(cultureId,name,location,date,budget);
                presenter.createNewCulture(culture);
                Toast.makeText(getContext(), "Nova cultura: "+cultureName.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();

//        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
//                                      @Override
//                                      public void onShow(DialogInterface arg0) {
//                                          dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.background));
//                                          dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.background));
//                                      }
//                                  });
        dialog.show();
    }
}
