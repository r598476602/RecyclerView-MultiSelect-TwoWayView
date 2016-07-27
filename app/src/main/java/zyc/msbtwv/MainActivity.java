package zyc.msbtwv;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import org.lucasr.twowayview.ItemSelectionSupport;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<String>> {
    public static final int LOADER_ID = 101;

    @Bind(R.id.rv_demo)
    RecyclerView mRvDemo;

    private ItemSelectionSupport mSelectionSupport;
    private BaseAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mRvDemo.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        mRvDemo.setLayoutManager(new LinearLayoutManager(this));
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle args) {
        return new DemoLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
        mSelectionSupport = ItemSelectionSupport.addTo(mRvDemo);
        mSelectionSupport.setChoiceMode(ItemSelectionSupport.ChoiceMode.MULTIPLE);
        mAdapter = new BaseAdapter(data);
        mRvDemo.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {
        mAdapter.setData(null);
    }

    @OnClick({R.id.btn_reload_list, R.id.btn_toast_selected, R.id.btn_clear_all})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reload_list:
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                break;
            case R.id.btn_toast_selected:
                SparseBooleanArray sba = mSelectionSupport.getCheckedItemPositions();
                StringBuilder sb = new StringBuilder();
                Stream.ofRange(0, sba.size())
                        .filter(i -> sba.valueAt(i))
                        .forEach(i -> sb.append(sba.keyAt(i) + " "));
                Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_clear_all:
                mSelectionSupport.clearChoices();
                break;
        }
    }

    /****************************************************************************************************************/

    public static class BaseAdapter extends RvAdapter<String, BaseAdapter.BaseViewHolder> {
        public BaseAdapter(List<String> data) {
            super(data);
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BaseViewHolder(inflateView(R.layout.list_item, parent));
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            holder.bind(mList.get(position));
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        public static class BaseViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.tv_demo)
            TextView mTvDemo;

            public BaseViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }

            public void bind(String text) {
                mTvDemo.setText(text);
            }
        }
    }

    /****************************************************************************************************************/

    private static class DemoLoader extends AsyncTaskLoader<List<String>> {
        public DemoLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            // 直接forceLoad()是为了强制调用loadInBackground()。标准的pattern请参考ApiDemos里面LoaderCustom.java
            forceLoad();
        }

        @Override
        public List<String> loadInBackground() {
            return Stream.ofRange(0, 15 + (new Random()).nextInt(15))
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
    }
}
