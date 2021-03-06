package com.example.lyl.wandroid.view.ui.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lyl.wandroid.R;
import com.example.lyl.wandroid.adapter.HomeListAdapter;
import com.example.lyl.wandroid.adapter.HomePicAdapter;
import com.example.lyl.wandroid.modle.bean.HomeArticalBean;
import com.example.lyl.wandroid.presenter.HomeFragmentPresenter;
import com.example.lyl.wandroid.util.BaseContent;
import com.example.lyl.wandroid.util.Event;
import com.example.lyl.wandroid.view.customview.RefreshLayout;
import com.example.lyl.wandroid.view.iview.IHomeFragment;
import com.hejunlin.superindicatorlibray.CircleIndicator;
import com.hejunlin.superindicatorlibray.LoopViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements IHomeFragment, SwipeRefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {


    private HomeFragmentPresenter presenter;
    private HomeListAdapter adapter;
    private ProgressDialog progressDialog;
    private ListView listView;
    private RefreshLayout refreshLayout;
    private int page = 0;
    private List<HomeArticalBean.DataBean.DatasBean> datas;
    private boolean isRefreshing = false;
    private boolean isLoading = false;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        presenter = new HomeFragmentPresenter(this);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("正在请求");

        listView = (ListView) view.findViewById(R.id.lv);

        //初始化头布局
        View lvHeadView = LayoutInflater.from(getActivity()).inflate(R.layout.item_head_homelv,null );
        //初始化轮播图
        LoopViewPager lvp = (LoopViewPager) lvHeadView.findViewById(R.id.looperviewpager);
        CircleIndicator indicator = (CircleIndicator) lvHeadView.findViewById(R.id.indicator);
        lvp.setAdapter(new HomePicAdapter(getActivity()));
        lvp.setLooperPic(true);//5s自动轮播
        indicator.setViewPager(lvp); //indicator需要知道viewpager
        //添加完成
        listView.addHeaderView(lvHeadView);

        adapter = new HomeListAdapter();
        listView.setAdapter(adapter);

        refreshLayout = (RefreshLayout) view.findViewById(R.id.swipRefreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorAccent, R.color.colorPrimaryDark);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadListener(this);

        datas = new ArrayList<>();


        progressDialog.show();
        presenter.requestHomeList(page);


    }


    @Override
    public void response(HomeArticalBean bean) {
        if (isRefreshing){
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
        if (isLoading){
            refreshLayout.setLoading(false);
            isLoading = false;
        }
        progressDialog.dismiss();
        if (bean.getData().getDatas() != null) {
            for (int i = 0; i <bean.getData().getDatas().size() ; i++) {
                datas.add(bean.getData().getDatas().get(i));
            }
            adapter.setList(datas);
        } else {
            Toast.makeText(getActivity(), "" + bean.getErrorMsg(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void fail() {

        if (page >= 1) {
            page--;
        }

        if (isRefreshing){
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
        if (isLoading){
            refreshLayout.setLoading(false);
            isLoading = false;
        }
        progressDialog.dismiss();
        Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        page = 0;
        isRefreshing = true;
        datas.clear();
        presenter.requestHomeList(0);
    }

    @Override
    public void onLoad() {
        isLoading = true;
       ++page;
        presenter.requestHomeList(page);

    }

    @Subscribe
    public void onEventMainThread(Event event) {
        if (event.getMsg().equals(BaseContent.REFRESHHOMEFRAGMENT)){
            HomeArticalBean.DataBean.DatasBean datasBean = datas.get(event.getPosition());
            datasBean.setCollect(event.iscollect());
            datas.set(event.getPosition(),datasBean);
            adapter.setList(datas);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
