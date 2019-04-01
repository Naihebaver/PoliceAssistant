package naihebaver.policeassistantapp.Activities;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import naihebaver.policeassistantapp.Adapters.HistoryAdapter;
import naihebaver.policeassistantapp.Models.Violation;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.R;

public class HistoryActivity extends AppCompatActivity {

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mUserHistoryRef;
    SharedPreferencesHelper mSharedPrefsHelper;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setupToolbar("Історія фіксацій");

        mSharedPrefsHelper = new SharedPreferencesHelper(this);
        mUserHistoryRef = mDatabase.getReference("History/");

        addListenerList();
    }

    private void addListenerList() {

        mUserHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Violation> list = new ArrayList<>();

                for (DataSnapshot d: Objects.requireNonNull(dataSnapshot).getChildren()) {

                    Violation violation = d.getValue(Violation.class);
                    if(violation != null && violation.getFrom_user() != null)
                        if(violation.getFrom_user().equals(mSharedPrefsHelper.getKey())) list.add(violation);
                }
                setRecycler(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setRecycler(List<Violation> list) {

        RecyclerView recyclerView = findViewById(R.id.recycler);
        HistoryAdapter adapter = new HistoryAdapter(list, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if( id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupToolbar(String tittle){
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(tittle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
