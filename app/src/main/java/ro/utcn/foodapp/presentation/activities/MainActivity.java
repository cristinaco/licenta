package ro.utcn.foodapp.presentation.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.presentation.adapters.ProductListAdapter;
import ro.utcn.foodapp.presentation.customViews.StyledExpandableListView;


public class MainActivity extends ActionBarActivity {

    private StyledExpandableListView expandableListView;
    private ProductListAdapter productListAdapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private List<Date> listProductRegistrationDate;
    private TreeMap<Date, List<Product>> productsGroupedByDate;
    private boolean drawerArrowColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expandableListView = (StyledExpandableListView) findViewById(R.id.main_activity_products_expandable_list);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);


        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                drawerArrow, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        //mDrawerLayout.setDrawerListener(mDrawerToggle);
        // mDrawerToggle.setDrawerIndicatorEnabled(true);
        //mDrawerToggle.syncState();


        String[] values = new String[]{
                "Add product",
                "View products",
                "Preferences"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        mDrawerList.setAdapter(adapter);

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
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * This method is called every time UI needs to be updated with products list
     */
    private void updateProductsList() {
        listProductRegistrationDate = new ArrayList<>();
        productsGroupedByDate = new TreeMap<>();

        Date date = new Date();
        listProductRegistrationDate.add(date);

        Product product = new Product();
        product.setName("test");
        product.setExpirationDate(new Date());
        product.setPiecesNumber(500);
        Product product1 = new Product();
        product1.setName("Product name");
        product1.setExpirationDate(new Date());
        product1.setPiecesNumber(35);

        List<Product> products = new ArrayList<>();
        products.add(product);
        products.add(product1);
        productsGroupedByDate.put(date, products);

        productListAdapter.clearItems();
        productListAdapter.updateHeaderData(listProductRegistrationDate);
        productListAdapter.updateAllItems(productsGroupedByDate);

        productListAdapter.notifyDataSetChanged();

        for (int i = 0; i < productListAdapter.getHeaders().size(); ++i) {
            expandableListView.expandGroup(i);
        }


    }

    private void setListeners() {
        // Simple click on a child creates DetailsFragment
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

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
                        startActivity(intent);
//                        mDrawerToggle.setAnimateEnabled(false);
//                        drawerArrow.setProgress(1f);
                        break;
                    case 1:
//                        mDrawerToggle.setAnimateEnabled(false);
//                        drawerArrow.setProgress(0f);
                        break;
                }
            }
        });
    }

}
