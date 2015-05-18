package ro.utcn.foodapp.presentation.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.business.ProductManager;
import ro.utcn.foodapp.business.RegistrationManager;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.model.Registration;
import ro.utcn.foodapp.presentation.adapters.ProductListAdapter;
import ro.utcn.foodapp.presentation.customViews.StyledExpandableListView;


public class MainActivity extends ActionBarActivity {

    private StyledExpandableListView expandableListView;
    private ProductListAdapter productListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton registerProductBtn;
    private List<Date> listProductRegistrationDate;
    private TreeMap<Date, List<Product>> productsGroupedByDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expandableListView = (StyledExpandableListView) findViewById(R.id.main_activity_products_expandable_list);
        registerProductBtn = (FloatingActionButton) findViewById(R.id.main_activity_register_product);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_products);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setTitle(getResources().getString(R.string.main_activity_title));

        productListAdapter = new ProductListAdapter(MainActivity.this);
        expandableListView.setAdapter(productListAdapter);


        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateProductsList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }


    /**
     * This method is called every time UI needs to be updated with products list
     */
    private void updateProductsList() {
        listProductRegistrationDate = new ArrayList<>();
        productsGroupedByDate = new TreeMap<>();

        List<Product> productsForReg = new ArrayList<>();
        List<Registration> productRegistrations = RegistrationManager.getInstance().getAllRegistrations();
        for (Registration registration : productRegistrations) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(registration.getRegistrationDate());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            if (!listProductRegistrationDate.contains(calendar.getTime())) {
                listProductRegistrationDate.add(calendar.getTime());
            }
            productsForReg.add(ProductManager.getInstance().getProduct(registration.getProductId()));
        }
        productsGroupedByDate = ProductManager.getInstance().groupProductsByRegDate(productRegistrations, productsForReg);

        productListAdapter.clearItems();
        productListAdapter.updateHeaderData(listProductRegistrationDate);
        productListAdapter.updateAllItems(productsGroupedByDate);

        productListAdapter.notifyDataSetChanged();

        for (int i = 0; i < productListAdapter.getHeaders().size(); ++i) {
            expandableListView.expandGroup(i);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {

                //deselectChild();

                Toast.makeText(MainActivity.this, "Child clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Toast.makeText(MainActivity.this, "Header clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        registerProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });
    }

}
