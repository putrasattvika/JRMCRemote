package io.github.putrasattvika.jrmcremote.ui.playlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.putrasattvika.jrmcremote.R;
import io.github.putrasattvika.jrmcremote.RxBus;
import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.event.PlayListUpdatedEvent;
import io.github.putrasattvika.jrmcremote.ui.common.AbstractFooterAdapter;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/9/16
 * Time: 10:16 PM
 * Desc: PlayListAdapter
 */
public class JRiverPlayListAdapter extends AbstractFooterAdapter<PlayList, PlayListItemView> {

    private Context mContext;

    private View mFooterView;
    private TextView textViewSummary;
    private View layoutRefreshPlaylist;

    private PlayListCallback mPlayListCallback;

    public JRiverPlayListAdapter(Context context, List<PlayList> data) {
        super(context, data);
        mContext = context;
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateFooterView();
            }
        });
    }

    @Override
    protected PlayListItemView createView(Context context) {
        return new PlayListItemView(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        if (holder.itemView instanceof PlayListItemView) {
            final PlayListItemView itemView = (PlayListItemView) holder.itemView;
            itemView.buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (mPlayListCallback != null) {
                        mPlayListCallback.onAction(itemView.buttonAction, position);
                    }
                }
            });
        }
        return holder;
    }

    // Footer View

    @Override
    protected boolean isFooterEnabled() {
        return true;
    }

    @Override
    protected View createFooterView() {
        if (mFooterView == null) {
            mFooterView = View.inflate(mContext, R.layout.item_play_list_footer, null);

            layoutRefreshPlaylist = mFooterView.findViewById(R.id.layout_add_play_list);
            layoutRefreshPlaylist.setVisibility(View.GONE);
            layoutRefreshPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RxBus.getInstance().post(new PlayListUpdatedEvent(null));
                }
            });

            textViewSummary = (TextView) mFooterView.findViewById(R.id.text_view_summary);
        }
        updateFooterView();
        return mFooterView;
    }

    public void updateFooterView() {
        if (textViewSummary == null) return;

        int itemCount = getItemCount() - 1; // real data count
        if (itemCount > 1) {
            textViewSummary.setVisibility(View.VISIBLE);
            textViewSummary.setText(mContext.getString(R.string.mp_play_list_footer_end_summary_formatter, itemCount));
        } else {
            textViewSummary.setVisibility(View.GONE);
        }
    }

    public void setRefreshPlaylistVisibility(boolean isVisible) {
        if (isVisible) {
            layoutRefreshPlaylist.setVisibility(View.VISIBLE);
        } else {
            layoutRefreshPlaylist.setVisibility(View.GONE);
        }
    }

    // Callback

    public void setAddPlayListCallback(PlayListCallback callback) {
        mPlayListCallback = callback;
    }

    interface PlayListCallback {

        void onAction(View actionView, int position);
    }
}
