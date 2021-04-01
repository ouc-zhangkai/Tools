package com.example.tools.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tools.Activity.NewsDetailsActivity;
import com.example.tools.MyData;
import com.example.tools.R;
import com.example.tools.SQLite.myApplication;
import com.example.tools.SQLite.operation;
import com.example.tools.Utils;
import com.example.tools.tools.Comments;
import com.example.tools.tools.Data;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.Response;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0; //说明是带有Header的
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_no =3;
    private String email;
    private int month;
    private int day;
    private boolean focus,type;
    private Context context;
    private GridViewAdapter gridAdpter;
    private List<Comments> list;
    private String token;
    private MyData myData;
    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 5000;


    public CommentAdapter(Context context,List<Comments> list) {
        this.context=context;
        this.list=list;
        myData = new MyData(context);
        token = myData.load_token();
        email=myData.load_email();
        Calendar c=Calendar.getInstance();
        month=c.get(Calendar.MONTH)+1;
        day=c.get(Calendar.DAY_OF_MONTH);
    }






    public void addData(List<Comments> addList){
        if (addList!=null){
            list.addAll(addList);
            notifyItemRangeChanged(list.size()-addList.size(),addList.size());
        }
    }


    @Override
    public int getItemViewType(int i) {
        if (list.get(i).getNoComments()!=null)
        {
            return TYPE_no;
        }  else {

        if (i>0 ){
            return TYPE_NORMAL;
        }
        if (i == 0){
            return TYPE_HEADER;
        }}

        return super.getItemViewType(i);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=null;
        RecyclerView.ViewHolder holder=null;
        if (i==TYPE_no){
            view= LayoutInflater.from(context).inflate(R.layout.item_no,viewGroup,false);
            holder= new ViewHolderNo(view);
        }
        if (i==TYPE_NORMAL){
            view= LayoutInflater.from(context).inflate(R.layout.item_comments,viewGroup,false);
            holder= new ViewHolderComments(view);}
        else if (i==TYPE_HEADER){
            view= LayoutInflater.from(context).inflate(R.layout.comment_header,viewGroup,false);
            holder= new ViewHolderNews(view);}
        return holder;

    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int i) {

        if (holder instanceof ViewHolderComments){
            ( (ViewHolderComments)holder).comment.setText(list.get(i).getComment_content());
            ( (ViewHolderComments)holder).writer.setText(list.get(i).getComment_writer());
            Glide.with(context).load(list.get(i).getPhoto()).error(R.drawable.errorhead).into(( (ViewHolderComments)holder).img);


        }else if (holder instanceof ViewHolderNews) {
            ((ViewHolderNews) holder).title.setText(list.get(i).getTitle());
            ((ViewHolderNews) holder).writer.setText(list.get(i).getWriter());
            ((ViewHolderNews) holder).content.setText(list.get(i).getContent());
            Glide.with(context).load(list.get(i).getPhoto()).error(R.drawable.user_icon).circleCrop().into(((ViewHolderNews) holder).photo);

            if (list.get(i).getPics() != null) {
                gridAdpter = new GridViewAdapter(context, list.get(i).getPics());
                ((ViewHolderNews) holder).gridView.setAdapter(gridAdpter);
            }


            if (list.get(i).getWriter().equals(myData.load_name())) {
                ((ViewHolderNews) holder).btn_focus.setVisibility(View.GONE);
            } else {
                focus = list.get(i).getFollow();
                Log.i("asd1", focus + "");
                if (focus) {
                    ((ViewHolderNews) holder).btn_focus.setBackgroundResource(R.drawable.button_focus);
                    ((ViewHolderNews) holder).btn_focus.setText("已关注");
                    ((ViewHolderNews) holder).btn_focus.setTextColor(context.getResources().getColor(R.color.gradientstart));
                } else {
                    ((ViewHolderNews) holder).btn_focus.setBackgroundResource(R.drawable.btn_focus_fill);
                    ((ViewHolderNews) holder).btn_focus.setText("关注");
                    ((ViewHolderNews) holder).btn_focus.setTextColor(context.getResources().getColor(R.color.white));
                }

                //关注按钮
                ((ViewHolderNews) holder).btn_focus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                            type = true;
                            try {
                                Utils.post_json(token, "http://122.9.2.27/api/users/" + list.get(i).getAuthor_id() + "/operator/follow", "", new Utils.OkhttpCallBack() {
                                    @Override
                                    public void onSuccess(final Response response) {

                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    JSONObject jsonObject21 = new JSONObject(Objects.requireNonNull(response.body()).string());
                                                    final String msg2 = jsonObject21.getString("msg");
                                                    Log.i("asd", msg2);
                                                    Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show();
                                                    if (msg2.equals("关注成功")) {
                                                        String write = list.get(i).getWriter();
                                                        DbManager dbManager = x.getDb(((myApplication) context.getApplicationContext()).getDaoConfig());
                                                        operation operation = new operation();
                                                        operation.setTitle(write);
                                                        operation.setType(5);
                                                        operation.setDate(month + "月" + day + "日");
                                                        operation.setRead(1);
                                                        operation.setEmail(email);
                                                        dbManager.save(operation);
                                                        ((ViewHolderNews) holder).btn_focus.setBackgroundResource(R.drawable.button_focus);
                                                        ((ViewHolderNews) holder).btn_focus.setText("已关注");
                                                        ((ViewHolderNews) holder).btn_focus.setTextColor(context.getResources().getColor(R.color.gradientstart));

                                                    } else {
                                                        ((ViewHolderNews) holder).btn_focus.setBackgroundResource(R.drawable.btn_focus_fill);
                                                        ((ViewHolderNews) holder).btn_focus.setText("关注");
                                                        ((ViewHolderNews) holder).btn_focus.setTextColor(context.getResources().getColor(R.color.white));
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFail(String error) {

                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "关注失败，过一会再试吧！", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            lastClickTime = System.currentTimeMillis();
                        } else {
                            if (type) {
                                type = false;
                                Toast.makeText(context, "操作频繁，过一会再试吧！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }

    }


    public int getItemCount() {

        return list.size();
    }


    public static class ViewHolderComments extends RecyclerView.ViewHolder {
        TextView writer,comment;
        ImageView img;
        public ViewHolderComments(@NonNull View itemView) {
            super(itemView);
            comment=itemView.findViewById(R.id.comments);
            writer=itemView.findViewById(R.id.comment_writer);
            img=itemView.findViewById(R.id.comment_head);

        }
    }

    public static class ViewHolderNo extends RecyclerView.ViewHolder {

        public ViewHolderNo(@NonNull View itemView) {
            super(itemView);

        }
    }

    public static class ViewHolderNews extends RecyclerView.ViewHolder {
        GridView gridView;
        TextView title,content,writer;
        Button btn_focus;
        ImageView photo;
        public ViewHolderNews(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.tital);
            btn_focus=itemView.findViewById(R.id.details_btn);
            gridView=itemView.findViewById(R.id.gridview);
            content=itemView.findViewById(R.id.details_news);
            photo=itemView.findViewById(R.id.details_photo);
            writer=itemView.findViewById(R.id.details_writer);
        }
    }
     }

