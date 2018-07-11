package com.example.khangnt.mobifonefeedback.feedbacks;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.khangnt.mobifonefeedback.R;
import com.example.khangnt.mobifonefeedback.feedbacks.model.Feedback;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static String TAG = FeedbackAdapter.class.getSimpleName();
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private final int TYPE_LOADING = 2;
    private Context mContext;
    private List<Feedback> feedbacks;
    private static OnItemClickListener onItemClickListener;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isScrolling;
    private int totalFeedbackOfUser = 0;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    public FeedbackAdapter(Context context, List<Feedback> list,
                           RecyclerView recyclerView, int total) {
        this.mContext = context;
        onItemClickListener = (OnItemClickListener) mContext;
        feedbacks = list;
        this.totalFeedbackOfUser = total;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (onLoadMoreListener != null && !isScrolling &&
                            totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        isScrolling = true;
                        onLoadMoreListener.onLoadMore();
                    }
                }
            });
        }
    }

    public void setLoaded() {
        isScrolling = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row, parent, false);
            return new MyViewHolder(view);
        } else if (viewType == TYPE_FOOTER){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footer_layout, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_layout, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            String subject = feedbacks.get(position).getOriginalTitle();
            String content = feedbacks.get(position).getOverview();
            String time = feedbacks.get(position).getReleaseDate();

            ((MyViewHolder) holder).txt_fb_subject.setText(subject);
            ((MyViewHolder) holder).txt_time.setText(time);
            ((MyViewHolder) holder).txt_fb_content.setText(content);
        } else if (holder instanceof FooterViewHolder){
            StringBuilder footerText = new StringBuilder();
            if (totalFeedbackOfUser == 1) {
                footerText.append(totalFeedbackOfUser).append(" item");
            } else {
                footerText.append(totalFeedbackOfUser).append(" items");
            }
            ((FooterViewHolder)holder).txtFooter.setText(footerText.toString());
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return feedbacks.size();
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, position+"");
        if (position == totalFeedbackOfUser-1 && feedbacks.get(position) == null) {
            return TYPE_FOOTER;
        } else if (feedbacks.get(position) == null){
            return TYPE_LOADING;
        } else {
            return TYPE_ITEM;
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        protected TextView txt_fb_subject;
        protected TextView txt_time;
        protected TextView txt_fb_content;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.txt_fb_subject = (TextView) itemView.findViewById(R.id.txt_subject_fb);
            this.txt_time = (TextView)itemView.findViewById(R.id.txt_time);
            this.txt_fb_content = (TextView) itemView.findViewById(R.id.txt_content_fb);

            txt_fb_subject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view, getAdapterPosition());
                }
            });

            txt_fb_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view, getAdapterPosition());
                }
            });
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public TextView txtFooter;
        public FooterViewHolder (View v) {
            super(v);
            txtFooter = (TextView)v.findViewById(R.id.txt_footer_view);
        }

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
