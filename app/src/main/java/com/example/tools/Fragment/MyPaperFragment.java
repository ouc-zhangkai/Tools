package com.example.tools.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.tools.Activity.NewsDetailsActivity;
import com.example.tools.Activity.WriteActivity;
import com.example.tools.Adapter.CommentAdapter;
import com.example.tools.Adapter.PaperAdapter;
import com.example.tools.MyData;
import com.example.tools.R;
import com.example.tools.Utils;
import com.example.tools.tools.Comments;
import com.example.tools.tools.Data;
import com.example.tools.tools.MyNews;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Response;


public class MyPaperFragment extends Fragment {
    private RelativeLayout go_edit;
    private PaperAdapter adapter;
    private String token;
    private RecyclerView recyclerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_paper, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        go_edit= Objects.requireNonNull(getActivity()).findViewById(R.id.go_edit);
        go_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getActivity(), WriteActivity.class);
                startActivity(intent);
            }
        });
        recyclerView=getActivity().findViewById(R.id.myPaper_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        MyData myData=new MyData(getContext());
        token=myData.load_token();





        //加载新闻

        List<MyNews> list=new ArrayList<>();
        GetNews(list);


    }

    public void GetNews(final  List<MyNews> list){

        try {
            Utils.get_token("http://122.9.2.27/api/self/news-ids", token, new Utils.OkhttpCallBack() {
                @Override
                public void onSuccess(Response response) {
                    try {
                        JSONObject jsonObject=new JSONObject(response.body().string());
                        String msg=jsonObject.getString("msg");
                        Log.i("asd",msg);
                        JSONObject jsonObject1=jsonObject.getJSONObject("data");
                        JSONArray jsonArray=jsonObject1.getJSONArray("news");
                        Log.i("asd",jsonArray.length()+"");
                        if (jsonArray.length()!=0){
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject2=jsonArray.getJSONObject(i);
                            MyNews news=new MyNews();

                            news.setTitle(jsonObject2.getString("title"));
                            news.setId(jsonObject2.getInt("id"));
                            JSONArray jsonArray1=jsonObject2.getJSONArray("news_pic_set");
                            news.setImg(jsonArray1.getString(0));
                            news.setTag(jsonObject2.getInt("tag_type"));

                            list.add(news);
                        }}else {  MyNews news=new MyNews();news.setNo("no");list.add(news);}
                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("asd",list.size()+"");
                                adapter= new PaperAdapter(getActivity(), list);
                                recyclerView.setAdapter(adapter);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFail(String error) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            MyNews myNews=new MyNews();
                            myNews.setError("error");
                            list.add(myNews);
                            adapter= new PaperAdapter(getActivity(), list);
                            recyclerView.setAdapter(adapter);
                            Toast.makeText(getContext(),"连接失败，请刷新重试",Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}