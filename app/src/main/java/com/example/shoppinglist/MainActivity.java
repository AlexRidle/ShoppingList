package com.example.shoppinglist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase mDatabase;
    private ProductAdapter mAdapter;
    private EditText mEditTextName;
    private TextView textView;
    ImageView downloadedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadCurrency();

        downloadedImage = findViewById(R.id.imageView);
        Picasso.get()
                .load("https://i.imgur.com/MhvTfLZ.png")
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder_error)
                .into(downloadedImage);

        ProductDBHelper dbHelper = new ProductDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ProductAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);

        textView = findViewById(R.id.textView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((long) viewHolder.itemView.getTag());
            }
        }).attachToRecyclerView(recyclerView);

        mEditTextName = findViewById(R.id.edittext_name);

        Button buttonAdd = findViewById(R.id.button_add);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    private void loadCurrency() {

        CurrencyApi currencyApi = Controller.getApi();
        currencyApi.getData(2).enqueue(new Callback<CurrencyModel>() {

            @Override
            public void onResponse(Call<CurrencyModel> call, Response<CurrencyModel> response) {
                textView.setText("1 BYN = " + response.body().getCurOfficialRate() + " USD");
            }

            @Override
            public void onFailure(Call<CurrencyModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MainActivity.this, "Cant load currency.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void addItem() {

        String name = mEditTextName.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(ProductContract.ProductEntry.COLUMN_NAME, name);

        mDatabase.insert(ProductContract.ProductEntry.TABLE_NAME, null, cv);
        mAdapter.swapCursor(getAllItems());

        mEditTextName.getText().clear();
    }

    private void removeItem(long id) {
        mDatabase.delete(ProductContract.ProductEntry.TABLE_NAME,
                ProductContract.ProductEntry._ID + "=" + id, null);
        mAdapter.swapCursor(getAllItems());
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                ProductContract.ProductEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ProductContract.ProductEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }
}